package com.embervault.application.port.in;

import java.util.UUID;

/**
 * Use case for deleting notes.
 *
 * <p>Supports unconditional deletion and conditional leaf-only deletion.</p>
 */
public interface DeleteNoteUseCase {

  /**
   * Deletes the note with the given id.
   *
   * @param id the note id
   */
  void deleteNote(UUID id);

  /**
   * Deletes the note only if it is a leaf (has no children).
   *
   * @param noteId the note id
   * @return true if the note was deleted, false if it has children or does not exist
   */
  boolean deleteNoteIfLeaf(UUID noteId);
}
