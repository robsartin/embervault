package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A {@link LayoutStrategy} that delegates to the hyperbolic (Poincare disk)
 * layout algorithm.
 *
 * <p>Arranges linked notes radially using BFS from a focus node, with
 * exponentially decreasing node sizes to simulate hyperbolic projection.</p>
 */
public final class HyperbolicLayoutStrategy implements LayoutStrategy {

    @Override
    public List<HyperbolicNode> layout(UUID focusId,
            Map<UUID, Set<UUID>> adjacency, double viewportRadius) {
        return HyperbolicLayout.layout(focusId, adjacency, viewportRadius);
    }
}
