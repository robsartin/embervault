package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

/**
 * Presentation-layer representation of a note for display in the View.
 *
 * <p>Decouples the View from the domain {@code Note} entity, satisfying
 * the ADR-0013 constraint that Views must not reference domain packages.</p>
 */
public final class NoteDisplayItem {

    private final UUID id;
    private final String title;
    private final String content;

    /**
     * Constructs a display item with the given id, title, and content.
     */
    public NoteDisplayItem(UUID id, String title, String content) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    /** Returns the note id. */
    public UUID getId() {
        return id;
    }

    /** Returns the title. */
    public String getTitle() {
        return title;
    }

    /** Returns the content. */
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NoteDisplayItem other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
