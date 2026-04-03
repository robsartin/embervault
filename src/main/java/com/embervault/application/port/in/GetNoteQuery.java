package com.embervault.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Query interface for reading notes and their relationships.
 *
 * <p>Provides read-only access to notes, their children, and
 * batch queries for children existence checks.</p>
 */
public interface GetNoteQuery {

  /**
   * Retrieves a note by its id.
   *
   * @param id the note id
   * @return the note, or empty if not found
   */
  Optional<Note> getNote(UUID id);

  /**
   * Retrieves all notes.
   *
   * @return all notes
   */
  List<Note> getAllNotes();

  /**
   * Returns the children of the note with the given id, ordered by $OutlineOrder.
   *
   * @param parentId the parent note id
   * @return the list of child notes
   */
  List<Note> getChildren(UUID parentId);

  /**
   * Returns whether the note with the given id has any children.
   *
   * @param noteId the note id
   * @return true if the note has children
   */
  boolean hasChildren(UUID noteId);

  /**
   * Batch-checks which of the given note ids have children.
   *
   * @param noteIds the note ids to check
   * @return a map from each input id to whether it has children
   */
  Map<UUID, Boolean> hasChildrenBatch(Collection<UUID> noteIds);
}
