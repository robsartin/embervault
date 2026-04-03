package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.SearchNotesQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchViewModelTest {

    private SearchViewModel viewModel;
    private NoteService noteService;
    private AppState appState;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        appState = new AppState();
        SearchNotesQuery searchQuery = noteService;
        viewModel = new SearchViewModel(searchQuery, appState);
    }

    @Test
    @DisplayName("queryProperty() is initially empty")
    void queryProperty_shouldBeInitiallyEmpty() {
        assertEquals("", viewModel.queryProperty().get());
    }

    @Test
    @DisplayName("resultsProperty() is initially empty")
    void resultsProperty_shouldBeInitiallyEmpty() {
        assertTrue(viewModel.getResults().isEmpty());
    }

    @Test
    @DisplayName("visibleProperty() is initially false")
    void visibleProperty_shouldBeInitiallyFalse() {
        assertFalse(viewModel.visibleProperty().get());
    }

    @Test
    @DisplayName("search() populates results from NoteService")
    void search_shouldPopulateResults() {
        noteService.createNote("Meeting notes", "Important stuff");
        noteService.createNote("Shopping list", "Eggs, milk");

        viewModel.search("meeting");

        assertEquals(1, viewModel.getResults().size());
        assertEquals("Meeting notes", viewModel.getResults().get(0).getTitle());
    }

    @Test
    @DisplayName("search() clears previous results")
    void search_shouldClearPreviousResults() {
        noteService.createNote("Alpha", "");
        noteService.createNote("Beta", "");

        viewModel.search("alpha");
        assertEquals(1, viewModel.getResults().size());

        viewModel.search("beta");
        assertEquals(1, viewModel.getResults().size());
        assertEquals("Beta", viewModel.getResults().get(0).getTitle());
    }

    @Test
    @DisplayName("search() with blank query clears results")
    void search_shouldClearResultsForBlankQuery() {
        noteService.createNote("Alpha", "");

        viewModel.search("alpha");
        assertEquals(1, viewModel.getResults().size());

        viewModel.search("");
        assertTrue(viewModel.getResults().isEmpty());
    }

    @Test
    @DisplayName("search() orders title matches before text matches")
    void search_shouldOrderTitleMatchesFirst() {
        noteService.createNote("Unrelated title", "Contains meeting in text");
        noteService.createNote("Meeting agenda", "Some content");

        viewModel.search("meeting");

        assertEquals(2, viewModel.getResults().size());
        assertEquals("Meeting agenda", viewModel.getResults().get(0).getTitle());
    }

    @Test
    @DisplayName("selectResult() sets selectedNoteIdProperty")
    void selectResult_shouldSetSelectedNoteId() {
        UUID noteId = UUID.randomUUID();

        viewModel.selectResult(noteId);

        assertEquals(noteId, viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("selectResult(null) clears selection")
    void selectResult_null_shouldClearSelection() {
        viewModel.selectResult(UUID.randomUUID());

        viewModel.selectResult(null);

        assertNull(viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("toggleVisible() flips visibility")
    void toggleVisible_shouldFlipVisibility() {
        assertFalse(viewModel.visibleProperty().get());

        viewModel.toggleVisible();
        assertTrue(viewModel.visibleProperty().get());

        viewModel.toggleVisible();
        assertFalse(viewModel.visibleProperty().get());
    }

    @Test
    @DisplayName("hide() sets visibility to false")
    void hide_shouldSetVisibleToFalse() {
        viewModel.toggleVisible();
        assertTrue(viewModel.visibleProperty().get());

        viewModel.hide();
        assertFalse(viewModel.visibleProperty().get());
    }

    @Test
    @DisplayName("hide() clears query and results")
    void hide_shouldClearQueryAndResults() {
        noteService.createNote("Alpha", "");
        viewModel.queryProperty().set("alpha");
        viewModel.search("alpha");

        viewModel.hide();

        assertEquals("", viewModel.queryProperty().get());
        assertTrue(viewModel.getResults().isEmpty());
    }

    @Test
    @DisplayName("search() converts Note to NoteDisplayItem")
    void search_shouldConvertToNoteDisplayItem() {
        noteService.createNote("Test Note", "Some content");

        viewModel.search("test");

        NoteDisplayItem item = viewModel.getResults().get(0);
        assertNotNull(item.getId());
        assertEquals("Test Note", item.getTitle());
        assertEquals("Some content", item.getContent());
    }

    @Test
    @DisplayName("constructor rejects null searchNotesQuery")
    void constructor_shouldRejectNullSearchNotesQuery() {
        org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class,
                () -> new SearchViewModel(null, appState));
    }

    @Test
    @DisplayName("refreshResults() re-runs the current search when visible")
    void refreshResults_shouldRerunSearchWhenVisible() {
        noteService.createNote("Alpha", "");
        viewModel.toggleVisible();
        viewModel.queryProperty().set("alpha");
        viewModel.search("alpha");
        assertEquals(1, viewModel.getResults().size());

        // Add another matching note and refresh
        noteService.createNote("Alpha Two", "");
        viewModel.refreshResults();

        assertEquals(2, viewModel.getResults().size());
    }

    @Test
    @DisplayName("refreshResults() does nothing when search is hidden")
    void refreshResults_shouldDoNothingWhenHidden() {
        noteService.createNote("Alpha", "");
        viewModel.queryProperty().set("alpha");
        viewModel.search("alpha");
        assertEquals(1, viewModel.getResults().size());

        // Hidden by default — refresh should not update results
        noteService.createNote("Alpha Two", "");
        viewModel.refreshResults();

        assertEquals(1, viewModel.getResults().size());
    }

    @Test
    @DisplayName("refreshResults() does nothing when query is blank")
    void refreshResults_shouldDoNothingWhenQueryBlank() {
        noteService.createNote("Alpha", "");
        viewModel.toggleVisible();
        viewModel.refreshResults();

        assertTrue(viewModel.getResults().isEmpty());
    }

}
