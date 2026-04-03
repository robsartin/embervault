package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapViewInteractionStateTest {

    private MapViewInteractionState state;

    @BeforeEach
    void setUp() {
        state = new MapViewInteractionState();
    }

    @Test
    @DisplayName("rendering is false initially")
    void rendering_shouldBeFalseInitially() {
        assertFalse(state.isRendering());
    }

    @Test
    @DisplayName("tryBeginRender returns true when not rendering")
    void tryBeginRender_shouldReturnTrueWhenNotRendering() {
        assertTrue(state.tryBeginRender());
    }

    @Test
    @DisplayName("tryBeginRender sets rendering to true")
    void tryBeginRender_shouldSetRenderingTrue() {
        state.tryBeginRender();

        assertTrue(state.isRendering());
    }

    @Test
    @DisplayName("tryBeginRender returns false when already rendering")
    void tryBeginRender_shouldReturnFalseWhenAlreadyRendering() {
        state.tryBeginRender();

        assertFalse(state.tryBeginRender());
    }

    @Test
    @DisplayName("endRender sets rendering to false")
    void endRender_shouldSetRenderingFalse() {
        state.tryBeginRender();

        state.endRender();

        assertFalse(state.isRendering());
    }
}
