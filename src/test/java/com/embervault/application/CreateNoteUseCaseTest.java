package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateNoteUseCaseTest {

    private CreateNoteUseCase useCase;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        useCase = new NoteServiceImpl(repository);
    }

    @Test
    @DisplayName("NoteServiceImpl implements CreateNoteUseCase")
    void noteServiceImpl_shouldImplementCreateNoteUseCase() {
        assertNotNull(useCase);
        assertTrue(useCase instanceof CreateNoteUseCase);
    }

    @Test
    @DisplayName("createNote() via CreateNoteUseCase returns a persisted note")
    void createNote_shouldPersistAndReturn() {
        Note note = useCase.createNote("Title", "Content");

        assertNotNull(note);
        assertEquals("Title", note.getTitle());
        assertTrue(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("createChildNote() via CreateNoteUseCase creates a child")
    void createChildNote_shouldCreateChild() {
        Note parent = useCase.createNote("Parent", "");

        Note child = useCase.createChildNote(parent.getId(), "Child");

        assertNotNull(child);
        assertEquals("Child", child.getTitle());
    }

    @Test
    @DisplayName("createSiblingNote() via CreateNoteUseCase creates a sibling")
    void createSiblingNote_shouldCreateSibling() {
        Note parent = useCase.createNote("Parent", "");
        Note child = useCase.createChildNote(parent.getId(), "First");

        Note sibling = useCase.createSiblingNote(child.getId(), "Second");

        assertNotNull(sibling);
        assertEquals("Second", sibling.getTitle());
    }
}
