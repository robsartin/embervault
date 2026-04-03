package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.MoveNoteUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoveNoteCommandTest {

    private NoteServiceImpl service;
    private MoveNoteUseCase moveUseCase;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
        moveUseCase = service;
    }

    @Test
    @DisplayName("execute() moves note to new parent")
    void execute_shouldMoveNoteToNewParent() {
        Note parent1 = service.createNote("Parent1", "");
        Note parent2 = service.createNote("Parent2", "");
        Note child = service.createChildNote(parent1.getId(), "Child");

        MoveNoteCommand command = new MoveNoteCommand(
                moveUseCase, service, child.getId(), parent2.getId());
        command.execute();

        List<Note> parent2Children = service.getChildren(parent2.getId());
        assertEquals(1, parent2Children.size());
        assertEquals("Child", parent2Children.get(0).getTitle());
    }

    @Test
    @DisplayName("undo() restores note to original parent")
    void undo_shouldRestoreNoteToOriginalParent() {
        Note parent1 = service.createNote("Parent1", "");
        Note parent2 = service.createNote("Parent2", "");
        Note child = service.createChildNote(parent1.getId(), "Child");

        MoveNoteCommand command = new MoveNoteCommand(
                moveUseCase, service, child.getId(), parent2.getId());
        command.execute();
        command.undo();

        List<Note> parent1Children = service.getChildren(parent1.getId());
        assertEquals(1, parent1Children.size());
        assertEquals("Child", parent1Children.get(0).getTitle());

        List<Note> parent2Children = service.getChildren(parent2.getId());
        assertEquals(0, parent2Children.size());
    }

    @Test
    @DisplayName("description() returns meaningful text")
    void description_shouldReturnMeaningfulText() {
        Note parent1 = service.createNote("Parent1", "");
        Note parent2 = service.createNote("Parent2", "");
        Note child = service.createChildNote(parent1.getId(), "Child");

        MoveNoteCommand command = new MoveNoteCommand(
                moveUseCase, service, child.getId(), parent2.getId());

        assertEquals("Move note", command.description());
    }
}
