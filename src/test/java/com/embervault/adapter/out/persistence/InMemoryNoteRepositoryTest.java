package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryNoteRepositoryTest {

    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
    }

    @Test
    @DisplayName("save() stores a note that can be retrieved by id")
    void save_shouldStoreNote() {
        Note note = Note.create("Title", "Content");

        repository.save(note);

        Optional<Note> found = repository.findById(note.getId());
        assertTrue(found.isPresent());
        assertEquals(note, found.get());
    }

    @Test
    @DisplayName("findById() returns empty for unknown id")
    void findById_shouldReturnEmptyForUnknownId() {
        Optional<Note> found = repository.findById(UUID.randomUUID());

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAll() returns all saved notes")
    void findAll_shouldReturnAllNotes() {
        Note noteA = Note.create("A", "a");
        Note noteB = Note.create("B", "b");
        repository.save(noteA);
        repository.save(noteB);

        List<Note> all = repository.findAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(noteA));
        assertTrue(all.contains(noteB));
    }

    @Test
    @DisplayName("findAll() returns empty list when no notes exist")
    void findAll_shouldReturnEmptyListWhenEmpty() {
        List<Note> all = repository.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("delete() removes a note by id")
    void delete_shouldRemoveNote() {
        Note note = Note.create("Title", "Content");
        repository.save(note);

        repository.delete(note.getId());

        assertFalse(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("delete() is safe for unknown ids")
    void delete_shouldBeSafeForUnknownId() {
        repository.delete(UUID.randomUUID());
        // no exception expected
    }

    @Test
    @DisplayName("save() replaces an existing note with the same id")
    void save_shouldReplaceExistingNote() {
        Note note = Note.create("Old", "Old content");
        repository.save(note);

        note.update("New", "New content");
        repository.save(note);

        Note found = repository.findById(note.getId()).orElseThrow();
        assertEquals("New", found.getTitle());
        assertEquals("New content", found.getContent());
    }
}
