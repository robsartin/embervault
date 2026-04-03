package com.embervault.application;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.Command;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.UpdateNoteTextUseCase;

/**
 * Command that updates a note's text content, capturing the previous
 * text for undo.
 */
public final class UpdateNoteTextCommand implements Command {

    private final UpdateNoteTextUseCase updateTextUseCase;
    private final GetNoteQuery getNoteQuery;
    private final UUID noteId;
    private final String newText;
    private String previousText;

    /**
     * Creates an update-text command.
     *
     * @param updateTextUseCase the use case for updating text
     * @param getNoteQuery      the query for reading the current text
     * @param noteId            the note to update
     * @param newText           the new text content
     */
    public UpdateNoteTextCommand(UpdateNoteTextUseCase updateTextUseCase,
            GetNoteQuery getNoteQuery, UUID noteId, String newText) {
        this.updateTextUseCase = Objects.requireNonNull(updateTextUseCase);
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery);
        this.noteId = Objects.requireNonNull(noteId);
        this.newText = Objects.requireNonNull(newText);
    }

    @Override
    public void execute() {
        previousText = getNoteQuery.getNote(noteId)
                .orElseThrow()
                .getText();
        updateTextUseCase.updateNoteText(noteId, newText);
    }

    @Override
    public void undo() {
        updateTextUseCase.updateNoteText(noteId, previousText);
    }

    @Override
    public String description() {
        return "Update note text";
    }
}
