package com.embervault.adapter.in.ui.viewmodel;

import static com.embervault.domain.Attributes.TEXT;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.RenameNoteUseCase;
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
 * the narrow use-case interfaces and notify listeners for cross-view
 * synchronization.</p>
 */
public final class SelectedNoteViewModel {

    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty text = new SimpleStringProperty("");
    private final GetNoteQuery getNoteQuery;
    private final RenameNoteUseCase renameNoteUseCase;
    private final AppState appState;
    private final EventBus eventBus;

    /**
     * Constructs a SelectedNoteViewModel.
     *
     * @param getNoteQuery     the query interface for reading notes
     * @param renameNoteUseCase the use case for renaming notes
     * @param appState         the shared application state for
     *                         data-change notification
     */
    public SelectedNoteViewModel(GetNoteQuery getNoteQuery,
            RenameNoteUseCase renameNoteUseCase, AppState appState,
            EventBus eventBus) {
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery,
                "getNoteQuery must not be null");
        this.renameNoteUseCase = Objects.requireNonNull(renameNoteUseCase,
                "renameNoteUseCase must not be null");
        this.appState = Objects.requireNonNull(appState,
                "appState must not be null");
        this.eventBus = Objects.requireNonNull(eventBus,
                "eventBus must not be null");
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
        getNoteQuery.getNote(noteId).ifPresentOrElse(note -> {
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
        renameNoteUseCase.renameNote(noteId, newTitle);
        title.set(newTitle);
        eventBus.publish(new NoteRenamedEvent(noteId, newTitle));
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
        getNoteQuery.getNote(noteId).ifPresent(note -> {
            note.setAttribute(TEXT,
                    new AttributeValue.StringValue(newText));
        });
        text.set(newText);
        eventBus.publish(new NoteUpdatedEvent(noteId));
    }
}
