package com.embervault.application.port.in;

import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Use case for renaming a note.
 */
public interface RenameNoteUseCase {

    /**
     * Renames the note with the given id.
     *
     * @param noteId   the note id
     * @param newTitle the new title (must not be blank)
     * @return the updated note
     */
    Note renameNote(UUID noteId, String newTitle);
}
