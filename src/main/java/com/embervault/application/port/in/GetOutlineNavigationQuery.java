package com.embervault.application.port.in;

import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Query interface for outline navigation operations.
 */
public interface GetOutlineNavigationQuery {

  /**
   * Returns the previous note in outline order for the given note.
   *
   * <p>The previous note is the sibling with the next-lower {@code $OutlineOrder}
   * within the same {@code $Container}. If no previous sibling exists, the parent
   * note is returned. If there is no parent, {@code Optional.empty()} is returned.</p>
   *
   * @param noteId the note id
   * @return the previous note, or empty if none
   */
  Optional<Note> getPreviousInOutline(UUID noteId);
}
