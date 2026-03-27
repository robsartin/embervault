package com.embervault.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A note entity representing a titled piece of content.
 */
public final class Note {

    private final UUID id;
    private String title;
    private String content;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Creates a new Note with the given id, title, content, and timestamps.
     */
    public Note(UUID id, String title, String content,
            Instant createdAt, Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new Note with auto-generated id and current timestamps.
     */
    public static Note create(String title, String content) {
        Instant now = Instant.now();
        return new Note(UUID.randomUUID(), title, content, now, now);
    }

    /** Returns the unique identifier. */
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

    /** Returns the creation timestamp. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Returns the last-updated timestamp. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Updates the title and content, setting updatedAt to now.
     */
    public void update(String newTitle, String newContent) {
        Objects.requireNonNull(newTitle, "title must not be null");
        Objects.requireNonNull(newContent, "content must not be null");

        if (newTitle.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        this.title = newTitle;
        this.content = newContent;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Note other)) {
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
        return "Note{id=" + id + ", title='" + title + "'}";
    }
}
