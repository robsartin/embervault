package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TimelineLayout} — maps notes with dates to positioned rectangles.
 */
class TimelineLayoutTest {

    private static final double WIDTH = 800.0;
    private static final double HEIGHT = 400.0;
    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("empty items returns empty result")
    void emptyItems() {
        List<TimelineRect> rects = TimelineLayout.layout(List.of(),
                WIDTH, HEIGHT);
        assertTrue(rects.isEmpty());
    }

    @Test
    @DisplayName("single item is placed within the bounds")
    void singleItem() {
        UUID id = UUID.randomUUID();
        TimelineItem item = new TimelineItem(id, BASE, null);

        List<TimelineRect> rects = TimelineLayout.layout(List.of(item),
                WIDTH, HEIGHT);

        assertEquals(1, rects.size());
        TimelineRect rect = rects.getFirst();
        assertEquals(id, rect.id());
        assertTrue(rect.x() >= 0 && rect.x() <= WIDTH);
        assertTrue(rect.y() >= 0);
    }

    @Test
    @DisplayName("earlier dates are placed to the left of later dates")
    void ordering() {
        UUID early = UUID.randomUUID();
        UUID late = UUID.randomUUID();
        TimelineItem earlyItem = new TimelineItem(early, BASE, null);
        TimelineItem lateItem = new TimelineItem(late,
                BASE.plus(30, ChronoUnit.DAYS), null);

        List<TimelineRect> rects = TimelineLayout.layout(
                List.of(earlyItem, lateItem), WIDTH, HEIGHT);

        TimelineRect earlyRect = rects.stream()
                .filter(r -> r.id().equals(early)).findFirst().orElseThrow();
        TimelineRect lateRect = rects.stream()
                .filter(r -> r.id().equals(late)).findFirst().orElseThrow();

        assertTrue(earlyRect.x() < lateRect.x(),
                "Earlier date should be left of later date");
    }

    @Test
    @DisplayName("item with date range has proportional width")
    void dateRange_hasWidth() {
        UUID id = UUID.randomUUID();
        Instant start = BASE;
        Instant end = BASE.plus(10, ChronoUnit.DAYS);
        TimelineItem item = new TimelineItem(id, start, end);

        List<TimelineRect> rects = TimelineLayout.layout(List.of(item),
                WIDTH, HEIGHT);

        TimelineRect rect = rects.getFirst();
        assertTrue(rect.width() > 0,
                "Date range item should have positive width");
    }

    @Test
    @DisplayName("overlapping dates get different y positions")
    void overlappingDates_differentY() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        TimelineItem item1 = new TimelineItem(id1, BASE, null);
        TimelineItem item2 = new TimelineItem(id2, BASE, null);

        List<TimelineRect> rects = TimelineLayout.layout(
                List.of(item1, item2), WIDTH, HEIGHT);

        TimelineRect rect1 = rects.stream()
                .filter(r -> r.id().equals(id1)).findFirst().orElseThrow();
        TimelineRect rect2 = rects.stream()
                .filter(r -> r.id().equals(id2)).findFirst().orElseThrow();

        assertTrue(rect1.y() != rect2.y(),
                "Overlapping items should have different y positions");
    }
}
