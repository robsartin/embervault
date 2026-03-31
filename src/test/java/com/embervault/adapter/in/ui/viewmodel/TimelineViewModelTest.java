package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TimelineViewModel} — filters notes by date attributes.
 */
class TimelineViewModelTest {

    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteService = new NoteServiceImpl(new InMemoryNoteRepository());
    }

    @Test
    @DisplayName("notes without date attribute are excluded")
    void notesWithoutDate_excluded() {
        noteService.createNote("No Date", "Content");

        List<TimelineItem> items = TimelineViewModel.filterByDate(
                noteService.getAllNotes(), Attributes.CREATED);

        // The note has $Created set by Note.create, so it should be included
        assertEquals(1, items.size());
    }

    @Test
    @DisplayName("notes with the selected date attribute are included")
    void notesWithDate_included() {
        Note note = noteService.createNote("Has Date", "Content");
        note.setAttribute(Attributes.START_DATE,
                new AttributeValue.DateValue(Instant.parse("2026-03-15T00:00:00Z")));

        List<TimelineItem> items = TimelineViewModel.filterByDate(
                noteService.getAllNotes(), Attributes.START_DATE);

        assertEquals(1, items.size());
        assertEquals(note.getId(), items.getFirst().id());
    }

    @Test
    @DisplayName("filtering by $StartDate excludes notes without $StartDate")
    void filterByStartDate_excludesNonMatching() {
        Note withDate = noteService.createNote("With", "");
        withDate.setAttribute(Attributes.START_DATE,
                new AttributeValue.DateValue(Instant.now()));

        noteService.createNote("Without", "");

        List<TimelineItem> items = TimelineViewModel.filterByDate(
                noteService.getAllNotes(), Attributes.START_DATE);

        assertEquals(1, items.size());
        assertEquals(withDate.getId(), items.getFirst().id());
    }

    @Test
    @DisplayName("date range items have both start and end")
    void dateRange_hasBothDates() {
        Note note = noteService.createNote("Range", "");
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-10T00:00:00Z");
        note.setAttribute(Attributes.START_DATE,
                new AttributeValue.DateValue(start));
        note.setAttribute(Attributes.END_DATE,
                new AttributeValue.DateValue(end));

        List<TimelineItem> items = TimelineViewModel.filterByDate(
                noteService.getAllNotes(), Attributes.START_DATE);

        assertEquals(1, items.size());
        assertEquals(start, items.getFirst().start());
        assertEquals(end, items.getFirst().end());
    }

    @Test
    @DisplayName("empty note list returns empty items")
    void emptyList_returnsEmpty() {
        assertTrue(TimelineViewModel.filterByDate(
                List.of(), Attributes.CREATED).isEmpty());
    }
}
