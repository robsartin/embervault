package com.embervault.adapter.in.ui.viewmodel;

import static com.embervault.domain.Attributes.TEXT;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the text pane that edits the currently selected note.
 *
 * <p>Listens to all views' selection changes and loads the selected note's
 * title and text content. Provides save methods that persist changes via
 * the NoteService and notify listeners for cross-view synchronization.</p>
 */
public final class SelectedNoteViewModel {

    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty text = new SimpleStringProperty("");
    private final NoteService noteService;
    private final DataChangeSupport dataChangeSupport = new DataChangeSupport();

    /**
     * Constructs a SelectedNoteViewModel.
     *
     * @param noteService the note service for querying and updating notes
     */
    public SelectedNoteViewModel(NoteService noteService) {
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
    }

    /**
     * Sets a callback to be invoked after any mutation operation.
     *
     * @param callback the callback to invoke, or null to clear
     */
    public void setOnDataChanged(Runnable callback) {
        dataChangeSupport.setOnDataChanged(callback);
    }

    /** Returns the selected note id property. */
    public ObjectProperty<UUID> selectedNoteIdProperty() {
        return selectedNoteId;
    }

    /** Returns the title property. */
    public StringProperty titleProperty() {
        return title;
    }

    /** Returns the text property. */
    public StringProperty textProperty() {
        return text;
    }

    /**
     * Selects a note by id, loading its title and text content.
     *
     * @param noteId the note id to select, or null to clear
     */
    public void setSelectedNoteId(UUID noteId) {
        selectedNoteId.set(noteId);
        if (noteId == null) {
            title.set("");
            text.set("");
            return;
        }
        noteService.getNote(noteId).ifPresentOrElse(note -> {
            title.set(note.getTitle());
            text.set(note.getContent());
        }, () -> {
            title.set("");
            text.set("");
        });
    }

    /**
     * Saves a new title for the currently selected note.
     *
     * @param newTitle the new title
     */
    public void saveTitle(String newTitle) {
        UUID noteId = selectedNoteId.get();
        if (noteId == null || newTitle == null || newTitle.isBlank()) {
            return;
        }
        noteService.renameNote(noteId, newTitle);
        title.set(newTitle);
        dataChangeSupport.notifyDataChanged();
    }

    /**
     * Saves new text content for the currently selected note.
     *
     * @param newText the new text content
     */
    public void saveText(String newText) {
        UUID noteId = selectedNoteId.get();
        if (noteId == null || newText == null) {
            return;
        }
        noteService.getNote(noteId).ifPresent(note -> {
            note.setAttribute(TEXT,
                    new AttributeValue.StringValue(newText));
        });
        text.set(newText);
        dataChangeSupport.notifyDataChanged();
    }
}
