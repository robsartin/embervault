package com.embervault.application.port.in;

import java.util.UUID;

/**
 * Use case for updating a note's text content.
 */
public interface UpdateNoteTextUseCase {

    /**
     * Updates the text content of the note with the given id.
     *
     * @param noteId  the note id
     * @param newText the new text content
     */
    void updateNoteText(UUID noteId, String newText);
}
