package com.embervault.application;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.Command;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.RenameNoteUseCase;

/**
 * Command that renames a note, capturing the previous title for undo.
 */
public final class RenameNoteCommand implements Command {

    private final RenameNoteUseCase renameUseCase;
    private final GetNoteQuery getNoteQuery;
    private final UUID noteId;
    private final String newTitle;
    private String previousTitle;

    /**
     * Creates a rename command.
     *
     * @param renameUseCase the use case for renaming
     * @param getNoteQuery  the query for reading the current title
     * @param noteId        the note to rename
     * @param newTitle      the new title
     */
    public RenameNoteCommand(RenameNoteUseCase renameUseCase,
            GetNoteQuery getNoteQuery, UUID noteId, String newTitle) {
        this.renameUseCase = Objects.requireNonNull(renameUseCase);
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery);
        this.noteId = Objects.requireNonNull(noteId);
        this.newTitle = Objects.requireNonNull(newTitle);
    }

    @Override
    public void execute() {
        previousTitle = getNoteQuery.getNote(noteId)
                .orElseThrow()
                .getTitle();
        renameUseCase.renameNote(noteId, newTitle);
    }

    @Override
    public void undo() {
        String restoreTitle = (previousTitle == null
                || previousTitle.isBlank())
                ? "Untitled" : previousTitle;
        renameUseCase.renameNote(noteId, restoreTitle);
    }

    @Override
    public String description() {
        return "Rename note to '" + newTitle + "'";
    }
}
