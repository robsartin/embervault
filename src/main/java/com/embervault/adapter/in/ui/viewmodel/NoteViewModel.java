package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the note management screen.
 *
 * <p>Exposes observable properties that the View binds to. Depends on the
 * {@link NoteService} inbound port, not on infrastructure directly.
 * Exposes {@link NoteDisplayItem} to the View so that the View never
 * references domain classes.</p>
 */
public final class NoteViewModel {

    private final NoteService noteService;

    private final ObservableList<NoteDisplayItem> notes =
            FXCollections.observableArrayList();
    private final ObjectProperty<NoteDisplayItem> selectedNote =
            new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty content = new SimpleStringProperty("");

    /**
     * Constructs a NoteViewModel backed by the given use-case port.
     */
    public NoteViewModel(NoteService noteService) {
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
    }

    /** Returns the observable list of note display items. */
    public ObservableList<NoteDisplayItem> getNotes() {
        return notes;
    }

    /** Returns the selected-note property. */
    public ObjectProperty<NoteDisplayItem> selectedNoteProperty() {
        return selectedNote;
    }

    /** Returns the title property for the editor field. */
    public StringProperty titleProperty() {
        return title;
    }

    /** Returns the content property for the editor field. */
    public StringProperty contentProperty() {
        return content;
    }

    /** Loads all notes from the service into the observable list. */
    public void loadNotes() {
        notes.setAll(noteService.getAllNotes().stream()
                .map(NoteViewModel::toDisplayItem)
                .toList());
    }

    /** Creates a new note from the current title and content properties. */
    public void addNote() {
        Note created = noteService.createNote(title.get(), content.get());
        notes.add(toDisplayItem(created));
        clearEditor();
    }

    /** Saves changes to the currently selected note. */
    public void saveNote() {
        NoteDisplayItem selected = selectedNote.get();
        if (selected == null) {
            return;
        }
        Note updated = noteService.updateNote(
                selected.getId(), title.get(), content.get());
        NoteDisplayItem updatedItem = toDisplayItem(updated);
        int index = findNoteIndex(updated.getId());
        if (index >= 0) {
            notes.set(index, updatedItem);
        }
        selectedNote.set(updatedItem);
    }

    /** Deletes the currently selected note. */
    public void deleteNote() {
        NoteDisplayItem selected = selectedNote.get();
        if (selected == null) {
            return;
        }
        noteService.deleteNote(selected.getId());
        notes.remove(selected);
        clearEditor();
        selectedNote.set(null);
    }

    /**
     * Called when the user selects a note in the list.
     * Populates the editor fields from the selected display item.
     */
    public void selectNote(NoteDisplayItem item) {
        selectedNote.set(item);
        if (item != null) {
            title.set(item.getTitle());
            content.set(item.getContent());
        } else {
            clearEditor();
        }
    }

    private void clearEditor() {
        title.set("");
        content.set("");
    }

    private int findNoteIndex(UUID id) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static NoteDisplayItem toDisplayItem(Note note) {
        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent());
    }
}
