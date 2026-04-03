package com.embervault.application;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.Command;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.MoveNoteUseCase;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;

/**
 * Command that moves a note to a new parent, capturing the previous
 * parent for undo.
 */
public final class MoveNoteCommand implements Command {

    private final MoveNoteUseCase moveUseCase;
    private final GetNoteQuery getNoteQuery;
    private final UUID noteId;
    private final UUID newParentId;
    private UUID previousParentId;

    /**
     * Creates a move command.
     *
     * @param moveUseCase  the use case for moving notes
     * @param getNoteQuery the query for reading the current parent
     * @param noteId       the note to move
     * @param newParentId  the target parent
     */
    public MoveNoteCommand(MoveNoteUseCase moveUseCase,
            GetNoteQuery getNoteQuery, UUID noteId, UUID newParentId) {
        this.moveUseCase = Objects.requireNonNull(moveUseCase);
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery);
        this.noteId = Objects.requireNonNull(noteId);
        this.newParentId = Objects.requireNonNull(newParentId);
    }

    @Override
    public void execute() {
        Note note = getNoteQuery.getNote(noteId).orElseThrow();
        note.getAttribute(Attributes.CONTAINER)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .map(UUID::fromString)
                .ifPresent(id -> previousParentId = id);
        moveUseCase.moveNote(noteId, newParentId);
    }

    @Override
    public void undo() {
        if (previousParentId != null) {
            moveUseCase.moveNote(noteId, previousParentId);
        }
    }

    @Override
    public String description() {
        return "Move note";
    }
}
