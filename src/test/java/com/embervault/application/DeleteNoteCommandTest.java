package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteNoteCommandTest {

    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
    }

    @Test
    @DisplayName("undo restores the deleted note")
    void undo_restoresDeletedNote() {
        Note note = Note.create("Test", "content");
        repository.save(note);
        DeleteNoteCommand cmd = new DeleteNoteCommand(repository, note);
        cmd.redo();
        assertFalse(repository.findById(note.getId()).isPresent());

        cmd.undo();

        assertTrue(repository.findById(note.getId()).isPresent());
        assertEquals("Test", repository.findById(note.getId())
                .orElseThrow().getTitle());
    }

    @Test
    @DisplayName("redo deletes the note again")
    void redo_deletesNoteAgain() {
        Note note = Note.create("Test", "content");
        repository.save(note);
        DeleteNoteCommand cmd = new DeleteNoteCommand(repository, note);
        cmd.redo();
        cmd.undo();

        cmd.redo();

        assertFalse(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("description mentions deletion")
    void description_mentionsDeletion() {
        Note note = Note.create("My Note", "content");
        DeleteNoteCommand cmd = new DeleteNoteCommand(repository, note);

        assertEquals("Delete note 'My Note'", cmd.description());
    }
}
