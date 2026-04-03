package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // --- Drag state tests ---

    @Test
    @DisplayName("dragging is false initially")
    void dragging_shouldBeFalseInitially() {
        assertFalse(state.isDragging());
    }

    @Test
    @DisplayName("beginDrag records offset and marks dragging false initially")
    void beginDrag_shouldRecordOffset() {
        state.beginDrag(100.0, 200.0, 50.0, 60.0);

        assertFalse(state.isDragging());
        assertEquals(50.0, state.getDragDeltaX(), 0.001);
        assertEquals(140.0, state.getDragDeltaY(), 0.001);
    }

    @Test
    @DisplayName("updateDrag marks dragging true and computes clamped position")
    void updateDrag_shouldMarkDraggingAndComputePosition() {
        state.beginDrag(100.0, 200.0, 50.0, 60.0);

        state.updateDrag(80.0, 100.0);

        assertTrue(state.isDragging());
        assertEquals(130.0, state.getDragX(), 0.001);
        assertEquals(240.0, state.getDragY(), 0.001);
    }

    @Test
    @DisplayName("updateDrag clamps position to minimum zero")
    void updateDrag_shouldClampToZero() {
        state.beginDrag(10.0, 20.0, 50.0, 60.0);

        state.updateDrag(0.0, 0.0);

        assertEquals(0.0, state.getDragX(), 0.001);
        assertEquals(0.0, state.getDragY(), 0.001);
    }
}
