package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PositionedNodeTest {

    @Test
    @DisplayName("record stores all fields correctly")
    void record_shouldStoreAllFields() {
        UUID id = UUID.randomUUID();
        PositionedNode node = new PositionedNode(id, 1.5, 2.5, 10.0, 3);

        assertEquals(id, node.noteId());
        assertEquals(1.5, node.x(), 0.001);
        assertEquals(2.5, node.y(), 0.001);
        assertEquals(10.0, node.size(), 0.001);
        assertEquals(3, node.depth());
    }

    @Test
    @DisplayName("constructor rejects null noteId")
    void constructor_shouldRejectNullNoteId() {
        assertThrows(NullPointerException.class,
                () -> new PositionedNode(null, 0, 0, 10.0, 0));
    }
}
