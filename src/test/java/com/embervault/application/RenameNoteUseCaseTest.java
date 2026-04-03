package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.RenameNoteUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RenameNoteUseCaseTest {

  private RenameNoteUseCase renameUseCase;
  private CreateNoteUseCase creator;

  @BeforeEach
  void setUp() {
    InMemoryNoteRepository repository = new InMemoryNoteRepository();
    NoteServiceImpl service = new NoteServiceImpl(repository);
    renameUseCase = service;
    creator = service;
  }

  @Test
  @DisplayName("NoteServiceImpl implements RenameNoteUseCase")
  void noteServiceImpl_shouldImplementRenameNoteUseCase() {
    assertTrue(renameUseCase instanceof RenameNoteUseCase);
  }

  @Test
  @DisplayName("renameNote() updates the note title")
  void renameNote_shouldUpdateTitle() {
    Note note = creator.createNote("Old Title", "Content");

    Note renamed = renameUseCase.renameNote(note.getId(), "New Title");

    assertEquals("New Title", renamed.getTitle());
  }

  @Test
  @DisplayName("renameNote() throws for missing note")
  void renameNote_shouldThrowForMissing() {
    assertThrows(NoSuchElementException.class,
        () -> renameUseCase.renameNote(UUID.randomUUID(), "Title"));
  }

  @Test
  @DisplayName("renameNote() rejects blank title")
  void renameNote_shouldRejectBlankTitle() {
    Note note = creator.createNote("Title", "Content");

    assertThrows(IllegalArgumentException.class,
        () -> renameUseCase.renameNote(note.getId(), "   "));
  }
}
