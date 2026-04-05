package com.embervault.application;

import java.util.Objects;

import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Note;

/**
 * Reversible command for note deletion.
 *
 * <p>Captures the full note before deletion so that undo can re-save
 * it with the same identity and attributes.</p>
 */
public final class DeleteNoteCommand implements Reversible {

    private final NoteRepository repository;
    private final Note note;

    /**
     * Creates a command for a note that is about to be deleted.
     *
     * @param repository the note repository
     * @param note       the note snapshot (before deletion)
     */
    public DeleteNoteCommand(NoteRepository repository, Note note) {
        this.repository = Objects.requireNonNull(repository);
        this.note = Objects.requireNonNull(note);
    }

    @Override
    public void undo() {
        repository.save(note);
    }

    @Override
    public void redo() {
        repository.delete(note.getId());
    }

    @Override
    public String description() {
        return "Delete note '" + note.getTitle() + "'";
    }
}
