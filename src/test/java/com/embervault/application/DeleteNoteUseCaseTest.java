package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.DeleteNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteNoteUseCaseTest {

  private DeleteNoteUseCase deleteUseCase;
  private CreateNoteUseCase creator;
  private GetNoteQuery query;

  @BeforeEach
  void setUp() {
    InMemoryNoteRepository repository = new InMemoryNoteRepository();
    NoteServiceImpl service = new NoteServiceImpl(repository);
    deleteUseCase = service;
    creator = service;
    query = service;
  }

  @Test
  @DisplayName("NoteServiceImpl implements DeleteNoteUseCase")
  void noteServiceImpl_shouldImplementDeleteNoteUseCase() {
    assertTrue(deleteUseCase instanceof DeleteNoteUseCase);
  }

  @Test
  @DisplayName("deleteNote() removes the note")
  void deleteNote_shouldRemoveNote() {
    Note note = creator.createNote("Title", "Content");

    deleteUseCase.deleteNote(note.getId());

    assertTrue(query.getNote(note.getId()).isEmpty());
  }

  @Test
  @DisplayName("deleteNoteIfLeaf() deletes leaf note and returns true")
  void deleteNoteIfLeaf_shouldDeleteLeaf() {
    Note parent = creator.createNote("Parent", "");
    Note child = creator.createChildNote(parent.getId(), "Child");

    boolean result = deleteUseCase.deleteNoteIfLeaf(child.getId());

    assertTrue(result);
    assertTrue(query.getNote(child.getId()).isEmpty());
  }

  @Test
  @DisplayName("deleteNoteIfLeaf() does not delete note with children")
  void deleteNoteIfLeaf_shouldNotDeleteParent() {
    Note parent = creator.createNote("Parent", "");
    Note child = creator.createChildNote(parent.getId(), "Child");
    creator.createChildNote(child.getId(), "Grandchild");

    boolean result = deleteUseCase.deleteNoteIfLeaf(child.getId());

    assertFalse(result);
    assertTrue(query.getNote(child.getId()).isPresent());
  }

  @Test
  @DisplayName("deleteNoteIfLeaf() returns false for missing note")
  void deleteNoteIfLeaf_shouldReturnFalseForMissing() {
    assertFalse(deleteUseCase.deleteNoteIfLeaf(UUID.randomUUID()));
  }
}
