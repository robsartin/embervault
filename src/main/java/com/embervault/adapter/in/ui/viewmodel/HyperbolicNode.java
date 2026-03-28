package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

/**
 * A positioned node in the hyperbolic layout.
 *
 * <p>Each node has coordinates (x, y) within the Poincare disk,
 * a display radius that decreases with distance from the focus,
 * and a level indicating the BFS depth from the focus node.</p>
 *
 * @param noteId        the note id
 * @param x             the x position in viewport coordinates
 * @param y             the y position in viewport coordinates
 * @param displayRadius the rendered radius (larger near center)
 * @param level         the BFS depth from the focus node
 */
public record HyperbolicNode(UUID noteId, double x, double y,
        double displayRadius, int level) {

    /**
     * Canonical constructor with validation.
     */
    public HyperbolicNode {
        Objects.requireNonNull(noteId, "noteId must not be null");
    }
}
