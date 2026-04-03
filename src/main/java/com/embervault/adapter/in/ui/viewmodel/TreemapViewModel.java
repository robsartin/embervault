package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.domain.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Treemap view tab.
 *
 * <p>Computes a tab title of the form "Treemap: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages note display
 * items for treemap rendering and supports drill-down navigation.</p>
 */
public final class TreemapViewModel {

    private static final int MAX_TITLE_LENGTH = 20;

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> noteItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NavigationStack navigationStack = new NavigationStack();
    private final GetNoteQuery getNoteQuery;
    private final CreateNoteUseCase createNoteUseCase;
    private final StringProperty rootNoteTitle;
    private final AppState appState;
    private final EventBus eventBus;

    /**
     * Constructs a TreemapViewModel that derives its tab title from
     * the given note title property.
     *
     * @param noteTitle        the observable note title
     * @param getNoteQuery     the query interface for reading notes
     * @param createNoteUseCase the use case for creating notes
     * @param appState         the shared application state for
     *                         data-change notification
     */
    public TreemapViewModel(StringProperty noteTitle,
            GetNoteQuery getNoteQuery,
            CreateNoteUseCase createNoteUseCase,
            AppState appState, EventBus eventBus) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery,
                "getNoteQuery must not be null");
        this.createNoteUseCase = Objects.requireNonNull(
                createNoteUseCase,
                "createNoteUseCase must not be null");
        this.appState = Objects.requireNonNull(appState,
                "appState must not be null");
        this.eventBus = Objects.requireNonNull(eventBus,
                "eventBus must not be null");
        this.rootNoteTitle = noteTitle;
        updateTabTitle(noteTitle.get());
        noteTitle.addListener((obs, oldVal, newVal) -> {
            if (navigationStack.isAtRoot()) {
                updateTabTitle(newVal);
            }
        });
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
        String name = getNoteQuery.getNote(noteId)
                .map(Note::getTitle).orElse("Unknown");
        navigationStack.setCurrentId(noteId, name);
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
        Objects.requireNonNull(baseNoteId,
                "baseNoteId must be set before creating children");
        Note child = createNoteUseCase.createChildNote(baseNoteId, title);
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        eventBus.publish(new NoteCreatedEvent(child.getId()));
        return item;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return navigationStack.canNavigateBackProperty();
    }

    /**
     * Returns the observable breadcrumb trail for the current drill-down path.
     *
     * @return the unmodifiable observable list of breadcrumb entries
     */
    public ObservableList<BreadcrumbEntry> getBreadcrumbs() {
        return navigationStack.getBreadcrumbs();
    }

    /**
     * Navigates to the breadcrumb at the given index, reloading notes
     * and updating the tab title.
     *
     * @param index the zero-based breadcrumb index to navigate to
     */
    public void navigateToBreadcrumb(int index) {
        navigationStack.navigateTo(index);
        if (navigationStack.isAtRoot()) {
            updateTabTitle(rootNoteTitle.get());
        } else {
            getNoteQuery.getNote(navigationStack.getCurrentId())
                    .ifPresent(note -> updateTabTitle(note.getTitle()));
        }
        loadNotes();
    }

    /**
     * Drills down into a child note, making it the new base note.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        String name = getNoteQuery.getNote(noteId)
                .map(Note::getTitle).orElse("Unknown");
        navigationStack.push(noteId, name);
        updateTabTitle(name);
        loadNotes();
        eventBus.publish(new NoteMovedEvent(noteId));
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
        eventBus.publish(new NoteMovedEvent(previous));
    }

    /**
     * Computes treemap rectangle positions for the current notes at the given dimensions.
     *
     * <p>Each note is given equal weight (1.0) by default.</p>
     *
     * @param width  the available width
     * @param height the available height
     * @return the positioned rectangles
     */
    public List<TreemapRect> getTreemapRects(double width, double height) {
        List<TreemapItem> items = noteItems.stream()
                .map(n -> new TreemapItem(n.getId(), 1.0))
                .toList();
        return TreemapLayout.layout(items, 0, 0, width, height);
    }

    private void updateTabTitle(String title) {
        tabTitle.set(TextUtils.tabTitle("Treemap", title, MAX_TITLE_LENGTH));
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        return toDisplayItem(note, getNoteQuery.hasChildren(note.getId()));
    }

    private NoteDisplayItem toDisplayItem(Note note, boolean hasChildren) {
        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                0, 0, 0, 0,
                NoteDisplayHelper.resolveColorHex(note),
                hasChildren,
                NoteDisplayHelper.resolveBadge(note));
    }
}
