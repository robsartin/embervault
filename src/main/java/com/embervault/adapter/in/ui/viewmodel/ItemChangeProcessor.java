package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Classifies list-change events for note display items.
 *
 * <p>Extracts the decision logic from {@code onNoteItemsChanged} in the
 * controller so it can be unit-tested without a JavaFX scene graph.
 * The controller delegates to this class to decide whether a change
 * requires a full re-render or can be handled incrementally.</p>
 */
public final class ItemChangeProcessor {

    private ItemChangeProcessor() { }

    /**
     * Result of classifying a replacement change.
     *
     * @param requiresFullRender true if the change cannot be handled
     *                           incrementally
     * @param updatedItems       items that can be updated in place
     * @param staleIds           IDs removed from the list but not re-added
     */
    public record ReplacementResult(
            boolean requiresFullRender,
            List<NoteDisplayItem> updatedItems,
            Set<UUID> staleIds) { }

    /**
     * Classifies a replacement change into an incremental update or a
     * full re-render.
     *
     * <p>If every added item has a corresponding node in {@code knownIds},
     * the change can be handled incrementally by updating those nodes.
     * Any removed item whose ID is not in the added set is stale and
     * should be removed from the scene.</p>
     *
     * @param addedItems   the items added by the replacement
     * @param removedItems the items removed by the replacement
     * @param knownIds     the set of IDs that have existing nodes
     * @return the classification result
     */
    public static ReplacementResult classifyReplacement(
            List<? extends NoteDisplayItem> addedItems,
            List<? extends NoteDisplayItem> removedItems,
            Set<UUID> knownIds) {
        Set<UUID> addedIds = new HashSet<>();
        List<NoteDisplayItem> updatedItems = new ArrayList<>();

        for (NoteDisplayItem item : addedItems) {
            addedIds.add(item.getId());
            if (knownIds.contains(item.getId())) {
                updatedItems.add(item);
            } else {
                return new ReplacementResult(true, List.of(), Set.of());
            }
        }

        Set<UUID> staleIds = new HashSet<>();
        for (NoteDisplayItem removed : removedItems) {
            if (!addedIds.contains(removed.getId())) {
                staleIds.add(removed.getId());
            }
        }

        return new ReplacementResult(false, updatedItems, staleIds);
    }
}
