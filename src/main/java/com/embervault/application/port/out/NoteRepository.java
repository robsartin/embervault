package com.embervault.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Outbound port for persisting and retrieving notes.
 */
public interface NoteRepository {

    /**
     * Saves the given note, creating or replacing it by id.
     */
    Note save(Note note);

    /**
     * Finds a note by its unique identifier.
     */
    Optional<Note> findById(UUID id);

    /**
     * Returns all notes.
     */
    List<Note> findAll();

    /**
     * Returns the children of the note with the given parent id.
     *
     * @param parentId the parent note id
     * @return the list of child notes
     */
    List<Note> findChildren(UUID parentId);

    /**
     * Returns the ids from the given collection that have at least one child note.
     *
     * <p>A child note is one whose {@code $Container} attribute references the
     * parent's id. This method performs a single scan over all notes, making it
     * O(M) where M is the total number of notes, regardless of how many ids are
     * queried.</p>
     *
     * @param noteIds the note ids to check
     * @return the subset of ids that have at least one child
     */
    Set<UUID> findNoteIdsWithChildren(Collection<UUID> noteIds);

    /**
     * Deletes the note with the given id.
     */
    void delete(UUID id);
}
