package com.embervault.application.port.in;

import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Use case for creating notes.
 *
 * <p>Supports creating top-level notes, child notes under a parent,
 * and sibling notes adjacent to an existing note.</p>
 */
public interface CreateNoteUseCase {

  /**
   * Creates a new note with the given title and content.
   *
   * @param title   the note title
   * @param content the note content
   * @return the created note
   */
  Note createNote(String title, String content);

  /**
   * Creates a new child note under the given parent.
   *
   * @param parentId the parent note id
   * @param title    the title for the new note
   * @return the newly created child note
   */
  Note createChildNote(UUID parentId, String title);

  /**
   * Creates a new sibling note directly after the given sibling in outline order.
   *
   * @param siblingId the id of the note to create a sibling after
   * @param title     the title for the new note (may be empty for rapid entry)
   * @return the newly created sibling note
   */
  Note createSiblingNote(UUID siblingId, String title);
}
