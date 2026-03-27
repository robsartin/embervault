package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteServiceImplTest {

    private NoteService service;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
    }

    @Test
    @DisplayName("createNote() returns a persisted note")
    void createNote_shouldPersistAndReturn() {
        Note note = service.createNote("Title", "Content");

        assertNotNull(note);
        assertNotNull(note.getId());
        assertEquals("Title", note.getTitle());
        assertTrue(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("getNote() returns the note when it exists")
    void getNote_shouldReturnExistingNote() {
        Note created = service.createNote("Title", "Content");

        Optional<Note> found = service.getNote(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created, found.get());
    }

    @Test
    @DisplayName("getNote() returns empty when note does not exist")
    void getNote_shouldReturnEmptyForMissing() {
        Optional<Note> found = service.getNote(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("getAllNotes() returns all created notes")
    void getAllNotes_shouldReturnAll() {
        service.createNote("A", "a");
        service.createNote("B", "b");

        List<Note> all = service.getAllNotes();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("updateNote() updates title and content")
    void updateNote_shouldModifyNote() {
        Note created = service.createNote("Old", "Old");

        Note updated = service.updateNote(created.getId(), "New", "New");

        assertEquals("New", updated.getTitle());
        assertEquals("New", updated.getContent());
    }

    @Test
    @DisplayName("updateNote() throws when note does not exist")
    void updateNote_shouldThrowForMissing() {
        assertThrows(NoSuchElementException.class,
                () -> service.updateNote(UUID.randomUUID(), "T", "C"));
    }

    @Test
    @DisplayName("deleteNote() removes the note")
    void deleteNote_shouldRemoveNote() {
        Note created = service.createNote("Title", "Content");

        service.deleteNote(created.getId());

        assertTrue(service.getNote(created.getId()).isEmpty());
    }
}
