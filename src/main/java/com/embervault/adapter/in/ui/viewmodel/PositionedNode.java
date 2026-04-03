package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

/**
 * A generic positioned node for any layout algorithm.
 *
 * <p>Each node has coordinates (x, y) within a viewport,
 * a display size, and a depth indicating the BFS depth from
 * the focus node. This decouples layout strategies from any
 * specific view type.</p>
 *
 * @param noteId the note id
 * @param x      the x position in viewport coordinates
 * @param y      the y position in viewport coordinates
 * @param size   the rendered size (larger near center)
 * @param depth  the BFS depth from the focus node
 */
public record PositionedNode(UUID noteId, double x, double y,
        double size, int depth) {

    /**
     * Canonical constructor with validation.
     */
    public PositionedNode {
        Objects.requireNonNull(noteId, "noteId must not be null");
    }
}
