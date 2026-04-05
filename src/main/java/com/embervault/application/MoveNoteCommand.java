package com.embervault.application;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.MoveNoteUseCase;

/**
 * Reversible command for moving a note to a different parent.
 *
 * <p>Captures the old and new parent ids so that undo moves the note
 * back to its original parent.</p>
 */
public final class MoveNoteCommand implements Reversible {

    private final MoveNoteUseCase moveUseCase;
    private final UUID noteId;
    private final UUID oldParentId;
    private final UUID newParentId;

    /**
     * Creates a move command.
     *
     * @param moveUseCase the use case for moving notes
     * @param noteId      the note to move
     * @param oldParentId the parent before the move
     * @param newParentId the parent after the move
     */
    public MoveNoteCommand(MoveNoteUseCase moveUseCase, UUID noteId,
            UUID oldParentId, UUID newParentId) {
        this.moveUseCase = Objects.requireNonNull(moveUseCase);
        this.noteId = Objects.requireNonNull(noteId);
        this.oldParentId = Objects.requireNonNull(oldParentId);
        this.newParentId = Objects.requireNonNull(newParentId);
    }

    @Override
    public void undo() {
        moveUseCase.moveNote(noteId, oldParentId);
    }

    @Override
    public void redo() {
        moveUseCase.moveNote(noteId, newParentId);
    }

    @Override
    public String description() {
        return "Move note";
    }
}
