package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.MoveNoteUseCase;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoveNoteUseCaseTest {

  private MoveNoteUseCase moveUseCase;
  private CreateNoteUseCase creator;
  private GetNoteQuery query;

  @BeforeEach
  void setUp() {
    InMemoryNoteRepository repository = new InMemoryNoteRepository();
    NoteServiceImpl service = new NoteServiceImpl(repository);
    moveUseCase = service;
    creator = service;
    query = service;
  }

  @Test
  @DisplayName("NoteServiceImpl implements MoveNoteUseCase")
  void noteServiceImpl_shouldImplementMoveNoteUseCase() {
    assertTrue(moveUseCase instanceof MoveNoteUseCase);
  }

  @Test
  @DisplayName("moveNote() changes container to new parent")
  void moveNote_shouldChangeContainer() {
    Note parent1 = creator.createNote("Parent1", "");
    Note parent2 = creator.createNote("Parent2", "");
    Note child = creator.createChildNote(parent1.getId(), "Child");

    Note moved = moveUseCase.moveNote(child.getId(), parent2.getId());

    String container = ((AttributeValue.StringValue)
        moved.getAttribute("$Container").orElseThrow()).value();
    assertEquals(parent2.getId().toString(), container);
  }

  @Test
  @DisplayName("moveNoteToPosition() places note at correct position")
  void moveNoteToPosition_shouldPlaceCorrectly() {
    Note parent = creator.createNote("Parent", "");
    creator.createChildNote(parent.getId(), "A");
    creator.createChildNote(parent.getId(), "B");
    Note parent2 = creator.createNote("Parent2", "");
    Note child = creator.createChildNote(parent2.getId(), "C");

    moveUseCase.moveNoteToPosition(child.getId(), parent.getId(), 1);

    List<Note> children = query.getChildren(parent.getId());
    assertEquals(3, children.size());
    assertEquals("A", children.get(0).getTitle());
    assertEquals("C", children.get(1).getTitle());
    assertEquals("B", children.get(2).getTitle());
  }

  @Test
  @DisplayName("indentNote() makes note a child of the note above")
  void indentNote_shouldMoveUnderNoteAbove() {
    Note parent = creator.createNote("Parent", "");
    Note child1 = creator.createChildNote(parent.getId(), "Child1");
    Note child2 = creator.createChildNote(parent.getId(), "Child2");

    Note indented = moveUseCase.indentNote(child2.getId());

    String container = ((AttributeValue.StringValue)
        indented.getAttribute("$Container").orElseThrow()).value();
    assertEquals(child1.getId().toString(), container);
  }

  @Test
  @DisplayName("outdentNote() moves note to be sibling of parent")
  void outdentNote_shouldMoveToGrandparent() {
    Note root = creator.createNote("Root", "");
    Note parent = creator.createChildNote(root.getId(), "Parent");
    Note child = creator.createChildNote(parent.getId(), "Child");

    Note outdented = moveUseCase.outdentNote(child.getId());

    String container = ((AttributeValue.StringValue)
        outdented.getAttribute("$Container").orElseThrow()).value();
    assertEquals(root.getId().toString(), container);
  }

  @Test
  @DisplayName("moveNote() throws for missing note")
  void moveNote_shouldThrowForMissing() {
    Note parent = creator.createNote("Parent", "");

    assertThrows(NoSuchElementException.class,
        () -> moveUseCase.moveNote(UUID.randomUUID(), parent.getId()));
  }
}
