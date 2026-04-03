package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetOutlineNavigationQuery;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetOutlineNavigationQueryTest {

  private GetOutlineNavigationQuery navQuery;
  private CreateNoteUseCase creator;

  @BeforeEach
  void setUp() {
    InMemoryNoteRepository repository = new InMemoryNoteRepository();
    NoteServiceImpl service = new NoteServiceImpl(repository);
    navQuery = service;
    creator = service;
  }

  @Test
  @DisplayName("NoteServiceImpl implements GetOutlineNavigationQuery")
  void noteServiceImpl_shouldImplementGetOutlineNavigationQuery() {
    assertTrue(navQuery instanceof GetOutlineNavigationQuery);
  }

  @Test
  @DisplayName("getPreviousInOutline() returns previous sibling")
  void getPreviousInOutline_shouldReturnPreviousSibling() {
    Note parent = creator.createNote("Parent", "");
    Note child1 = creator.createChildNote(parent.getId(), "Child1");
    Note child2 = creator.createChildNote(parent.getId(), "Child2");

    Optional<Note> previous = navQuery.getPreviousInOutline(
        child2.getId());

    assertTrue(previous.isPresent());
    assertEquals(child1.getId(), previous.get().getId());
  }

  @Test
  @DisplayName("getPreviousInOutline() returns parent when first child")
  void getPreviousInOutline_shouldReturnParent() {
    Note parent = creator.createNote("Parent", "");
    Note child = creator.createChildNote(parent.getId(), "Child");

    Optional<Note> previous = navQuery.getPreviousInOutline(
        child.getId());

    assertTrue(previous.isPresent());
    assertEquals(parent.getId(), previous.get().getId());
  }

  @Test
  @DisplayName("getPreviousInOutline() returns empty for root note")
  void getPreviousInOutline_shouldReturnEmptyForRoot() {
    Note root = creator.createNote("Root", "");

    assertTrue(navQuery.getPreviousInOutline(root.getId()).isEmpty());
  }

  @Test
  @DisplayName("getPreviousInOutline() throws for missing note")
  void getPreviousInOutline_shouldThrowForMissing() {
    assertThrows(NoSuchElementException.class,
        () -> navQuery.getPreviousInOutline(UUID.randomUUID()));
  }
}
