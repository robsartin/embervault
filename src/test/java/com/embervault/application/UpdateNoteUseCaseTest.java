package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.UpdateNoteUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateNoteUseCaseTest {

    private UpdateNoteUseCase updateUseCase;
    private CreateNoteUseCase creator;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        NoteServiceImpl service = new NoteServiceImpl(repository);
        updateUseCase = service;
        creator = service;
    }

    @Test
    @DisplayName("NoteServiceImpl implements UpdateNoteUseCase")
    void noteServiceImpl_shouldImplementUpdateNoteUseCase() {
        assertTrue(updateUseCase instanceof UpdateNoteUseCase);
    }

    @Test
    @DisplayName("updateNote() updates title and content")
    void updateNote_shouldModifyNote() {
        Note created = creator.createNote("Old", "Old");

        Note updated = updateUseCase.updateNote(
                created.getId(), "New", "New");

        assertEquals("New", updated.getTitle());
        assertEquals("New", updated.getContent());
    }

    @Test
    @DisplayName("updateNote() throws for missing note")
    void updateNote_shouldThrowForMissing() {
        assertThrows(NoSuchElementException.class,
                () -> updateUseCase.updateNote(
                        UUID.randomUUID(), "T", "C"));
    }
}
