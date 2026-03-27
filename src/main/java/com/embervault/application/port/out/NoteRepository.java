package com.embervault.application.port.out;

import java.util.List;
import java.util.Optional;
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
     * Deletes the note with the given id.
     */
    void delete(UUID id);
}
