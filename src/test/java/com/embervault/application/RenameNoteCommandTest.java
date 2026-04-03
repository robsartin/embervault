package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.RenameNoteUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RenameNoteCommandTest {

    private NoteServiceImpl service;
    private RenameNoteUseCase renameUseCase;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
        renameUseCase = service;
    }

    @Test
    @DisplayName("execute() renames the note via RenameNoteUseCase")
    void execute_shouldRenameNote() {
        Note note = service.createNote("Original", "content");
        RenameNoteCommand command = new RenameNoteCommand(
                renameUseCase, service, note.getId(), "Renamed");

        command.execute();

        assertEquals("Renamed",
                service.getNote(note.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("undo() restores the original title")
    void undo_shouldRestoreOriginalTitle() {
        Note note = service.createNote("Original", "content");
        RenameNoteCommand command = new RenameNoteCommand(
                renameUseCase, service, note.getId(), "Renamed");
        command.execute();

        command.undo();

        assertEquals("Original",
                service.getNote(note.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("description() returns meaningful text")
    void description_shouldReturnMeaningfulText() {
        Note note = service.createNote("Original", "content");
        RenameNoteCommand command = new RenameNoteCommand(
                renameUseCase, service, note.getId(), "Renamed");

        assertEquals("Rename note to 'Renamed'", command.description());
    }
}
