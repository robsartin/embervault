package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateNoteCommandTest {

    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
    }

    @Test
    @DisplayName("undo deletes the created note")
    void undo_deletesCreatedNote() {
        Note note = Note.create("Test", "content");
        repository.save(note);
        CreateNoteCommand cmd = new CreateNoteCommand(repository, note);

        cmd.undo();

        assertFalse(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("redo re-saves the note")
    void redo_reSavesNote() {
        Note note = Note.create("Test", "content");
        repository.save(note);
        CreateNoteCommand cmd = new CreateNoteCommand(repository, note);
        cmd.undo();

        cmd.redo();

        assertTrue(repository.findById(note.getId()).isPresent());
        assertEquals("Test", repository.findById(note.getId())
                .orElseThrow().getTitle());
    }

    @Test
    @DisplayName("description mentions creation")
    void description_mentionsCreation() {
        Note note = Note.create("My Note", "content");
        CreateNoteCommand cmd = new CreateNoteCommand(repository, note);

        assertEquals("Create note 'My Note'", cmd.description());
    }
}
