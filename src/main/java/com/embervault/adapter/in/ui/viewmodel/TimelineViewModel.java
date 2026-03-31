package com.embervault.adapter.in.ui.viewmodel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;

/**
 * ViewModel for the Timeline view.
 *
 * <p>Provides a static method to filter notes by a selected date attribute,
 * producing {@link TimelineItem}s suitable for layout.</p>
 */
public final class TimelineViewModel {

    private TimelineViewModel() {
    }

    /**
     * Filters notes that have the given date attribute, producing TimelineItems.
     *
     * <p>If the date attribute is {@code $StartDate} and the note also has
     * {@code $EndDate}, the item includes both for date-range rendering.
     * Otherwise the item is point-in-time.</p>
     *
     * @param notes         the notes to filter
     * @param dateAttribute the attribute name to use (e.g., Attributes.CREATED)
     * @return timeline items for notes with the given date attribute
     */
    public static List<TimelineItem> filterByDate(List<Note> notes,
            String dateAttribute) {
        if (notes.isEmpty()) {
            return Collections.emptyList();
        }
        List<TimelineItem> items = new ArrayList<>();
        for (Note note : notes) {
            note.getAttribute(dateAttribute)
                    .filter(v -> v instanceof AttributeValue.DateValue)
                    .map(v -> ((AttributeValue.DateValue) v).value())
                    .ifPresent(start -> {
                        Instant end = null;
                        if (Attributes.START_DATE.equals(dateAttribute)) {
                            end = note.getAttribute(Attributes.END_DATE)
                                    .filter(v -> v
                                            instanceof AttributeValue.DateValue)
                                    .map(v -> ((AttributeValue.DateValue) v)
                                            .value())
                                    .orElse(null);
                        }
                        items.add(new TimelineItem(note.getId(), start, end));
                    });
        }
        return Collections.unmodifiableList(items);
    }
}
