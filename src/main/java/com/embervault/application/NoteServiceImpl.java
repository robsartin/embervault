package com.embervault.application;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeMap;
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
    public Note createSiblingNote(UUID siblingId, String title) {
        Note sibling = repository.findById(siblingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Sibling note not found: " + siblingId));

        String containerId = sibling.getAttribute("$Container")
                .filter(v -> v instanceof AttributeValue.StringValue)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElseThrow(() -> new NoSuchElementException(
                        "Sibling has no $Container: " + siblingId));

        double siblingOrder = sibling.getAttribute("$OutlineOrder")
                .filter(v -> v instanceof AttributeValue.NumberValue)
                .map(v -> ((AttributeValue.NumberValue) v).value())
                .orElse(0.0);

        UUID parentId = UUID.fromString(containerId);

        // Bump order of all siblings after this one
        List<Note> siblings = repository.findChildren(parentId);
        for (Note s : siblings) {
            double order = s.getAttribute("$OutlineOrder")
                    .filter(v -> v instanceof AttributeValue.NumberValue)
                    .map(v -> ((AttributeValue.NumberValue) v).value())
                    .orElse(0.0);
            if (order > siblingOrder) {
                s.setAttribute("$OutlineOrder",
                        new AttributeValue.NumberValue(order + 1));
                repository.save(s);
            }
        }

        // Create the new note (use AttributeMap constructor to allow empty titles)
        Note newNote = createNoteWithTitle(title);
        newNote.setAttribute("$Container",
                new AttributeValue.StringValue(containerId));
        newNote.setAttribute("$OutlineOrder",
                new AttributeValue.NumberValue(siblingOrder + 1));
        newNote.setAttribute("$Xpos",
                new AttributeValue.NumberValue(random.nextDouble() * MAX_XPOS));
        newNote.setAttribute("$Ypos",
                new AttributeValue.NumberValue(random.nextDouble() * MAX_YPOS));

        return repository.save(newNote);
    }

    @Override
    public Note indentNote(UUID noteId) {
        Note note = repository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + noteId));

        String containerId = note.getAttribute("$Container")
                .filter(v -> v instanceof AttributeValue.StringValue)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse(null);

        if (containerId == null) {
            return note;
        }

        UUID parentId = UUID.fromString(containerId);
        double noteOrder = note.getAttribute("$OutlineOrder")
                .filter(v -> v instanceof AttributeValue.NumberValue)
                .map(v -> ((AttributeValue.NumberValue) v).value())
                .orElse(0.0);

        // Find the note directly above (sibling with the next-lower order)
        List<Note> siblings = repository.findChildren(parentId);
        Note noteAbove = null;
        double bestOrder = -1;
        for (Note s : siblings) {
            if (s.getId().equals(noteId)) {
                continue;
            }
            double order = s.getAttribute("$OutlineOrder")
                    .filter(v -> v instanceof AttributeValue.NumberValue)
                    .map(v -> ((AttributeValue.NumberValue) v).value())
                    .orElse(0.0);
            if (order < noteOrder && order > bestOrder) {
                bestOrder = order;
                noteAbove = s;
            }
        }

        if (noteAbove == null) {
            return note;
        }

        // Move note to be a child of noteAbove
        int newOrder = repository.findChildren(noteAbove.getId()).size();
        note.setAttribute("$Container",
                new AttributeValue.StringValue(noteAbove.getId().toString()));
        note.setAttribute("$OutlineOrder",
                new AttributeValue.NumberValue(newOrder));

        return repository.save(note);
    }

    @Override
    public Note outdentNote(UUID noteId) {
        Note note = repository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + noteId));

        String containerId = note.getAttribute("$Container")
                .filter(v -> v instanceof AttributeValue.StringValue)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse(null);

        if (containerId == null) {
            return note;
        }

        UUID parentId = UUID.fromString(containerId);
        Note parent = repository.findById(parentId).orElse(null);
        if (parent == null) {
            return note;
        }

        String grandparentContainerId = parent.getAttribute("$Container")
                .filter(v -> v instanceof AttributeValue.StringValue)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse(null);

        if (grandparentContainerId == null) {
            return note;
        }

        UUID grandparentId = UUID.fromString(grandparentContainerId);

        // Find parent's order in grandparent's children
        double parentOrder = parent.getAttribute("$OutlineOrder")
                .filter(v -> v instanceof AttributeValue.NumberValue)
                .map(v -> ((AttributeValue.NumberValue) v).value())
                .orElse(0.0);

        // Bump order of grandparent's children that come after parent
        List<Note> grandparentChildren = repository.findChildren(grandparentId);
        for (Note gc : grandparentChildren) {
            double order = gc.getAttribute("$OutlineOrder")
                    .filter(v -> v instanceof AttributeValue.NumberValue)
                    .map(v -> ((AttributeValue.NumberValue) v).value())
                    .orElse(0.0);
            if (order > parentOrder) {
                gc.setAttribute("$OutlineOrder",
                        new AttributeValue.NumberValue(order + 1));
                repository.save(gc);
            }
        }

        // Move note to grandparent, positioned just after parent
        note.setAttribute("$Container",
                new AttributeValue.StringValue(grandparentContainerId));
        note.setAttribute("$OutlineOrder",
                new AttributeValue.NumberValue(parentOrder + 1));

        return repository.save(note);
    }

    @Override
    public void deleteNote(UUID id) {
        repository.delete(id);
    }

    @Override
    public Optional<Note> getPreviousInOutline(UUID noteId) {
        Note note = repository.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note not found: " + noteId));

        String containerId = note.getAttribute("$Container")
                .filter(v -> v instanceof AttributeValue.StringValue)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse(null);

        if (containerId == null) {
            return Optional.empty();
        }

        UUID parentId = UUID.fromString(containerId);
        double noteOrder = note.getAttribute("$OutlineOrder")
                .filter(v -> v instanceof AttributeValue.NumberValue)
                .map(v -> ((AttributeValue.NumberValue) v).value())
                .orElse(0.0);

        // Find the sibling with the next-lower outline order
        List<Note> siblings = repository.findChildren(parentId);
        Note previousSibling = null;
        double bestOrder = Double.NEGATIVE_INFINITY;
        for (Note s : siblings) {
            if (s.getId().equals(noteId)) {
                continue;
            }
            double order = s.getAttribute("$OutlineOrder")
                    .filter(v -> v instanceof AttributeValue.NumberValue)
                    .map(v -> ((AttributeValue.NumberValue) v).value())
                    .orElse(0.0);
            if (order < noteOrder && order > bestOrder) {
                bestOrder = order;
                previousSibling = s;
            }
        }

        if (previousSibling != null) {
            return Optional.of(previousSibling);
        }

        // No previous sibling — return the parent note
        return repository.findById(parentId);
    }

    @Override
    public boolean deleteNoteIfLeaf(UUID noteId) {
        Optional<Note> noteOpt = repository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return false;
        }
        if (hasChildren(noteId)) {
            return false;
        }
        repository.delete(noteId);
        return true;
    }

    /**
     * Creates a note allowing an empty title by using the AttributeMap constructor.
     */
    private Note createNoteWithTitle(String title) {
        if (title != null && !title.isBlank()) {
            return Note.create(title, "");
        }
        Instant now = Instant.now();
        AttributeMap attrs = new AttributeMap();
        attrs.set("$Name", new AttributeValue.StringValue(
                title == null ? "" : title));
        attrs.set("$Text", new AttributeValue.StringValue(""));
        attrs.set("$Created", new AttributeValue.DateValue(now));
        attrs.set("$Modified", new AttributeValue.DateValue(now));
        return new Note(UUID.randomUUID(), attrs);
    }
}
