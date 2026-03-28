package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

/**
 * An edge in the hyperbolic layout connecting two notes.
 *
 * @param sourceId      the source note id
 * @param destinationId the destination note id
 */
public record HyperbolicEdge(UUID sourceId, UUID destinationId) {

    /**
     * Canonical constructor with validation.
     */
    public HyperbolicEdge {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(destinationId, "destinationId must not be null");
    }
}
