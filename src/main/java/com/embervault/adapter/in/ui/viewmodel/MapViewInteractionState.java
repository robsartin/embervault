package com.embervault.adapter.in.ui.viewmodel;

/**
 * Tracks interaction state for the Map view.
 *
 * <p>Encapsulates the rendering guard flag and drag-tracking state that
 * were previously scattered across primitive fields and arrays in the
 * controller. The guard prevents re-entrant rendering when list-change
 * listeners fire during a render pass.</p>
 */
public final class MapViewInteractionState {

    private boolean rendering;
    private boolean dragging;
    private double dragDeltaX;
    private double dragDeltaY;
    private double dragX;
    private double dragY;

    /** Returns whether the view is currently rendering. */
    public boolean isRendering() {
        return rendering;
    }

    /**
     * Attempts to begin a render pass.
     *
     * @return true if rendering was started, false if already rendering
     */
    public boolean tryBeginRender() {
        if (rendering) {
            return false;
        }
        rendering = true;
        return true;
    }

    /** Ends the current render pass. */
    public void endRender() {
        rendering = false;
    }

    /** Returns whether a drag is in progress. */
    public boolean isDragging() {
        return dragging;
    }

    /**
     * Begins tracking a drag from the given layout and scene positions.
     *
     * @param layoutX the node's layout X
     * @param layoutY the node's layout Y
     * @param sceneX  the mouse scene X at press time
     * @param sceneY  the mouse scene Y at press time
     */
    public void beginDrag(double layoutX, double layoutY,
            double sceneX, double sceneY) {
        dragDeltaX = layoutX - sceneX;
        dragDeltaY = layoutY - sceneY;
        dragging = false;
    }

    /**
     * Updates the drag position from the current mouse scene coordinates.
     *
     * @param sceneX the current mouse scene X
     * @param sceneY the current mouse scene Y
     */
    public void updateDrag(double sceneX, double sceneY) {
        dragging = true;
        dragX = Math.max(0, sceneX + dragDeltaX);
        dragY = Math.max(0, sceneY + dragDeltaY);
    }

    /** Returns the drag delta X (layout minus scene offset). */
    public double getDragDeltaX() {
        return dragDeltaX;
    }

    /** Returns the drag delta Y (layout minus scene offset). */
    public double getDragDeltaY() {
        return dragDeltaY;
    }

    /** Returns the computed drag X position (clamped to zero). */
    public double getDragX() {
        return dragX;
    }

    /** Returns the computed drag Y position (clamped to zero). */
    public double getDragY() {
        return dragY;
    }
}
