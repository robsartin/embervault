package com.embervault.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A link between two notes, representing a directed relationship.
 *
 * <p>Links are the core navigation primitive for the Hyperbolic view.
 * Each link connects a source note to a destination note with an optional
 * type descriptor (defaulting to "untitled").</p>
 *
 * @param id            the unique identifier of this link
 * @param sourceId      the id of the source note
 * @param destinationId the id of the destination note
 * @param type          the link type (e.g., "untitled", "web", "prototype")
 */
public record Link(UUID id, UUID sourceId, UUID destinationId, String type) {

    /**
     * Canonical constructor with validation.
     */
    public Link {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(destinationId, "destinationId must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    /**
     * Creates a new link with a generated id and default type "untitled".
     *
     * @param source the source note id
     * @param dest   the destination note id
     * @return a new Link
     */
    public static Link create(UUID source, UUID dest) {
        return new Link(UuidGenerator.generate(), source, dest, "untitled");
    }

    /**
     * Creates a new link with a generated id and the specified type.
     *
     * @param source the source note id
     * @param dest   the destination note id
     * @param type   the link type
     * @return a new Link
     */
    public static Link create(UUID source, UUID dest, String type) {
        return new Link(UuidGenerator.generate(), source, dest, type);
    }
}
