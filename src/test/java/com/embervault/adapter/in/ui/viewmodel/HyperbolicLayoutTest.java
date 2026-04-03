package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HyperbolicLayoutTest {

    private static final double VIEWPORT_RADIUS = 300.0;

    @Test
    @DisplayName("single node is placed at origin")
    void singleNode_shouldBePlacedAtOrigin() {
        UUID focus = UUID.randomUUID();
        Map<UUID, Set<UUID>> adjacency = new HashMap<>();

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(1, nodes.size());
        PositionedNode node = nodes.get(0);
        assertEquals(focus, node.noteId());
        assertEquals(0, node.x(), 0.001);
        assertEquals(0, node.y(), 0.001);
        assertEquals(0, node.depth());
        assertTrue(node.size() > 0);
    }

    @Test
    @DisplayName("star graph places center at origin and neighbors on ring")
    void starGraph_shouldPlaceCenterAndNeighborsOnRing() {
        UUID focus = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(focus, new HashSet<>(Set.of(n1, n2, n3)));
        adjacency.put(n1, new HashSet<>(Set.of(focus)));
        adjacency.put(n2, new HashSet<>(Set.of(focus)));
        adjacency.put(n3, new HashSet<>(Set.of(focus)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(4, nodes.size());

        PositionedNode center = findNode(nodes, focus);
        assertNotNull(center);
        assertEquals(0, center.x(), 0.001);
        assertEquals(0, center.y(), 0.001);
        assertEquals(0, center.depth());

        // Neighbors should be at depth 1 and not at origin
        for (UUID id : List.of(n1, n2, n3)) {
            PositionedNode neighbor = findNode(nodes, id);
            assertNotNull(neighbor);
            assertEquals(1, neighbor.depth());
            double dist = Math.sqrt(neighbor.x() * neighbor.x()
                    + neighbor.y() * neighbor.y());
            assertTrue(dist > 0, "Neighbor should not be at origin");
        }
    }

    @Test
    @DisplayName("chain graph assigns increasing depths")
    void chainGraph_shouldAssignIncreasingDepths() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        UUID d = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(a, new HashSet<>(Set.of(b)));
        adjacency.put(b, new HashSet<>(Set.of(a, c)));
        adjacency.put(c, new HashSet<>(Set.of(b, d)));
        adjacency.put(d, new HashSet<>(Set.of(c)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                a, adjacency, VIEWPORT_RADIUS);

        assertEquals(4, nodes.size());
        assertEquals(0, findNode(nodes, a).depth());
        assertEquals(1, findNode(nodes, b).depth());
        assertEquals(2, findNode(nodes, c).depth());
        assertEquals(3, findNode(nodes, d).depth());
    }

    @Test
    @DisplayName("node size decreases with depth")
    void nodeSize_shouldDecreaseWithDepth() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(a, new HashSet<>(Set.of(b)));
        adjacency.put(b, new HashSet<>(Set.of(a, c)));
        adjacency.put(c, new HashSet<>(Set.of(b)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                a, adjacency, VIEWPORT_RADIUS);

        double depth0Size = findNode(nodes, a).size();
        double depth1Size = findNode(nodes, b).size();
        double depth2Size = findNode(nodes, c).size();

        assertTrue(depth0Size > depth1Size,
                "Depth 0 size should be larger than depth 1");
        assertTrue(depth1Size > depth2Size,
                "Depth 1 size should be larger than depth 2");
    }

    @Test
    @DisplayName("cycle graph does not produce duplicates")
    void cycleGraph_shouldNotProduceDuplicates() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(a, new HashSet<>(Set.of(b, c)));
        adjacency.put(b, new HashSet<>(Set.of(a, c)));
        adjacency.put(c, new HashSet<>(Set.of(a, b)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                a, adjacency, VIEWPORT_RADIUS);

        assertEquals(3, nodes.size());
        Set<UUID> ids = new HashSet<>();
        for (PositionedNode node : nodes) {
            assertTrue(ids.add(node.noteId()),
                    "Duplicate node: " + node.noteId());
        }
    }

    @Test
    @DisplayName("focus node not in adjacency still returns single node")
    void focusNotInAdjacency_shouldReturnSingleNode() {
        UUID focus = UUID.randomUUID();
        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(UUID.randomUUID(), Set.of(UUID.randomUUID()));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(1, nodes.size());
        assertEquals(focus, nodes.get(0).noteId());
    }

    @Test
    @DisplayName("two-level graph places depth 2 nodes around their parent")
    void twoLevelGraph_shouldPlaceDepth2NodesAroundParent() {
        UUID focus = UUID.randomUUID();
        UUID child1 = UUID.randomUUID();
        UUID grandchild1 = UUID.randomUUID();
        UUID grandchild2 = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(focus, new HashSet<>(Set.of(child1)));
        adjacency.put(child1, new HashSet<>(Set.of(focus, grandchild1, grandchild2)));
        adjacency.put(grandchild1, new HashSet<>(Set.of(child1)));
        adjacency.put(grandchild2, new HashSet<>(Set.of(child1)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(4, nodes.size());
        assertEquals(2, findNode(nodes, grandchild1).depth());
        assertEquals(2, findNode(nodes, grandchild2).depth());
    }

    @Test
    @DisplayName("single child at depth 1 is placed correctly")
    void singleChildAtDepth1_shouldBePlacedCorrectly() {
        UUID focus = UUID.randomUUID();
        UUID child = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(focus, new HashSet<>(Set.of(child)));
        adjacency.put(child, new HashSet<>(Set.of(focus)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                focus, adjacency, VIEWPORT_RADIUS);

        assertEquals(2, nodes.size());
        PositionedNode childNode = findNode(nodes, child);
        assertNotNull(childNode);
        assertEquals(1, childNode.depth());
        double dist = Math.sqrt(childNode.x() * childNode.x()
                + childNode.y() * childNode.y());
        assertTrue(dist > 0, "Single child should not be at origin");
    }

    @Test
    @DisplayName("empty level in between is handled")
    void emptyLevelGap_shouldStillWork() {
        // This tests when all neighbors of a node are already visited
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        adjacency.put(a, new HashSet<>(Set.of(b)));
        adjacency.put(b, new HashSet<>(Set.of(a)));

        List<PositionedNode> nodes = HyperbolicLayout.layout(
                a, adjacency, VIEWPORT_RADIUS);

        assertEquals(2, nodes.size());
    }

    private static PositionedNode findNode(List<PositionedNode> nodes, UUID id) {
        for (PositionedNode node : nodes) {
            if (node.noteId().equals(id)) {
                return node;
            }
        }
        return null;
    }
}
