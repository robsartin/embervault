package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Computes a hyperbolic (Poincare disk) layout for a link graph.
 *
 * <p>Uses BFS from a focus node to arrange linked notes radially.
 * Each successive level is placed further from the center with
 * exponentially decreasing node sizes, simulating hyperbolic projection.</p>
 */
final class HyperbolicLayout {

    /** Decay factor for radius per level. */
    static final double DECAY_FACTOR = 0.5;

    /** Base radius for level-0 (focus) node as fraction of viewport radius. */
    static final double BASE_RADIUS_FRACTION = 0.12;

    /** Radius of the first ring as fraction of viewport radius. */
    static final double RING_1_FRACTION = 0.30;

    /** Multiplier for successive ring radii. */
    static final double RING_GROWTH = 1.6;

    private HyperbolicLayout() {
        // utility class
    }

    /**
     * Computes the hyperbolic layout.
     *
     * @param focusId        the focus note id
     * @param adjacency      adjacency list (note id to set of neighbor note ids)
     * @param viewportRadius the viewport radius in pixels
     * @return the list of positioned nodes
     */
    static List<PositionedNode> layout(UUID focusId,
            Map<UUID, Set<UUID>> adjacency, double viewportRadius) {

        if (!adjacency.containsKey(focusId)) {
            return List.of(new PositionedNode(focusId, 0, 0,
                    viewportRadius * BASE_RADIUS_FRACTION, 0));
        }

        // BFS to assign levels
        Map<UUID, Integer> levels = new HashMap<>();
        Map<UUID, UUID> parent = new HashMap<>();
        Queue<UUID> queue = new ArrayDeque<>();
        levels.put(focusId, 0);
        queue.add(focusId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            int currentLevel = levels.get(current);
            Set<UUID> neighbors = adjacency.getOrDefault(current, Set.of());
            for (UUID neighbor : neighbors) {
                if (!levels.containsKey(neighbor)) {
                    levels.put(neighbor, currentLevel + 1);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Group nodes by level
        Map<Integer, List<UUID>> byLevel = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : levels.entrySet()) {
            byLevel.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        // Group children by parent for level > 0
        Map<UUID, List<UUID>> childrenByParent = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : parent.entrySet()) {
            childrenByParent.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        List<PositionedNode> result = new ArrayList<>();

        // Place focus node at center
        double focusRadius = viewportRadius * BASE_RADIUS_FRACTION;
        result.add(new PositionedNode(focusId, 0, 0, focusRadius, 0));

        // Compute positions for each node by parent
        Map<UUID, double[]> positions = new HashMap<>();
        positions.put(focusId, new double[]{0, 0});

        int maxLevel = byLevel.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

        for (int level = 1; level <= maxLevel; level++) {
            List<UUID> nodesAtLevel = byLevel.getOrDefault(level, List.of());
            if (nodesAtLevel.isEmpty()) {
                continue;
            }

            double ringRadius = viewportRadius * RING_1_FRACTION
                    * Math.pow(RING_GROWTH, level - 1.0);
            double nodeRadius = focusRadius * Math.pow(DECAY_FACTOR, level);

            // For level 1, place evenly around the center
            if (level == 1) {
                placeChildrenAroundParent(nodesAtLevel, 0, 0,
                        ringRadius, nodeRadius, level, 0, 2 * Math.PI,
                        positions, result);
            } else {
                // For level > 1, place children around their parent
                placeByParent(nodesAtLevel, childrenByParent, parent, positions,
                        ringRadius, nodeRadius, level, result);
            }
        }

        return result;
    }

    private static void placeByParent(List<UUID> nodesAtLevel,
            Map<UUID, List<UUID>> childrenByParent,
            Map<UUID, UUID> parentMap,
            Map<UUID, double[]> positions,
            double ringRadius, double nodeRadius, int level,
            List<PositionedNode> result) {

        // Collect distinct parents for nodes at this level
        Set<UUID> parents = new HashSet<>();
        for (UUID node : nodesAtLevel) {
            parents.add(parentMap.get(node));
        }

        for (UUID par : parents) {
            List<UUID> children = childrenByParent.getOrDefault(par, List.of());
            // Filter to only those at this level
            List<UUID> levelChildren = new ArrayList<>();
            for (UUID child : children) {
                if (nodesAtLevel.contains(child)) {
                    levelChildren.add(child);
                }
            }
            if (levelChildren.isEmpty()) {
                continue;
            }

            double[] parentPos = positions.getOrDefault(par, new double[]{0, 0});
            // Compute the angle from center to parent
            double parentAngle = Math.atan2(parentPos[1], parentPos[0]);
            // Spread children in a sector around the parent's angle
            double sectorSize = Math.PI / Math.max(1, level);

            placeChildrenAroundParent(levelChildren,
                    parentPos[0], parentPos[1],
                    ringRadius * 0.5, nodeRadius, level,
                    parentAngle - sectorSize / 2,
                    parentAngle + sectorSize / 2,
                    positions, result);
        }
    }

    private static void placeChildrenAroundParent(List<UUID> children,
            double parentX, double parentY,
            double ringRadius, double nodeRadius, int level,
            double startAngle, double endAngle,
            Map<UUID, double[]> positions,
            List<PositionedNode> result) {

        int count = children.size();
        for (int i = 0; i < count; i++) {
            double angle;
            if (count == 1) {
                angle = (startAngle + endAngle) / 2;
            } else {
                angle = startAngle + (endAngle - startAngle) * i / (count - 1.0);
            }

            double nodeX = parentX + ringRadius * Math.cos(angle);
            double nodeY = parentY + ringRadius * Math.sin(angle);

            // Clamp to Poincare disk (within 0.95 of a reasonable viewport)
            UUID nodeId = children.get(i);
            positions.put(nodeId, new double[]{nodeX, nodeY});
            result.add(new PositionedNode(nodeId, nodeX, nodeY, nodeRadius, level));
        }
    }
}
