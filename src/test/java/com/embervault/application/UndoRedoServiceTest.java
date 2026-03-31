package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UndoRedoService}.
 */
class UndoRedoServiceTest {

    private UndoRedoService undoRedoService;
    private NoteService noteService;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        undoRedoService = new UndoRedoService();
    }

    @Test
    @DisplayName("initially canUndo and canRedo are false")
    void initialState() {
        assertFalse(undoRedoService.canUndo());
        assertFalse(undoRedoService.canRedo());
    }

    @Test
    @DisplayName("after recording a change, canUndo is true")
    void recordChange_enablesUndo() {
        Note note = noteService.createNote("Title", "Content");
        undoRedoService.recordChange(note);

        assertTrue(undoRedoService.canUndo());
        assertFalse(undoRedoService.canRedo());
    }

    @Test
    @DisplayName("undo restores note to previous state")
    void undo_restoresPreviousState() {
        Note note = noteService.createNote("Original", "Content");
        undoRedoService.recordChange(note);

        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Changed"));
        repository.save(note);

        undoRedoService.undo(repository);

        Note restored = repository.findById(note.getId()).orElseThrow();
        assertEquals("Original", restored.getTitle());
    }

    @Test
    @DisplayName("redo re-applies an undone change")
    void redo_reappliesChange() {
        Note note = noteService.createNote("Original", "Content");
        undoRedoService.recordChange(note);

        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Changed"));
        repository.save(note);

        undoRedoService.undo(repository);
        assertTrue(undoRedoService.canRedo());

        undoRedoService.redo(repository);

        Note result = repository.findById(note.getId()).orElseThrow();
        assertEquals("Changed", result.getTitle());
    }

    @Test
    @DisplayName("new change after undo clears redo stack")
    void newChange_clearsRedoStack() {
        Note note = noteService.createNote("Original", "Content");
        undoRedoService.recordChange(note);

        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Changed"));
        repository.save(note);

        undoRedoService.undo(repository);
        assertTrue(undoRedoService.canRedo());

        undoRedoService.recordChange(note);
        assertFalse(undoRedoService.canRedo());
    }

    @Test
    @DisplayName("multiple undos work in reverse order")
    void multipleUndos_reverseOrder() {
        Note note = noteService.createNote("First", "Content");

        undoRedoService.recordChange(note);
        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Second"));
        repository.save(note);

        undoRedoService.recordChange(note);
        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Third"));
        repository.save(note);

        undoRedoService.undo(repository);
        assertEquals("Second",
                repository.findById(note.getId()).orElseThrow().getTitle());

        undoRedoService.undo(repository);
        assertEquals("First",
                repository.findById(note.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("undo stack respects max size limit")
    void undoStack_respectsMaxSize() {
        Note note = noteService.createNote("Start", "Content");

        for (int i = 0; i < 60; i++) {
            undoRedoService.recordChange(note);
            note.setAttribute(Attributes.NAME,
                    new AttributeValue.StringValue("Change " + i));
            repository.save(note);
        }

        int undoCount = 0;
        while (undoRedoService.canUndo()) {
            undoRedoService.undo(repository);
            undoCount++;
        }

        assertTrue(undoCount <= 50);
    }
}
