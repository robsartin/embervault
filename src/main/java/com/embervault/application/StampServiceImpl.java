package com.embervault.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.in.StampService;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.application.port.out.StampRepository;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;
import com.embervault.domain.StampAction;

/**
 * Application service implementing stamp use cases.
 *
 * <p>Delegates persistence to the {@link StampRepository} and {@link NoteRepository}
 * outbound ports.</p>
 */
public final class StampServiceImpl implements StampService {

    private final StampRepository stampRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructs a StampServiceImpl backed by the given repositories.
     *
     * @param stampRepository the stamp repository
     * @param noteRepository  the note repository
     */
    public StampServiceImpl(StampRepository stampRepository,
            NoteRepository noteRepository) {
        this.stampRepository = Objects.requireNonNull(stampRepository,
                "stampRepository must not be null");
        this.noteRepository = Objects.requireNonNull(noteRepository,
                "noteRepository must not be null");
    }

    @Override
    public Stamp createStamp(String name, String action) {
        Stamp stamp = Stamp.create(name, action);
        return stampRepository.save(stamp);
    }

    @Override
    public void deleteStamp(UUID id) {
        stampRepository.delete(id);
    }

    @Override
    public List<Stamp> getAllStamps() {
        return stampRepository.findAll();
    }

    @Override
    public Optional<Stamp> getStamp(UUID id) {
        return stampRepository.findById(id);
    }

    @Override
    public void applyStamp(UUID stampId, UUID noteId) {
        Stamp stamp = stampRepository.findById(stampId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Stamp not found: " + stampId));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + noteId));

        StampAction.ParsedAction parsed = StampAction.parse(stamp.action());
        note.setAttribute(parsed.attributeName(), parsed.value());
        noteRepository.save(note);
    }
}
