package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.SearchNotesQuery;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchNotesQueryTest {

    private SearchNotesQuery searchQuery;
    private CreateNoteUseCase creator;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        NoteServiceImpl service = new NoteServiceImpl(repository);
        searchQuery = service;
        creator = service;
    }

    @Test
    @DisplayName("NoteServiceImpl implements SearchNotesQuery")
    void noteServiceImpl_shouldImplementSearchNotesQuery() {
        assertTrue(searchQuery instanceof SearchNotesQuery);
    }

    @Test
    @DisplayName("searchNotes() finds notes by title")
    void searchNotes_shouldFindByTitle() {
        creator.createNote("Alpha", "content");
        creator.createNote("Beta", "content");

        List<Note> results = searchQuery.searchNotes("Alpha");

        assertEquals(1, results.size());
        assertEquals("Alpha", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchNotes() returns empty for blank query")
    void searchNotes_shouldReturnEmptyForBlank() {
        creator.createNote("Note", "content");

        assertTrue(searchQuery.searchNotes("").isEmpty());
        assertTrue(searchQuery.searchNotes(null).isEmpty());
    }
}
