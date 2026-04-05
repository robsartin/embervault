package com.embervault.application;

import java.util.Objects;

import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Note;

/**
 * Reversible command for note creation.
 *
 * <p>Undo deletes the created note; redo re-saves it with the same
 * identity and attributes.</p>
 */
public final class CreateNoteCommand implements Reversible {

    private final NoteRepository repository;
    private final Note note;

    /**
     * Creates a command for a newly created note.
     *
     * @param repository the note repository
     * @param note       the created note (must already be saved)
     */
    public CreateNoteCommand(NoteRepository repository, Note note) {
        this.repository = Objects.requireNonNull(repository);
        this.note = Objects.requireNonNull(note);
    }

    @Override
    public void undo() {
        repository.delete(note.getId());
    }

    @Override
    public void redo() {
        repository.save(note);
    }

    @Override
    public String description() {
        return "Create note '" + note.getTitle() + "'";
    }
}
