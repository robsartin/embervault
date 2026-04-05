package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.MoveNoteUseCase;
import com.embervault.domain.Note;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoveNoteCommandTest {

    private MoveNoteUseCase moveUseCase;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        NoteServiceImpl service = new NoteServiceImpl(repository);
        moveUseCase = service;
    }

    @Test
    @DisplayName("undo moves note back to original parent")
    void undo_movesBackToOriginalParent() {
        Note parent1 = Note.create("Parent1", "");
        Note parent2 = Note.create("Parent2", "");
        Note child = Note.create("Child", "");
        repository.save(parent1);
        repository.save(parent2);
        repository.save(child);

        moveUseCase.moveNote(child.getId(), parent1.getId());

        MoveNoteCommand cmd = new MoveNoteCommand(
                moveUseCase, child.getId(),
                parent1.getId(), parent2.getId());
        cmd.redo();

        assertEquals(parent2.getId().toString(),
                containerOf(child.getId()));

        cmd.undo();

        assertEquals(parent1.getId().toString(),
                containerOf(child.getId()));
    }

    @Test
    @DisplayName("description mentions move")
    void description_mentionsMove() {
        MoveNoteCommand cmd = new MoveNoteCommand(
                moveUseCase, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID());

        assertEquals("Move note", cmd.description());
    }

    private String containerOf(UUID noteId) {
        return repository.findById(noteId)
                .orElseThrow()
                .getAttribute(com.embervault.domain.Attributes.CONTAINER)
                .map(v -> ((com.embervault.domain.AttributeValue.StringValue) v)
                        .value())
                .orElse("");
    }
}
