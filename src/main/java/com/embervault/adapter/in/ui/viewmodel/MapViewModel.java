package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.RenameNoteUseCase;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Map view tab.
 *
 * <p>Computes a tab title of the form "Map: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages note display
 * items for spatial rendering and supports creating child notes.</p>
 */
public final class MapViewModel {

    private static final int MAX_TITLE_LENGTH = 20;
    private static final double SCALE_X = 40.0;
    private static final double SCALE_Y = 40.0;
    private static final double DEFAULT_WIDTH = 6.0;
    private static final double DEFAULT_HEIGHT = 4.0;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_IN_FACTOR = 1.25;
    private static final double ZOOM_OUT_FACTOR = 0.8;
    private static final double FIT_ALL_PADDING = 40.0;

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> noteItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NavigationStack navigationStack = new NavigationStack();
    private final GetNoteQuery getNoteQuery;
    private final CreateNoteUseCase createNoteUseCase;
    private final RenameNoteUseCase renameNoteUseCase;
    private final StringProperty rootNoteTitle;
    private final AppState appState;
    private final DoubleProperty zoomLevel = new SimpleDoubleProperty(1.0);
    private final ReadOnlyObjectWrapper<ZoomTier> currentTier =
            new ReadOnlyObjectWrapper<>(ZoomTier.NORMAL);

    /**
     * Constructs a MapViewModel that derives its tab title from the
     * given note title property.
     *
     * @param noteTitle        the observable note title
     * @param getNoteQuery     the query interface for reading notes
     * @param createNoteUseCase the use case for creating notes
     * @param renameNoteUseCase the use case for renaming notes
     * @param appState         the shared application state for
     *                         data-change notification
     */
    public MapViewModel(StringProperty noteTitle,
            GetNoteQuery getNoteQuery,
            CreateNoteUseCase createNoteUseCase,
            RenameNoteUseCase renameNoteUseCase,
            AppState appState) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery,
                "getNoteQuery must not be null");
        this.createNoteUseCase = Objects.requireNonNull(
                createNoteUseCase,
                "createNoteUseCase must not be null");
        this.renameNoteUseCase = Objects.requireNonNull(
                renameNoteUseCase,
                "renameNoteUseCase must not be null");
        this.appState = Objects.requireNonNull(appState,
                "appState must not be null");
        this.rootNoteTitle = noteTitle;
        updateTabTitle(noteTitle.get());
        // When the root note title changes and we're at the root level, update tab title
        noteTitle.addListener((obs, oldVal, newVal) -> {
            if (navigationStack.isAtRoot()) {
                updateTabTitle(newVal);
            }
        });

        // Update current tier when zoom level changes
        zoomLevel.addListener((obs, oldVal, newVal) ->
                currentTier.set(ZoomTier.fromZoomLevel(newVal.doubleValue())));
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }

    /** Returns the observable list of note display items. */
    public ObservableList<NoteDisplayItem> getNoteItems() {
        return noteItems;
    }

    /** Returns the selected note id property. */
    public ObjectProperty<UUID> selectedNoteIdProperty() {
        return selectedNoteId;
    }

    /**
     * Sets the base note (root container) whose children are displayed.
     *
     * @param noteId the base note id
     */
    public void setBaseNoteId(UUID noteId) {
        navigationStack.setCurrentId(noteId);
    }

    /** Returns the base note id. */
    public UUID getBaseNoteId() {
        return navigationStack.getCurrentId();
    }

    /**
     * Selects a note by id.
     *
     * @param noteId the note id to select, or null to clear selection
     */
    public void selectNote(UUID noteId) {
        selectedNoteId.set(noteId);
    }

    /** Loads the children of the base note into the observable list. */
    public void loadNotes() {
        UUID baseNoteId = navigationStack.getCurrentId();
        if (baseNoteId == null) {
            noteItems.clear();
            return;
        }
        List<Note> children = getNoteQuery.getChildren(baseNoteId);
        Map<UUID, Boolean> hasChildrenMap = getNoteQuery.hasChildrenBatch(
                children.stream().map(Note::getId).toList());
        noteItems.setAll(
                children.stream()
                        .map(note -> toDisplayItem(note,
                                hasChildrenMap.getOrDefault(note.getId(), false)))
                        .toList());
    }

    /**
     * Creates a new child note under the base note with the given title.
     *
     * @param title the title for the new note
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNote(String title) {
        UUID baseNoteId = navigationStack.getCurrentId();
        Objects.requireNonNull(baseNoteId, "baseNoteId must be set before creating children");
        Note child = createNoteUseCase.createChildNote(baseNoteId, title);
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        appState.notifyDataChanged();
        return item;
    }

    /**
     * Creates a new child note at the specified canvas position.
     *
     * @param title the title for the new note
     * @param xpos  the x position on the canvas
     * @param ypos  the y position on the canvas
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNoteAt(String title, double xpos, double ypos) {
        UUID baseNoteId = navigationStack.getCurrentId();
        Objects.requireNonNull(baseNoteId, "baseNoteId must be set before creating children");
        Note child = createNoteUseCase.createChildNote(baseNoteId, title);
        child.setAttribute(Attributes.XPOS, new AttributeValue.NumberValue(xpos / SCALE_X));
        child.setAttribute(Attributes.YPOS, new AttributeValue.NumberValue(ypos / SCALE_Y));
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        appState.notifyDataChanged();
        return item;
    }

    /**
     * Creates a new sibling note after the given sibling, positioned slightly below it.
     *
     * @param siblingId the id of the note to create a sibling after
     * @param title     the title for the new note (may be empty for rapid entry)
     * @return the display item for the created note
     */
    public NoteDisplayItem createSiblingNote(UUID siblingId, String title) {
        Note sibling = createNoteUseCase.createSiblingNote(siblingId, title);
        // Position the new note near the original sibling
        NoteDisplayItem siblingItem = findItemById(siblingId);
        if (siblingItem != null) {
            double offsetY = siblingItem.getYpos() + siblingItem.getHeight() + 20;
            sibling.setAttribute(Attributes.XPOS,
                    new AttributeValue.NumberValue(siblingItem.getXpos() / SCALE_X));
            sibling.setAttribute(Attributes.YPOS,
                    new AttributeValue.NumberValue(offsetY / SCALE_Y));
        }
        NoteDisplayItem item = toDisplayItem(sibling);
        noteItems.add(item);
        appState.notifyDataChanged();
        return item;
    }

    private NoteDisplayItem findItemById(UUID noteId) {
        for (NoteDisplayItem item : noteItems) {
            if (item.getId().equals(noteId)) {
                return item;
            }
        }
        return null;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return navigationStack.canNavigateBackProperty();
    }

    /** Returns the zoom level property. */
    public DoubleProperty zoomLevelProperty() {
        return zoomLevel;
    }

    /** Returns the current zoom tier property. */
    public ReadOnlyObjectProperty<ZoomTier> currentTierProperty() {
        return currentTier.getReadOnlyProperty();
    }

    /** Returns the current zoom tier. */
    public ZoomTier getCurrentTier() {
        return currentTier.get();
    }

    /**
     * Sets the zoom level, clamped between 0.1 and 5.0.
     *
     * @param level the desired zoom level
     */
    public void setZoomLevel(double level) {
        zoomLevel.set(Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, level)));
    }

    /** Zooms in by multiplying the current zoom level by 1.25. */
    public void zoomIn() {
        setZoomLevel(zoomLevel.get() * ZOOM_IN_FACTOR);
    }

    /** Zooms out by multiplying the current zoom level by 0.8. */
    public void zoomOut() {
        setZoomLevel(zoomLevel.get() * ZOOM_OUT_FACTOR);
    }

    /**
     * Calculates and sets the zoom level to fit all notes within the viewport.
     *
     * @param viewportWidth  the viewport width in pixels
     * @param viewportHeight the viewport height in pixels
     */
    public void fitAll(double viewportWidth, double viewportHeight) {
        if (noteItems.isEmpty()) {
            return;
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for (NoteDisplayItem item : noteItems) {
            minX = Math.min(minX, item.getXpos());
            minY = Math.min(minY, item.getYpos());
            maxX = Math.max(maxX, item.getXpos() + item.getWidth());
            maxY = Math.max(maxY, item.getYpos() + item.getHeight());
        }
        double contentWidth = maxX - minX;
        double contentHeight = maxY - minY;
        if (contentWidth <= 0 || contentHeight <= 0) {
            return;
        }
        double scaleX = viewportWidth / (contentWidth + FIT_ALL_PADDING);
        double scaleY = viewportHeight / (contentHeight + FIT_ALL_PADDING);
        setZoomLevel(Math.min(scaleX, scaleY));
    }

    /**
     * Renames a note, updating the display item in the list.
     *
     * @param noteId   the note id
     * @param newTitle the new title
     * @return true if the rename succeeded, false if the title was blank
     */
    public boolean renameNote(UUID noteId, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            return false;
        }
        renameNoteUseCase.renameNote(noteId, newTitle);
        for (int i = 0; i < noteItems.size(); i++) {
            NoteDisplayItem item = noteItems.get(i);
            if (item.getId().equals(noteId)) {
                noteItems.set(i, new NoteDisplayItem(
                        item.getId(), newTitle, item.getContent(),
                        item.getXpos(), item.getYpos(),
                        item.getWidth(), item.getHeight(),
                        item.getColorHex(), item.isHasChildren(),
                        item.getBadge()));
                break;
            }
        }
        appState.notifyDataChanged();
        return true;
    }

    /**
     * Drills down into a child note, making it the new base note.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        navigationStack.push(noteId);
        getNoteQuery.getNote(noteId).ifPresent(note ->
                updateTabTitle(note.getTitle()));
        loadNotes();
        appState.notifyDataChanged();
    }

    /**
     * Navigates back to the previous base note.
     */
    public void navigateBack() {
        UUID previous = navigationStack.pop();
        if (previous == null) {
            return;
        }
        if (navigationStack.isAtRoot()) {
            updateTabTitle(rootNoteTitle.get());
        } else {
            getNoteQuery.getNote(previous).ifPresent(note ->
                    updateTabTitle(note.getTitle()));
        }
        loadNotes();
        appState.notifyDataChanged();
    }

    private void updateTabTitle(String title) {
        tabTitle.set(TextUtils.tabTitle("Map", title, MAX_TITLE_LENGTH));
    }

    /**
     * Updates a note's position by setting its $Xpos and $Ypos attributes.
     *
     * @param noteId the note id
     * @param xpos   the new x position
     * @param ypos   the new y position
     */
    public void updateNotePosition(UUID noteId, double xpos, double ypos) {
        getNoteQuery.getNote(noteId).ifPresent(note -> {
            note.setAttribute(Attributes.XPOS, new AttributeValue.NumberValue(xpos / SCALE_X));
            note.setAttribute(Attributes.YPOS, new AttributeValue.NumberValue(ypos / SCALE_Y));
        });
        // Update the display item in place
        for (int i = 0; i < noteItems.size(); i++) {
            NoteDisplayItem item = noteItems.get(i);
            if (item.getId().equals(noteId)) {
                noteItems.set(i, new NoteDisplayItem(
                        item.getId(), item.getTitle(), item.getContent(),
                        xpos, ypos,
                        item.getWidth(), item.getHeight(),
                        item.getColorHex(), item.isHasChildren(),
                        item.getBadge()));
                break;
            }
        }
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        return toDisplayItem(note, getNoteQuery.hasChildren(note.getId()));
    }

    private NoteDisplayItem toDisplayItem(Note note, boolean hasChildren) {
        double xpos = NoteDisplayHelper.resolveNumber(
                note, Attributes.XPOS, 0.0) * SCALE_X;
        double ypos = NoteDisplayHelper.resolveNumber(
                note, Attributes.YPOS, 0.0) * SCALE_Y;
        double width = NoteDisplayHelper.resolveNumber(
                note, Attributes.WIDTH, DEFAULT_WIDTH) * SCALE_X;
        double height = NoteDisplayHelper.resolveNumber(
                note, Attributes.HEIGHT, DEFAULT_HEIGHT) * SCALE_Y;

        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                xpos, ypos, width, height,
                NoteDisplayHelper.resolveColorHex(note),
                hasChildren,
                NoteDisplayHelper.resolveBadge(note));
    }
}
