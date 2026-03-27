package com.embervault.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Inbound port defining the note management use cases.
 */
public interface NoteService {

    /**
     * Creates a new note with the given title and content.
     */
    Note createNote(String title, String content);

    /**
     * Retrieves a note by its id.
     */
    Optional<Note> getNote(UUID id);

    /**
     * Retrieves all notes.
     */
    List<Note> getAllNotes();

    /**
     * Updates an existing note's title and content.
     */
    Note updateNote(UUID id, String title, String content);

    /**
     * Creates a new child note under the given parent.
     *
     * @param parentId the parent note id
     * @param title    the title for the new note
     * @return the newly created child note
     */
    Note createChildNote(UUID parentId, String title);

    /**
     * Returns the children of the note with the given id.
     *
     * @param parentId the parent note id
     * @return the list of child notes
     */
    List<Note> getChildren(UUID parentId);

    /**
     * Deletes the note with the given id.
     */
    void deleteNote(UUID id);
}
