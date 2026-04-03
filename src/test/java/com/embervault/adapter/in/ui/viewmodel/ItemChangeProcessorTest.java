package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemChangeProcessorTest {

    @Test
    @DisplayName("classifyReplacement returns UPDATE for items with known IDs")
    void classifyReplacement_knownIds_shouldReturnUpdate() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Set<UUID> knownIds = Set.of(id1, id2);

        NoteDisplayItem item1 = new NoteDisplayItem(id1, "A", "");
        NoteDisplayItem item2 = new NoteDisplayItem(id2, "B", "");

        ItemChangeProcessor.ReplacementResult result =
                ItemChangeProcessor.classifyReplacement(
                        List.of(item1, item2),
                        List.of(item1, item2),
                        knownIds);

        assertFalse(result.requiresFullRender());
        assertEquals(2, result.updatedItems().size());
        assertTrue(result.staleIds().isEmpty());
    }

    @Test
    @DisplayName("classifyReplacement requires full render for unknown added IDs")
    void classifyReplacement_unknownIds_shouldRequireFullRender() {
        UUID id1 = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();
        Set<UUID> knownIds = Set.of(id1);

        NoteDisplayItem item1 = new NoteDisplayItem(id1, "A", "");
        NoteDisplayItem unknownItem = new NoteDisplayItem(unknownId, "B", "");

        ItemChangeProcessor.ReplacementResult result =
                ItemChangeProcessor.classifyReplacement(
                        List.of(item1, unknownItem),
                        List.of(item1),
                        knownIds);

        assertTrue(result.requiresFullRender());
    }

    @Test
    @DisplayName("classifyReplacement identifies stale removed IDs")
    void classifyReplacement_staleIds_shouldBeReported() {
        UUID id1 = UUID.randomUUID();
        UUID staleId = UUID.randomUUID();
        Set<UUID> knownIds = Set.of(id1, staleId);

        NoteDisplayItem item1 = new NoteDisplayItem(id1, "A", "");
        NoteDisplayItem staleItem = new NoteDisplayItem(staleId, "B", "");

        ItemChangeProcessor.ReplacementResult result =
                ItemChangeProcessor.classifyReplacement(
                        List.of(item1),
                        List.of(item1, staleItem),
                        knownIds);

        assertFalse(result.requiresFullRender());
        assertEquals(1, result.staleIds().size());
        assertTrue(result.staleIds().contains(staleId));
    }
}
