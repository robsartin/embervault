package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateNoteTextUseCaseTest {

    private NoteService noteService;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
    }

    @Test
    @DisplayName("updateNoteText persists new text content")
    void updateNoteText_shouldPersistNewText() {
        Note note = noteService.createNote("Title", "Old text");

        noteService.updateNoteText(note.getId(), "New text");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        assertEquals("New text", reloaded.getContent());
    }

    @Test
    @DisplayName("updateNoteText preserves existing title")
    void updateNoteText_shouldPreserveTitle() {
        Note note = noteService.createNote("My Title", "Old text");

        noteService.updateNoteText(note.getId(), "New text");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        assertEquals("My Title", reloaded.getTitle());
    }

    @Test
    @DisplayName("updateNoteText throws for unknown note id")
    void updateNoteText_shouldThrowForUnknownId() {
        assertThrows(NoSuchElementException.class,
                () -> noteService.updateNoteText(
                        UUID.randomUUID(), "text"));
    }

    @Test
    @DisplayName("updateNoteText throws for null text")
    void updateNoteText_shouldThrowForNullText() {
        Note note = noteService.createNote("Title", "Content");
        assertThrows(NullPointerException.class,
                () -> noteService.updateNoteText(
                        note.getId(), null));
    }
}
