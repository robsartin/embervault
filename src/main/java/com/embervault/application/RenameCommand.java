package com.embervault.application;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.RenameNoteUseCase;

/**
 * Reversible command that renames a note.
 *
 * <p>Captures the old and new title so that undo restores the original
 * name and redo re-applies the new name.</p>
 */
public final class RenameCommand implements Reversible {

    private final RenameNoteUseCase renameUseCase;
    private final UUID noteId;
    private final String oldTitle;
    private final String newTitle;

    /**
     * Creates a rename command.
     *
     * @param renameUseCase the use case for renaming
     * @param noteId        the note to rename
     * @param oldTitle      the title before the rename
     * @param newTitle      the title after the rename
     */
    public RenameCommand(RenameNoteUseCase renameUseCase, UUID noteId,
            String oldTitle, String newTitle) {
        this.renameUseCase = Objects.requireNonNull(renameUseCase);
        this.noteId = Objects.requireNonNull(noteId);
        this.oldTitle = Objects.requireNonNull(oldTitle);
        this.newTitle = Objects.requireNonNull(newTitle);
    }

    @Override
    public void undo() {
        renameUseCase.renameNote(noteId, oldTitle);
    }

    @Override
    public void redo() {
        renameUseCase.renameNote(noteId, newTitle);
    }

    @Override
    public String description() {
        return "Rename '" + oldTitle + "' to '" + newTitle + "'";
    }
}
