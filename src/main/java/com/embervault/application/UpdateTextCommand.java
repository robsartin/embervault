package com.embervault.application;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.UpdateNoteTextUseCase;

/**
 * Reversible command for updating a note's text content.
 *
 * <p>Captures the old and new text so that undo restores the previous
 * content and redo re-applies the new content.</p>
 */
public final class UpdateTextCommand implements Reversible {

    private final UpdateNoteTextUseCase updateTextUseCase;
    private final UUID noteId;
    private final String oldText;
    private final String newText;

    /**
     * Creates a text update command.
     *
     * @param updateTextUseCase the use case for updating text
     * @param noteId            the note to update
     * @param oldText           the text before the update
     * @param newText           the text after the update
     */
    public UpdateTextCommand(UpdateNoteTextUseCase updateTextUseCase,
            UUID noteId, String oldText, String newText) {
        this.updateTextUseCase = Objects.requireNonNull(updateTextUseCase);
        this.noteId = Objects.requireNonNull(noteId);
        this.oldText = Objects.requireNonNull(oldText);
        this.newText = Objects.requireNonNull(newText);
    }

    @Override
    public void undo() {
        updateTextUseCase.updateNoteText(noteId, oldText);
    }

    @Override
    public void redo() {
        updateTextUseCase.updateNoteText(noteId, newText);
    }

    @Override
    public String description() {
        return "Update note text";
    }
}
