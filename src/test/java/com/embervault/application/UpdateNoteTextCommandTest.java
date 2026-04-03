package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.UpdateNoteTextUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateNoteTextCommandTest {

    private NoteServiceImpl service;
    private UpdateNoteTextUseCase updateTextUseCase;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
        updateTextUseCase = service;
    }

    @Test
    @DisplayName("execute() updates the note text")
    void execute_shouldUpdateNoteText() {
        Note note = service.createNote("Title", "original text");
        UpdateNoteTextCommand command = new UpdateNoteTextCommand(
                updateTextUseCase, service, note.getId(), "new text");

        command.execute();

        assertEquals("new text",
                service.getNote(note.getId()).orElseThrow().getText());
    }

    @Test
    @DisplayName("undo() restores the original text")
    void undo_shouldRestoreOriginalText() {
        Note note = service.createNote("Title", "original text");
        UpdateNoteTextCommand command = new UpdateNoteTextCommand(
                updateTextUseCase, service, note.getId(), "new text");
        command.execute();

        command.undo();

        assertEquals("original text",
                service.getNote(note.getId()).orElseThrow().getText());
    }

    @Test
    @DisplayName("description() returns meaningful text")
    void description_shouldReturnMeaningfulText() {
        Note note = service.createNote("Title", "original text");
        UpdateNoteTextCommand command = new UpdateNoteTextCommand(
                updateTextUseCase, service, note.getId(), "new text");

        assertEquals("Update note text", command.description());
    }
}
