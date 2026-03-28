package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TreemapLayoutTest {

    @Test
    @DisplayName("Single item fills entire bounds")
    void singleItem_shouldFillEntireBounds() {
        UUID id = UUID.randomUUID();
        List<TreemapItem> items = List.of(new TreemapItem(id, 1.0));

        List<TreemapRect> rects = TreemapLayout.layout(items, 0, 0, 800, 600);

        assertEquals(1, rects.size());
        TreemapRect r = rects.get(0);
        assertEquals(id, r.id());
        assertEquals(0, r.x(), 0.01);
        assertEquals(0, r.y(), 0.01);
        assertEquals(800, r.width(), 0.01);
        assertEquals(600, r.height(), 0.01);
    }

    @Test
    @DisplayName("Two equal items split the bounds in half")
    void twoEqualItems_shouldSplitBoundsInHalf() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<TreemapItem> items = List.of(
                new TreemapItem(id1, 1.0),
                new TreemapItem(id2, 1.0));

        List<TreemapRect> rects = TreemapLayout.layout(items, 0, 0, 800, 600);

        assertEquals(2, rects.size());
        double totalArea = rects.stream()
                .mapToDouble(r -> r.width() * r.height())
                .sum();
        assertEquals(800 * 600, totalArea, 1.0);
    }

    @Test
    @DisplayName("Multiple items: total area matches bounds")
    void multipleItems_totalAreaMatchesBounds() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 3.0),
                new TreemapItem(UUID.randomUUID(), 2.0),
                new TreemapItem(UUID.randomUUID(), 1.0),
                new TreemapItem(UUID.randomUUID(), 4.0));

        List<TreemapRect> rects = TreemapLayout.layout(items, 10, 20, 400, 300);

        assertEquals(4, rects.size());
        double totalArea = rects.stream()
                .mapToDouble(r -> r.width() * r.height())
                .sum();
        assertEquals(400 * 300, totalArea, 1.0);
    }

    @Test
    @DisplayName("Multiple items: all rects within bounds")
    void multipleItems_allRectsWithinBounds() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 3.0),
                new TreemapItem(UUID.randomUUID(), 2.0),
                new TreemapItem(UUID.randomUUID(), 1.0));

        List<TreemapRect> rects = TreemapLayout.layout(items, 10, 20, 400, 300);

        for (TreemapRect r : rects) {
            assertTrue(r.x() >= 10 - 0.01, "x should be >= 10");
            assertTrue(r.y() >= 20 - 0.01, "y should be >= 20");
            assertTrue(r.x() + r.width() <= 410 + 0.01,
                    "right edge should be <= 410");
            assertTrue(r.y() + r.height() <= 320 + 0.01,
                    "bottom edge should be <= 320");
        }
    }

    @Test
    @DisplayName("Zero-weight items are excluded from output")
    void zeroWeightItems_shouldBeExcluded() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 1.0),
                new TreemapItem(UUID.randomUUID(), 0.0),
                new TreemapItem(UUID.randomUUID(), 2.0));

        List<TreemapRect> rects = TreemapLayout.layout(items, 0, 0, 400, 300);

        assertEquals(2, rects.size());
        double totalArea = rects.stream()
                .mapToDouble(r -> r.width() * r.height())
                .sum();
        assertEquals(400 * 300, totalArea, 1.0);
    }

    @Test
    @DisplayName("Empty item list returns empty result")
    void emptyItems_shouldReturnEmpty() {
        List<TreemapRect> rects = TreemapLayout.layout(
                List.of(), 0, 0, 400, 300);

        assertTrue(rects.isEmpty());
    }

    @Test
    @DisplayName("Areas proportional to weights")
    void areas_shouldBeProportionalToWeights() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<TreemapItem> items = List.of(
                new TreemapItem(id1, 3.0),
                new TreemapItem(id2, 1.0));

        List<TreemapRect> rects = TreemapLayout.layout(
                items, 0, 0, 400, 400);

        TreemapRect r1 = rects.stream()
                .filter(r -> r.id().equals(id1)).findFirst().orElseThrow();
        TreemapRect r2 = rects.stream()
                .filter(r -> r.id().equals(id2)).findFirst().orElseThrow();
        double area1 = r1.width() * r1.height();
        double area2 = r2.width() * r2.height();
        assertEquals(3.0, area1 / area2, 0.1);
    }

    @Test
    @DisplayName("Rects have positive width and height")
    void rects_shouldHavePositiveDimensions() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 5.0),
                new TreemapItem(UUID.randomUUID(), 3.0),
                new TreemapItem(UUID.randomUUID(), 2.0),
                new TreemapItem(UUID.randomUUID(), 1.0),
                new TreemapItem(UUID.randomUUID(), 1.0));

        List<TreemapRect> rects = TreemapLayout.layout(
                items, 0, 0, 600, 400);

        for (TreemapRect r : rects) {
            assertTrue(r.width() > 0, "width should be > 0");
            assertTrue(r.height() > 0, "height should be > 0");
        }
    }

    @Test
    @DisplayName("Negative weights are treated as zero")
    void negativeWeights_shouldBeTreatedAsZero() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 1.0),
                new TreemapItem(UUID.randomUUID(), -2.0));

        List<TreemapRect> rects = TreemapLayout.layout(
                items, 0, 0, 400, 300);

        assertEquals(1, rects.size());
    }

    @Test
    @DisplayName("All zero-weight items returns empty result")
    void allZeroWeightItems_shouldReturnEmpty() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 0.0),
                new TreemapItem(UUID.randomUUID(), 0.0));

        List<TreemapRect> rects = TreemapLayout.layout(
                items, 0, 0, 400, 300);

        assertTrue(rects.isEmpty());
    }

    @Test
    @DisplayName("Three items at offset bounds still fit within bounds")
    void threeItems_atOffset_shouldFitWithinBounds() {
        List<TreemapItem> items = List.of(
                new TreemapItem(UUID.randomUUID(), 2.0),
                new TreemapItem(UUID.randomUUID(), 3.0),
                new TreemapItem(UUID.randomUUID(), 5.0));

        List<TreemapRect> rects = TreemapLayout.layout(
                items, 50, 100, 300, 200);

        assertEquals(3, rects.size());
        for (TreemapRect r : rects) {
            assertTrue(r.x() >= 50 - 0.01);
            assertTrue(r.y() >= 100 - 0.01);
            assertTrue(r.x() + r.width() <= 350 + 0.01);
            assertTrue(r.y() + r.height() <= 300 + 0.01);
        }
    }
}
