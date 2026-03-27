package com.embervault.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;

/**
 * Application service implementing note use cases.
 *
 * <p>Delegates persistence to the {@link NoteRepository} outbound port.</p>
 */
public final class NoteServiceImpl implements NoteService {

    private static final double MAX_XPOS = 12.0;
    private static final double MAX_YPOS = 8.0;

    private final NoteRepository repository;
    private final Random random;

    /**
     * Constructs a NoteServiceImpl backed by the given repository.
     */
    public NoteServiceImpl(NoteRepository repository) {
        this(repository, new Random());
    }

    /**
     * Constructs a NoteServiceImpl backed by the given repository and random source.
     *
     * @param repository the repository
     * @param random     the random source for position generation
     */
    public NoteServiceImpl(NoteRepository repository, Random random) {
        this.repository = Objects.requireNonNull(repository,
                "repository must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
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
    public Note createChildNote(UUID parentId, String title) {
        repository.findById(parentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Parent note not found: " + parentId));

        int nextOrder = repository.findChildren(parentId).size();

        Note child = Note.create(title, "");
        child.setAttribute("$Container",
                new AttributeValue.StringValue(parentId.toString()));
        child.setAttribute("$OutlineOrder",
                new AttributeValue.NumberValue(nextOrder));
        child.setAttribute("$Xpos",
                new AttributeValue.NumberValue(random.nextDouble() * MAX_XPOS));
        child.setAttribute("$Ypos",
                new AttributeValue.NumberValue(random.nextDouble() * MAX_YPOS));

        return repository.save(child);
    }

    @Override
    public Note renameNote(UUID noteId, String newTitle) {
        Objects.requireNonNull(newTitle, "newTitle must not be null");
        if (newTitle.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        Note note = repository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + noteId));
        note.setAttribute("$Name", new AttributeValue.StringValue(newTitle));
        return repository.save(note);
    }

    @Override
    public List<Note> getChildren(UUID parentId) {
        return repository.findChildren(parentId);
    }

    @Override
    public boolean hasChildren(UUID noteId) {
        return !repository.findChildren(noteId).isEmpty();
    }

    @Override
    public Note moveNote(UUID noteId, UUID newParentId) {
        Note note = repository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + noteId));
        repository.findById(newParentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Parent note not found: " + newParentId));

        int nextOrder = repository.findChildren(newParentId).size();
        note.setAttribute("$Container",
                new AttributeValue.StringValue(newParentId.toString()));
        note.setAttribute("$OutlineOrder",
                new AttributeValue.NumberValue(nextOrder));

        return repository.save(note);
    }

    @Override
    public void deleteNote(UUID id) {
        repository.delete(id);
    }
}
