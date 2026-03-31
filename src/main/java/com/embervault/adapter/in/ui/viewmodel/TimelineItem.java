package com.embervault.adapter.in.ui.viewmodel;

import java.time.Instant;
import java.util.UUID;

/**
 * A note to be placed on the timeline.
 *
 * @param id    the note id
 * @param start the start date (required)
 * @param end   the end date (nullable — point-in-time if null)
 */
record TimelineItem(UUID id, Instant start, Instant end) {
}
