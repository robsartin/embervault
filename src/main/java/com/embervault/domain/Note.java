package com.embervault.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A note entity backed by a type-safe attribute map.
 *
 * <p>Provides backward-compatible convenience methods ({@code getName()},
 * {@code getText()}, etc.) that delegate to the underlying attribute map.
 * Core Tinderbox attributes are stored as {@code $Name}, {@code $Text},
 * {@code $Created}, and {@code $Modified}.</p>
 */
public final class Note {

    private final UUID id;
    private final AttributeMap attributes;
    private final List<UUID> childIds;
    private UUID prototypeId;

    /**
     * Creates a new Note with the given id and pre-populated attribute map.
     *
     * @param id         the unique identifier
     * @param attributes the attribute map
     */
    public Note(UUID id, AttributeMap attributes) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        this.id = id;
        this.attributes = attributes;
        this.childIds = new ArrayList<>();
    }

    /**
     * Creates a new Note with the given id, title, content, and timestamps.
     *
     * <p>This constructor provides backward compatibility with the original
     * Note API.</p>
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
        this.childIds = new ArrayList<>();
        this.attributes = new AttributeMap();
        this.attributes.set("$Name", new AttributeValue.StringValue(title));
        this.attributes.set("$Text", new AttributeValue.StringValue(content));
        this.attributes.set("$Created", new AttributeValue.DateValue(createdAt));
        this.attributes.set("$Modified", new AttributeValue.DateValue(updatedAt));
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

    /** Returns the title ($Name attribute). */
    public String getTitle() {
        return attributes.get("$Name")
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse("");
    }

    /** Returns the title ($Name attribute). Alias for getTitle(). */
    public String getName() {
        return getTitle();
    }

    /** Returns the content ($Text attribute). */
    public String getContent() {
        return attributes.get("$Text")
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse("");
    }

    /** Returns the content ($Text attribute). Alias for getContent(). */
    public String getText() {
        return getContent();
    }

    /** Returns the creation timestamp ($Created attribute). */
    public Instant getCreatedAt() {
        return attributes.get("$Created")
                .map(v -> ((AttributeValue.DateValue) v).value())
                .orElse(Instant.EPOCH);
    }

    /** Returns the last-updated timestamp ($Modified attribute). */
    public Instant getUpdatedAt() {
        return attributes.get("$Modified")
                .map(v -> ((AttributeValue.DateValue) v).value())
                .orElse(Instant.EPOCH);
    }

    /** Returns the underlying attribute map. */
    public AttributeMap getAttributes() {
        return attributes;
    }

    /**
     * Gets a specific attribute value by name.
     *
     * @param name the attribute name
     * @return an optional containing the value, or empty
     */
    public Optional<AttributeValue> getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Sets a specific attribute value by name.
     *
     * @param name  the attribute name
     * @param value the value to set
     */
    public void setAttribute(String name, AttributeValue value) {
        attributes.set(name, value);
    }

    /**
     * Removes a local attribute value.
     *
     * @param name the attribute name
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /** Returns the prototype note id, if set. */
    public Optional<UUID> getPrototypeId() {
        return Optional.ofNullable(prototypeId);
    }

    /**
     * Sets the prototype note id.
     *
     * @param protoId the prototype note id, or null to clear
     */
    public void setPrototypeId(UUID protoId) {
        this.prototypeId = protoId;
    }

    /**
     * Adds a child note id to the end of the children list.
     *
     * @param childId the child note id
     */
    public void addChild(UUID childId) {
        Objects.requireNonNull(childId, "childId must not be null");
        childIds.add(childId);
    }

    /**
     * Removes a child note id from the children list.
     *
     * @param childId the child note id
     */
    public void removeChild(UUID childId) {
        childIds.remove(childId);
    }

    /**
     * Returns an unmodifiable view of the child note ids.
     *
     * @return unmodifiable list of child ids
     */
    public List<UUID> getChildIds() {
        return Collections.unmodifiableList(childIds);
    }

    /**
     * Returns whether this note has any children.
     *
     * @return true if this note has children
     */
    public boolean hasChildren() {
        return !childIds.isEmpty();
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

        this.attributes.set("$Name", new AttributeValue.StringValue(newTitle));
        this.attributes.set("$Text", new AttributeValue.StringValue(newContent));
        this.attributes.set("$Modified", new AttributeValue.DateValue(Instant.now()));
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
        return "Note{id=" + id + ", title='" + getTitle() + "'}";
    }
}
