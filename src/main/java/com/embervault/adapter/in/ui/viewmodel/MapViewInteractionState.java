package com.embervault.adapter.in.ui.viewmodel;

/**
 * Tracks interaction state for the Map view.
 *
 * <p>Encapsulates the rendering guard flag that was previously a bare
 * boolean field in the controller. The guard prevents re-entrant
 * rendering when list-change listeners fire during a render pass.</p>
 */
public final class MapViewInteractionState {

    private boolean rendering;

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
}
