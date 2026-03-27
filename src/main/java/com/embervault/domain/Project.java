package com.embervault.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A project entity representing a collection of notes with a root note.
 */
public final class Project {

    private final UUID id;
    private final String name;
    private final Note rootNote;

    /**
     * Creates a new Project with the given id, name, and root note.
     */
    public Project(UUID id, String name, Note rootNote) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(rootNote, "rootNote must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        this.id = id;
        this.name = name;
        this.rootNote = rootNote;
    }

    /**
     * Creates an empty project with a root Note titled "Untitled".
     */
    public static Project createEmpty() {
        Note rootNote = Note.create("Untitled", "");
        return new Project(UUID.randomUUID(), "Untitled", rootNote);
    }

    /** Returns the unique identifier. */
    public UUID getId() {
        return id;
    }

    /** Returns the project name. */
    public String getName() {
        return name;
    }

    /** Returns the root note. */
    public Note getRootNote() {
        return rootNote;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Project other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name + "'}";
    }
}
