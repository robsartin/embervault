package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link SearchViewController}.
 *
 * <p>Verifies visibility toggling, search result population, result list
 * show/hide behavior, Enter-key immediate search, and Escape-key hide.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class SearchViewControllerTest {

    private SearchViewController controller;
    private SearchViewModel viewModel;
    private NoteService noteService;
    private VBox searchRoot;
    private TextField searchField;
    private Button closeButton;
    private ListView<?> resultsList;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        // Create some notes for searching
        noteService.createNote("Alpha Note", "content about alpha");
        noteService.createNote("Beta Note", "content about beta");
        noteService.createNote("Alpha Beta", "mixed content");

        viewModel = new SearchViewModel(noteService);

        controller = new SearchViewController();
        searchRoot = new VBox();
        searchField = new TextField();
        closeButton = new Button("X");
        resultsList = new ListView<>();

        injectField("searchRoot", searchRoot);
        injectField("searchField", searchField);
        injectField("closeButton", closeButton);
        injectField("resultsList", resultsList);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("search root visibility is bound to viewModel visible property")
    void searchRoot_visibilityBoundToViewModel() {
        assertFalse(searchRoot.isVisible(),
                "Search bar should be hidden initially");

        viewModel.visibleProperty().set(true);
        assertTrue(searchRoot.isVisible(),
                "Search bar should be visible after setting visible true");

        viewModel.visibleProperty().set(false);
        assertFalse(searchRoot.isVisible(),
                "Search bar should hide after setting visible false");
    }

    @Test
    @DisplayName("search field text is bound bidirectionally to query property")
    void searchField_boundToQueryProperty() {
        viewModel.queryProperty().set("test query");
        assertEquals("test query", searchField.getText());

        searchField.setText("another query");
        assertEquals("another query", viewModel.queryProperty().get());
    }

    @Test
    @DisplayName("results list is hidden when results are empty")
    void resultsList_hiddenWhenEmpty() {
        assertFalse(resultsList.isVisible(),
                "Results list should be hidden when empty");
        assertFalse(resultsList.isManaged(),
                "Results list should not be managed when empty");
    }

    @Test
    @DisplayName("results list is shown after search produces results")
    void resultsList_shownAfterSearchWithResults() {
        viewModel.search("Alpha");

        assertTrue(resultsList.isVisible(),
                "Results list should be visible after search with results");
        assertTrue(resultsList.isManaged(),
                "Results list should be managed after search with results");
        assertEquals(2, viewModel.getResults().size(),
                "Should find two notes matching 'Alpha'");
    }

    @Test
    @DisplayName("results list hides when results are cleared")
    void resultsList_hidesWhenCleared() {
        viewModel.search("Alpha");
        assertTrue(resultsList.isVisible());

        viewModel.getResults().clear();
        assertFalse(resultsList.isVisible(),
                "Results list should hide when cleared");
    }

    @Test
    @DisplayName("Enter key bypasses debounce and searches immediately")
    void enterKey_searchesImmediately(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        searchField.setText("Beta");

        robot.interact(() -> {
            searchField.requestFocus();
            searchField.fireEvent(new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_PRESSED,
                    "", "", KeyCode.ENTER,
                    false, false, false, false));
        });

        assertFalse(viewModel.getResults().isEmpty(),
                "Enter should trigger immediate search");
        assertTrue(viewModel.getResults().size() >= 1,
                "Should find notes matching 'Beta'");
    }

    @Test
    @DisplayName("Enter key selects first result when results exist")
    void enterKey_selectsFirstResult(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        searchField.setText("Alpha");

        robot.interact(() -> {
            searchField.requestFocus();
            searchField.fireEvent(new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_PRESSED,
                    "", "", KeyCode.ENTER,
                    false, false, false, false));
        });

        UUID selectedId = viewModel.selectedNoteIdProperty().get();
        assertNotNull(selectedId,
                "Enter should select first search result");
    }

    @Test
    @DisplayName("Escape key hides the search bar")
    void escapeKey_hidesSearchBar(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        assertTrue(searchRoot.isVisible());

        robot.interact(() -> {
            searchField.requestFocus();
            searchField.fireEvent(new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_PRESSED,
                    "", "", KeyCode.ESCAPE,
                    false, false, false, false));
        });

        assertFalse(viewModel.visibleProperty().get(),
                "Escape should hide the search bar");
    }

    @Test
    @DisplayName("hide() clears query and results")
    void hide_clearsQueryAndResults() {
        viewModel.queryProperty().set("test");
        viewModel.search("Alpha");
        assertFalse(viewModel.getResults().isEmpty());

        viewModel.hide();

        assertEquals("", viewModel.queryProperty().get(),
                "Query should be cleared after hide");
        assertTrue(viewModel.getResults().isEmpty(),
                "Results should be cleared after hide");
        assertFalse(viewModel.visibleProperty().get(),
                "Visible should be false after hide");
    }

    @Test
    @DisplayName("search with no matches produces empty results")
    void search_noMatches_emptyResults() {
        viewModel.search("NonexistentNoteTitle");

        assertTrue(viewModel.getResults().isEmpty(),
                "Search for non-existent term should produce no results");
        assertFalse(resultsList.isVisible(),
                "Results list should be hidden with no matches");
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = SearchViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
