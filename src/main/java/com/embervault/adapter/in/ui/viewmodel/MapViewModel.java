package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.BadgeRegistry;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
    private static final String DEFAULT_COLOR_HEX = "#808080";

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> noteItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final BooleanProperty canNavigateBack =
            new SimpleBooleanProperty(false);
    private final NoteService noteService;
    private final StringProperty rootNoteTitle;
    private final Deque<UUID> navigationHistory = new ArrayDeque<>();
    private UUID baseNoteId;
    private Runnable onDataChanged;

    /**
     * Constructs a MapViewModel that derives its tab title from the given note title property.
     *
     * @param noteTitle   the observable note title
     * @param noteService the note service for creating and querying notes
     */
    public MapViewModel(StringProperty noteTitle, NoteService noteService) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
        this.rootNoteTitle = noteTitle;
        updateTabTitle(noteTitle.get());
        // When the root note title changes and we're at the root level, update tab title
        noteTitle.addListener((obs, oldVal, newVal) -> {
            if (navigationHistory.isEmpty()) {
                updateTabTitle(newVal);
            }
        });
    }

    /**
     * Sets a callback to be invoked after any mutation operation.
     *
     * @param callback the callback to invoke, or null to clear
     */
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
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
        this.baseNoteId = noteId;
    }

    /** Returns the base note id. */
    public UUID getBaseNoteId() {
        return baseNoteId;
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
        if (baseNoteId == null) {
            noteItems.clear();
            return;
        }
        noteItems.setAll(
                noteService.getChildren(baseNoteId).stream()
                        .map(this::toDisplayItem)
                        .toList());
    }

    /**
     * Creates a new child note under the base note with the given title.
     *
     * @param title the title for the new note
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNote(String title) {
        Objects.requireNonNull(baseNoteId, "baseNoteId must be set before creating children");
        Note child = noteService.createChildNote(baseNoteId, title);
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        notifyDataChanged();
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
        Objects.requireNonNull(baseNoteId, "baseNoteId must be set before creating children");
        Note child = noteService.createChildNote(baseNoteId, title);
        child.setAttribute("$Xpos", new AttributeValue.NumberValue(xpos / SCALE_X));
        child.setAttribute("$Ypos", new AttributeValue.NumberValue(ypos / SCALE_Y));
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        notifyDataChanged();
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
        Note sibling = noteService.createSiblingNote(siblingId, title);
        // Position the new note near the original sibling
        NoteDisplayItem siblingItem = findItemById(siblingId);
        if (siblingItem != null) {
            double offsetY = siblingItem.getYpos() + siblingItem.getHeight() + 20;
            sibling.setAttribute("$Xpos",
                    new AttributeValue.NumberValue(siblingItem.getXpos() / SCALE_X));
            sibling.setAttribute("$Ypos",
                    new AttributeValue.NumberValue(offsetY / SCALE_Y));
        }
        NoteDisplayItem item = toDisplayItem(sibling);
        noteItems.add(item);
        notifyDataChanged();
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
        return canNavigateBack;
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
        noteService.renameNote(noteId, newTitle);
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
        notifyDataChanged();
        return true;
    }

    /**
     * Drills down into a child note, making it the new base note.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        navigationHistory.push(baseNoteId);
        canNavigateBack.set(true);
        baseNoteId = noteId;
        noteService.getNote(noteId).ifPresent(note ->
                updateTabTitle(note.getTitle()));
        loadNotes();
        notifyDataChanged();
    }

    /**
     * Navigates back to the previous base note.
     */
    public void navigateBack() {
        if (navigationHistory.isEmpty()) {
            return;
        }
        baseNoteId = navigationHistory.pop();
        canNavigateBack.set(!navigationHistory.isEmpty());
        if (navigationHistory.isEmpty()) {
            updateTabTitle(rootNoteTitle.get());
        } else {
            noteService.getNote(baseNoteId).ifPresent(note ->
                    updateTabTitle(note.getTitle()));
        }
        loadNotes();
        notifyDataChanged();
    }

    private void updateTabTitle(String title) {
        tabTitle.set("Map: " + TextUtils.truncate(title, MAX_TITLE_LENGTH));
    }

    /**
     * Updates a note's position by setting its $Xpos and $Ypos attributes.
     *
     * @param noteId the note id
     * @param xpos   the new x position
     * @param ypos   the new y position
     */
    public void updateNotePosition(UUID noteId, double xpos, double ypos) {
        noteService.getNote(noteId).ifPresent(note -> {
            note.setAttribute("$Xpos", new AttributeValue.NumberValue(xpos / SCALE_X));
            note.setAttribute("$Ypos", new AttributeValue.NumberValue(ypos / SCALE_Y));
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
        double xpos = note.getAttribute("$Xpos")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_X)
                .orElse(0.0);
        double ypos = note.getAttribute("$Ypos")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_Y)
                .orElse(0.0);
        double width = note.getAttribute("$Width")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_X)
                .orElse(DEFAULT_WIDTH * SCALE_X);
        double height = note.getAttribute("$Height")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_Y)
                .orElse(DEFAULT_HEIGHT * SCALE_Y);
        String colorHex = note.getAttribute("$Color")
                .map(v -> ((AttributeValue.ColorValue) v).value())
                .map(TbxColor::toHex)
                .orElse(DEFAULT_COLOR_HEX);
        String badge = resolveBadge(note);

        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                xpos, ypos, width, height, colorHex,
                noteService.hasChildren(note.getId()), badge);
    }

    private static String resolveBadge(Note note) {
        return note.getAttribute("$Badge")
                .map(v -> ((AttributeValue.StringValue) v).value())
                .flatMap(BadgeRegistry::getBadgeSymbol)
                .orElse("");
    }
}
