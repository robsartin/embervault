package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that NoteServiceImpl auto-records changes to UndoRedoService.
 */
class UndoRedoAutoRecordTest {

    private UndoRedoService undoRedoService;
    private NoteServiceImpl noteService;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        undoRedoService = new UndoRedoService();
        noteService = new NoteServiceImpl(repository, undoRedoService);
    }

    @Test
    @DisplayName("renameNote auto-records for undo")
    void renameNote_autoRecords() {
        Note note = noteService.createNote("Original", "Content");

        noteService.renameNote(note.getId(), "Renamed");
        assertTrue(undoRedoService.canUndo());

        undoRedoService.undo(repository);
        assertEquals("Original",
                repository.findById(note.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("updateNote auto-records for undo")
    void updateNote_autoRecords() {
        Note note = noteService.createNote("Title", "Original text");

        noteService.updateNote(note.getId(), "Title", "New text");
        assertTrue(undoRedoService.canUndo());

        undoRedoService.undo(repository);
        assertEquals("Original text",
                repository.findById(note.getId()).orElseThrow().getContent());
    }

    @Test
    @DisplayName("moveNote auto-records for undo")
    void moveNote_autoRecords() {
        Note parent1 = noteService.createNote("Parent1", "");
        Note parent2 = noteService.createNote("Parent2", "");
        Note child = noteService.createChildNote(parent1.getId(), "Child");

        noteService.moveNote(child.getId(), parent2.getId());
        assertTrue(undoRedoService.canUndo());
    }
}
