package com.embervault.application.port.in;

import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Use case for moving notes within the hierarchy.
 *
 * <p>Supports reparenting, positional moves, indentation, and outdentation.</p>
 */
public interface MoveNoteUseCase {

  /**
   * Moves a note to a new parent.
   *
   * @param noteId      the note to move
   * @param newParentId the new parent note id
   * @return the updated note
   */
  Note moveNote(UUID noteId, UUID newParentId);

  /**
   * Moves a note to a specific position within a parent.
   *
   * @param noteId      the note to move
   * @param newParentId the target parent note id
   * @param position    the zero-based position among siblings
   * @return the updated note
   */
  Note moveNoteToPosition(UUID noteId, UUID newParentId, int position);

  /**
   * Indents a note by making it a child of the note directly above it.
   *
   * @param noteId the id of the note to indent
   * @return the updated note
   */
  Note indentNote(UUID noteId);

  /**
   * Outdents a note by moving it to be a sibling of its current parent.
   *
   * @param noteId the id of the note to outdent
   * @return the updated note
   */
  Note outdentNote(UUID noteId);
}
