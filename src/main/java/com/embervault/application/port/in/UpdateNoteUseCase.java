package com.embervault.application.port.in;

import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Use case for updating a note's title and content.
 */
public interface UpdateNoteUseCase {

  /**
   * Updates an existing note's title and content.
   *
   * @param id      the note id
   * @param title   the new title
   * @param content the new content
   * @return the updated note
   */
  Note updateNote(UUID id, String title, String content);
}
