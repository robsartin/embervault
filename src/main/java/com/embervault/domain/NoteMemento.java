package com.embervault.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * An immutable snapshot of a note's state at a point in time.
 *
 * <p>Captures the note's id, attribute map, and prototype id so the
 * note can be restored to this exact state later (Memento pattern).</p>
 */
public final class NoteMemento {

    private final UUID noteId;
    private final AttributeMap attributeSnapshot;
    private final UUID prototypeId;

    private NoteMemento(UUID noteId, AttributeMap attributeSnapshot,
            UUID prototypeId) {
        this.noteId = Objects.requireNonNull(noteId);
        this.attributeSnapshot = Objects.requireNonNull(attributeSnapshot);
        this.prototypeId = prototypeId;
    }

    /**
     * Captures an immutable snapshot of the given note's current state.
     *
     * @param note the note to snapshot
     * @return a memento holding a copy of the note's attributes
     */
    public static NoteMemento capture(Note note) {
        return new NoteMemento(
                note.getId(),
                new AttributeMap(note.getAttributes()),
                note.getPrototypeId().orElse(null));
    }

    /**
     * Restores the given note to the state captured in this memento.
     *
     * <p>Replaces all attributes on the note with the snapshotted values.
     * The note's id is not changed.</p>
     *
     * @param note the note to restore (must have the same id)
     */
    public void restore(Note note) {
        if (!note.getId().equals(noteId)) {
            throw new IllegalArgumentException(
                    "Cannot restore memento for note " + noteId
                    + " onto note " + note.getId());
        }
        // Clear current attributes and replace with snapshot
        AttributeMap current = note.getAttributes();
        for (String key : current.localEntries().keySet().toArray(String[]::new)) {
            current.remove(key);
        }
        for (var entry : attributeSnapshot.localEntries().entrySet()) {
            current.set(entry.getKey(), entry.getValue());
        }
        note.setPrototypeId(prototypeId);
    }

    /** Returns the id of the note this memento was captured from. */
    public UUID getNoteId() {
        return noteId;
    }
}
