package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
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
    private final NoteService noteService;
    private final StringProperty rootNoteTitle;
    private final DataChangeSupport dataChangeSupport = new DataChangeSupport();

    /**
     * Constructs a TreemapViewModel that derives its tab title from the given note title property.
     *
     * @param noteTitle   the observable note title
     * @param noteService the note service for creating and querying notes
     */
    public TreemapViewModel(StringProperty noteTitle, NoteService noteService) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
        this.rootNoteTitle = noteTitle;
        updateTabTitle(noteTitle.get());
        noteTitle.addListener((obs, oldVal, newVal) -> {
            if (navigationStack.isAtRoot()) {
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
        dataChangeSupport.setOnDataChanged(callback);
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
        List<Note> children = noteService.getChildren(baseNoteId);
        Map<UUID, Boolean> hasChildrenMap = noteService.hasChildrenBatch(
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
        Note child = noteService.createChildNote(baseNoteId, title);
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        dataChangeSupport.notifyDataChanged();
        return item;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return navigationStack.canNavigateBackProperty();
    }

    /**
     * Drills down into a child note, making it the new base note.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        navigationStack.push(noteId);
        noteService.getNote(noteId).ifPresent(note ->
                updateTabTitle(note.getTitle()));
        loadNotes();
        dataChangeSupport.notifyDataChanged();
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
            noteService.getNote(previous).ifPresent(note ->
                    updateTabTitle(note.getTitle()));
        }
        loadNotes();
        dataChangeSupport.notifyDataChanged();
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
        return toDisplayItem(note, noteService.hasChildren(note.getId()));
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
