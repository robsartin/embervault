package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Slice-and-dice treemap layout algorithm.
 *
 * <p>Produces a list of positioned rectangles from a list of weighted items.
 * The algorithm alternates horizontal and vertical splits at each recursion
 * depth, subdividing the bounding rectangle proportionally to each item's
 * weight.</p>
 */
final class TreemapLayout {

    private TreemapLayout() {
        // utility class
    }

    /**
     * Computes the treemap layout for the given items within the specified bounds.
     *
     * <p>Items with zero or negative weight are excluded. The total area of the
     * output rectangles equals {@code width * height}.</p>
     *
     * @param items  the weighted items to lay out
     * @param x      the x-coordinate of the bounding rectangle
     * @param y      the y-coordinate of the bounding rectangle
     * @param width  the width of the bounding rectangle
     * @param height the height of the bounding rectangle
     * @return the positioned rectangles, one per positive-weight item
     */
    static List<TreemapRect> layout(List<TreemapItem> items,
            double x, double y, double width, double height) {
        List<TreemapItem> filtered = items.stream()
                .filter(item -> item.weight() > 0)
                .toList();
        if (filtered.isEmpty()) {
            return List.of();
        }
        List<TreemapRect> result = new ArrayList<>();
        sliceAndDice(filtered, x, y, width, height, 0, result);
        return result;
    }

    private static void sliceAndDice(List<TreemapItem> items,
            double x, double y, double width, double height,
            int depth, List<TreemapRect> result) {
        if (items.size() == 1) {
            result.add(new TreemapRect(items.get(0).id(), x, y, width, height));
            return;
        }
        double totalWeight = items.stream().mapToDouble(TreemapItem::weight).sum();
        boolean horizontal = (depth % 2 == 0);
        double offset = 0;
        for (int i = 0; i < items.size(); i++) {
            TreemapItem item = items.get(i);
            double fraction = item.weight() / totalWeight;
            if (horizontal) {
                double sliceWidth = fraction * width;
                result.add(new TreemapRect(item.id(),
                        x + offset, y, sliceWidth, height));
                offset += sliceWidth;
            } else {
                double sliceHeight = fraction * height;
                result.add(new TreemapRect(item.id(),
                        x, y + offset, width, sliceHeight));
                offset += sliceHeight;
            }
        }
    }
}
