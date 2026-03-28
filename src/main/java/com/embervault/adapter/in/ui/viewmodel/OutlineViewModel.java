package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
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
 * ViewModel for the Outline view tab.
 *
 * <p>Computes a tab title of the form "Outline: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages a tree structure
 * of notes for hierarchical display and supports drill-down navigation.</p>
 */
public final class OutlineViewModel {

    private static final int MAX_TITLE_LENGTH = 20;

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> rootItems =
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
     * Constructs an OutlineViewModel that derives its tab title from the given note title property.
     *
     * @param noteTitle   the observable note title
     * @param noteService the note service for creating and querying notes
     */
    public OutlineViewModel(StringProperty noteTitle, NoteService noteService) {
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

    /** Returns the observable list of root-level note display items. */
    public ObservableList<NoteDisplayItem> getRootItems() {
        return rootItems;
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
            rootItems.clear();
            return;
        }
        rootItems.setAll(
                noteService.getChildren(baseNoteId).stream()
                        .map(this::toDisplayItem)
                        .toList());
    }

    /**
     * Creates a new child note under the given parent with the given title.
     *
     * @param parentId the parent note id
     * @param title    the title for the new note
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNote(UUID parentId, String title) {
        Note child = noteService.createChildNote(parentId, title);
        NoteDisplayItem item = toDisplayItem(child);
        // Add to root items if parent is the base note
        if (parentId.equals(baseNoteId)) {
            rootItems.add(item);
        }
        notifyDataChanged();
        return item;
    }

    /**
     * Creates a new sibling note directly after the given sibling and reloads the tree.
     *
     * @param siblingId the sibling note id
     * @param title     the title for the new note (may be empty)
     * @return the display item for the created note
     */
    public NoteDisplayItem createSiblingNote(UUID siblingId, String title) {
        Note sibling = noteService.createSiblingNote(siblingId, title);
        NoteDisplayItem item = toDisplayItem(sibling);
        loadNotes();
        notifyDataChanged();
        return item;
    }

    /**
     * Indents a note (makes it a child of the note above) and reloads the tree.
     *
     * @param noteId the note id to indent
     */
    public void indentNote(UUID noteId) {
        noteService.indentNote(noteId);
        loadNotes();
        notifyDataChanged();
    }

    /**
     * Outdents a note (moves it up one level) and reloads the tree.
     *
     * @param noteId the note id to outdent
     */
    public void outdentNote(UUID noteId) {
        noteService.outdentNote(noteId);
        loadNotes();
        notifyDataChanged();
    }

    /**
     * Renames a note, updating the display item in the root items list if present.
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
        for (int i = 0; i < rootItems.size(); i++) {
            NoteDisplayItem item = rootItems.get(i);
            if (item.getId().equals(noteId)) {
                rootItems.set(i, new NoteDisplayItem(
                        item.getId(), newTitle, item.getContent(),
                        item.getXpos(), item.getYpos(),
                        item.getWidth(), item.getHeight(),
                        item.getColorHex(), item.isHasChildren()));
                break;
            }
        }
        notifyDataChanged();
        return true;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return canNavigateBack;
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

    /**
     * Returns whether the note with the given id has any children.
     *
     * @param noteId the note id
     * @return true if the note has children
     */
    public boolean hasChildren(UUID noteId) {
        return noteService.hasChildren(noteId);
    }

    /**
     * Returns the id of the previous note in outline order, or null if none.
     *
     * @param noteId the note id
     * @return the previous note id, or null
     */
    public UUID getPreviousNoteId(UUID noteId) {
        return noteService.getPreviousInOutline(noteId)
                .map(Note::getId)
                .orElse(null);
    }

    /**
     * Deletes a leaf note and reloads the tree.
     *
     * @param noteId the note id to delete
     * @return true if the note was deleted, false if it has children or does not exist
     */
    public boolean deleteNote(UUID noteId) {
        boolean deleted = noteService.deleteNoteIfLeaf(noteId);
        if (deleted) {
            loadNotes();
            notifyDataChanged();
        }
        return deleted;
    }

    /**
     * Returns the children of the given note as display items.
     *
     * @param parentId the parent note id
     * @return the list of child display items
     */
    public ObservableList<NoteDisplayItem> getChildren(UUID parentId) {
        return FXCollections.observableArrayList(
                noteService.getChildren(parentId).stream()
                        .map(this::toDisplayItem)
                        .toList());
    }

    private void updateTabTitle(String title) {
        tabTitle.set("Outline: " + TextUtils.truncate(title, MAX_TITLE_LENGTH));
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        String colorHex = note.getAttribute("$Color")
                .map(v -> ((AttributeValue.ColorValue) v).value())
                .map(TbxColor::toHex)
                .orElse("#808080");

        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                0, 0, 0, 0, colorHex,
                noteService.hasChildren(note.getId()));
    }
}
