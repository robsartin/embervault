package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HyperbolicLayoutStrategyTest {

    private static final double VIEWPORT_RADIUS = 300.0;

    @Test
    @DisplayName("implements LayoutStrategy interface")
    void shouldImplementLayoutStrategy() {
        LayoutStrategy strategy = new HyperbolicLayoutStrategy();
        assertTrue(strategy instanceof LayoutStrategy);
    }

    @Test
    @DisplayName("single node is placed at origin via strategy")
    void singleNode_shouldBePlacedAtOrigin() {
        LayoutStrategy strategy = new HyperbolicLayoutStrategy();
        UUID focus = UUID.randomUUID();
        Map<UUID, Set<UUID>> adjacency = new HashMap<>();

        List<PositionedNode> nodes = strategy.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(1, nodes.size());
        PositionedNode node = nodes.get(0);
        assertEquals(focus, node.noteId());
        assertEquals(0, node.x(), 0.001);
        assertEquals(0, node.y(), 0.001);
    }

    @Test
    @DisplayName("star graph produces correct number of nodes via strategy")
    void starGraph_shouldProduceCorrectNodeCount() {
        LayoutStrategy strategy = new HyperbolicLayoutStrategy();
        UUID focus = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(focus, new HashSet<>(Set.of(n1, n2)));
        adjacency.put(n1, new HashSet<>(Set.of(focus)));
        adjacency.put(n2, new HashSet<>(Set.of(focus)));

        List<PositionedNode> nodes = strategy.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(3, nodes.size());
    }
}
