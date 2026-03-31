package com.embervault.adapter.in.ui.viewmodel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Timeline layout algorithm: maps items with dates to positioned rectangles.
 *
 * <p>Items are placed on a horizontal axis proportional to their date
 * within the overall time range. Date-range items get proportional width;
 * point-in-time items get a fixed marker width. Overlapping items are
 * stacked vertically.</p>
 */
final class TimelineLayout {

    private static final double ITEM_HEIGHT = 30.0;
    private static final double ITEM_GAP = 4.0;
    private static final double MARKER_WIDTH = 8.0;
    private static final double PADDING = 20.0;

    private TimelineLayout() {
    }

    /**
     * Computes timeline positions for the given items.
     *
     * @param items      the items to lay out
     * @param totalWidth the total available width
     * @param totalHeight the total available height
     * @return positioned rectangles
     */
    static List<TimelineRect> layout(List<TimelineItem> items,
            double totalWidth, double totalHeight) {
        if (items.isEmpty()) {
            return List.of();
        }

        // Determine time range
        Instant earliest = items.stream()
                .map(TimelineItem::start)
                .min(Comparator.naturalOrder()).orElseThrow();
        Instant latest = items.stream()
                .map(i -> i.end() != null ? i.end() : i.start())
                .max(Comparator.naturalOrder()).orElseThrow();

        long rangeMillis = latest.toEpochMilli() - earliest.toEpochMilli();
        if (rangeMillis == 0) {
            rangeMillis = 1; // avoid division by zero for same-date items
        }

        double usableWidth = totalWidth - 2 * PADDING;

        // Sort by start date, then lay out with overlap detection
        List<TimelineItem> sorted = items.stream()
                .sorted(Comparator.comparing(TimelineItem::start))
                .toList();

        List<TimelineRect> result = new ArrayList<>();
        // Track the rightmost x extent at each row for overlap detection
        List<Double> rowExtents = new ArrayList<>();

        for (TimelineItem item : sorted) {
            double x = PADDING + (item.start().toEpochMilli()
                    - earliest.toEpochMilli())
                    / (double) rangeMillis * usableWidth;
            double w;
            if (item.end() != null) {
                w = (item.end().toEpochMilli()
                        - item.start().toEpochMilli())
                        / (double) rangeMillis * usableWidth;
                w = Math.max(w, MARKER_WIDTH);
            } else {
                w = MARKER_WIDTH;
            }

            // Find first row where this item fits without overlap
            int row = 0;
            while (row < rowExtents.size()
                    && rowExtents.get(row) > x) {
                row++;
            }
            if (row >= rowExtents.size()) {
                rowExtents.add(0.0);
            }
            rowExtents.set(row, x + w + ITEM_GAP);

            double y = row * (ITEM_HEIGHT + ITEM_GAP);
            result.add(new TimelineRect(item.id(), x, y, w,
                    ITEM_HEIGHT));
        }

        return result;
    }
}
