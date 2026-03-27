package com.embervault.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Note;

/**
 * Application service implementing note use cases.
 *
 * <p>Delegates persistence to the {@link NoteRepository} outbound port.</p>
 */
public final class NoteServiceImpl implements NoteService {

    private final NoteRepository repository;

    /**
     * Constructs a NoteServiceImpl backed by the given repository.
     */
    public NoteServiceImpl(NoteRepository repository) {
        this.repository = Objects.requireNonNull(repository,
                "repository must not be null");
    }

    @Override
    public Note createNote(String title, String content) {
        Note note = Note.create(title, content);
        return repository.save(note);
    }

    @Override
    public Optional<Note> getNote(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Note> getAllNotes() {
        return repository.findAll();
    }

    @Override
    public Note updateNote(UUID id, String title, String content) {
        Note note = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + id));
        note.update(title, content);
        return repository.save(note);
    }

    @Override
    public void deleteNote(UUID id) {
        repository.delete(id);
    }
}
