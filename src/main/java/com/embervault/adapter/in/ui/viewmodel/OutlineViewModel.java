package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Outline view tab.
 *
 * <p>Computes a tab title of the form "Outline: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages a tree structure
 * of notes for hierarchical display.</p>
 */
public final class OutlineViewModel {

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> rootItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NoteService noteService;
    private UUID baseNoteId;

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
        tabTitle.bind(Bindings.concat("Outline: ", noteTitle));
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
        return item;
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
        return true;
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
