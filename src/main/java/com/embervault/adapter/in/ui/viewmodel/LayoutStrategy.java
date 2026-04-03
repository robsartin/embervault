package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Strategy interface for computing the spatial layout of graph nodes.
 *
 * <p>Implementations position nodes from an adjacency graph within a
 * viewport of the given radius. The Strategy pattern decouples layout
 * algorithms from the ViewModel that holds graph data.</p>
 */
public interface LayoutStrategy {

    /**
     * Computes the layout for the given graph.
     *
     * @param focusId        the focus node id (placed at center)
     * @param adjacency      adjacency list (node id to set of neighbor ids)
     * @param viewportRadius the viewport radius in pixels
     * @return the list of positioned nodes
     */
    List<HyperbolicNode> layout(UUID focusId,
            Map<UUID, Set<UUID>> adjacency, double viewportRadius);
}
