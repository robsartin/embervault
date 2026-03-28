package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchNotesTest {

    private NoteService service;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
    }

    @Test
    @DisplayName("searchNotes() returns empty list for null query")
    void searchNotes_shouldReturnEmptyForNullQuery() {
        service.createNote("Alpha", "content");

        List<Note> results = service.searchNotes(null);

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchNotes() returns empty list for blank query")
    void searchNotes_shouldReturnEmptyForBlankQuery() {
        service.createNote("Alpha", "content");

        List<Note> results = service.searchNotes("   ");

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchNotes() returns empty list for empty query")
    void searchNotes_shouldReturnEmptyForEmptyQuery() {
        service.createNote("Alpha", "content");

        List<Note> results = service.searchNotes("");

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchNotes() matches title substring case-insensitively")
    void searchNotes_shouldMatchTitleCaseInsensitive() {
        service.createNote("Hello World", "no match here");
        service.createNote("Goodbye", "no match either");

        List<Note> results = service.searchNotes("hello");

        assertEquals(1, results.size());
        assertEquals("Hello World", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchNotes() matches text content substring case-insensitively")
    void searchNotes_shouldMatchTextCaseInsensitive() {
        service.createNote("NoMatch", "The quick brown fox");
        service.createNote("AlsoNoMatch", "lazy dog");

        List<Note> results = service.searchNotes("QUICK");

        assertEquals(1, results.size());
        assertEquals("NoMatch", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchNotes() returns title matches before text matches")
    void searchNotes_shouldReturnTitleMatchesFirst() {
        Note textMatch = service.createNote("Alpha", "Meeting notes for today");
        Note titleMatch = service.createNote("Meeting agenda", "some content");

        List<Note> results = service.searchNotes("meeting");

        assertEquals(2, results.size());
        assertEquals(titleMatch.getId(), results.get(0).getId());
        assertEquals(textMatch.getId(), results.get(1).getId());
    }

    @Test
    @DisplayName("searchNotes() deduplicates notes that match both title and text")
    void searchNotes_shouldDeduplicateMatches() {
        service.createNote("Meeting notes", "Meeting agenda for today");

        List<Note> results = service.searchNotes("meeting");

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("searchNotes() returns empty when no notes match")
    void searchNotes_shouldReturnEmptyWhenNoMatch() {
        service.createNote("Alpha", "content");
        service.createNote("Beta", "more content");

        List<Note> results = service.searchNotes("zzzzz");

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchNotes() returns empty when no notes exist")
    void searchNotes_shouldReturnEmptyWhenNoNotes() {
        List<Note> results = service.searchNotes("anything");

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchNotes() matches partial substrings")
    void searchNotes_shouldMatchPartialSubstrings() {
        service.createNote("Embervault", "");

        List<Note> results = service.searchNotes("vault");

        assertEquals(1, results.size());
        assertEquals("Embervault", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchNotes() returns multiple title matches")
    void searchNotes_shouldReturnMultipleTitleMatches() {
        service.createNote("Meeting Monday", "");
        service.createNote("Meeting Tuesday", "");
        service.createNote("Unrelated", "");

        List<Note> results = service.searchNotes("meeting");

        assertEquals(2, results.size());
    }
}
