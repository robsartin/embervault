package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandHistoryIntegrationTest {

    private NoteServiceImpl service;
    private CommandHistory history;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
        history = new CommandHistory();
    }

    @Test
    @DisplayName("rename via history then undo restores original title")
    void renameViaHistory_thenUndo_shouldRestoreTitle() {
        Note note = service.createNote("Alpha", "content");
        RenameNoteCommand cmd = new RenameNoteCommand(
                service, service, note.getId(), "Beta");

        history.execute(cmd);
        assertEquals("Beta",
                service.getNote(note.getId()).orElseThrow().getTitle());

        history.undo();
        assertEquals("Alpha",
                service.getNote(note.getId()).orElseThrow().getTitle());

        history.redo();
        assertEquals("Beta",
                service.getNote(note.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("multiple commands undo in reverse order")
    void multipleCommands_undoInReverseOrder() {
        Note note = service.createNote("First", "content");

        history.execute(new RenameNoteCommand(
                service, service, note.getId(), "Second"));
        history.execute(new RenameNoteCommand(
                service, service, note.getId(), "Third"));

        assertEquals("Third",
                service.getNote(note.getId()).orElseThrow().getTitle());

        history.undo();
        assertEquals("Second",
                service.getNote(note.getId()).orElseThrow().getTitle());

        history.undo();
        assertEquals("First",
                service.getNote(note.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("new execute after undo clears redo stack")
    void newExecuteAfterUndo_clearsRedoStack() {
        Note note = service.createNote("Original", "content");

        history.execute(new RenameNoteCommand(
                service, service, note.getId(), "Changed"));
        history.undo();
        assertTrue(history.canRedo());

        history.execute(new RenameNoteCommand(
                service, service, note.getId(), "Different"));
        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("text update via history then undo restores original text")
    void textUpdateViaHistory_thenUndo_shouldRestoreText() {
        Note note = service.createNote("Title", "original");
        UpdateNoteTextCommand cmd = new UpdateNoteTextCommand(
                service, service, note.getId(), "updated");

        history.execute(cmd);
        assertEquals("updated",
                service.getNote(note.getId()).orElseThrow().getText());

        history.undo();
        assertEquals("original",
                service.getNote(note.getId()).orElseThrow().getText());
    }
}
