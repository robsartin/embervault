package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import org.testfx.util.WaitForAsyncUtils;

/**
 * Branch coverage tests for {@link SearchViewController}.
 *
 * <p>Covers debounce timing, empty query edge case, Escape hide,
 * Enter with empty results, close button, result cell rendering,
 * and focus-on-visible behavior.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class SearchViewControllerBranchTest {

    private SearchViewController controller;
    private SearchViewModel viewModel;
    private NoteService noteService;
    private VBox searchRoot;
    private TextField searchField;
    private Button closeButton;
    private ListView<NoteDisplayItem> resultsList;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository =
                new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        noteService.createNote("Apple", "fruit content");
        noteService.createNote("Banana", "fruit content");
        noteService.createNote("ApplePie", "dessert content");

        viewModel = new SearchViewModel(noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState());

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

    // --- Debounce ---

    @Test
    @DisplayName("debounce fires search after 200ms pause")
    void debounce_firesSearchAfterDelay(FxRobot robot)
            throws Exception {
        viewModel.visibleProperty().set(true);

        robot.interact(() -> searchField.setText("Apple"));

        // Wait for debounce (200ms) plus buffer
        WaitForAsyncUtils.waitFor(
                1, TimeUnit.SECONDS,
                () -> !viewModel.getResults().isEmpty());

        assertTrue(viewModel.getResults().size() >= 2,
                "Debounce should trigger search matching Apple");
    }

    @Test
    @DisplayName("debounce restarts on each keystroke")
    void debounce_restartsOnKeystroke(FxRobot robot)
            throws Exception {
        viewModel.visibleProperty().set(true);

        robot.interact(() -> searchField.setText("Ap"));

        // Quickly change the text (simulates rapid typing)
        robot.interact(() -> searchField.setText("Ban"));

        WaitForAsyncUtils.waitFor(
                1, TimeUnit.SECONDS,
                () -> !viewModel.getResults().isEmpty());

        // Should find Banana, not Apple results
        boolean hasBanana = viewModel.getResults().stream()
                .anyMatch(r -> r.getTitle().equals("Banana"));
        assertTrue(hasBanana,
                "Debounce should search with latest text");
    }

    // --- Empty query ---

    @Test
    @DisplayName("Enter on empty query produces no results")
    void enter_emptyQuery_noResults(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        robot.interact(() -> searchField.setText(""));

        robot.interact(() -> {
            searchField.requestFocus();
            searchField.fireEvent(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "", "", KeyCode.ENTER,
                    false, false, false, false));
        });

        assertTrue(viewModel.getResults().isEmpty(),
                "Empty query should produce no results");
    }

    @Test
    @DisplayName("Enter with no matching results does not "
            + "select anything")
    void enter_noResults_noSelection(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        robot.interact(
                () -> searchField.setText("ZZZZZ_NoMatch"));

        robot.interact(() -> {
            searchField.requestFocus();
            searchField.fireEvent(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "", "", KeyCode.ENTER,
                    false, false, false, false));
        });

        assertNull(viewModel.selectedNoteIdProperty().get(),
                "No result should mean no selection");
    }

    // --- Escape ---

    @Test
    @DisplayName("Escape hides search and clears state")
    void escape_hidesAndClears(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        viewModel.search("Apple");
        assertFalse(viewModel.getResults().isEmpty());

        robot.interact(() -> {
            searchField.requestFocus();
            searchField.fireEvent(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "", "", KeyCode.ESCAPE,
                    false, false, false, false));
        });

        assertFalse(viewModel.visibleProperty().get(),
                "Search bar should be hidden after Escape");
    }

    // --- Close button ---

    @Test
    @DisplayName("close button hides the search bar")
    void closeButton_hidesSearch(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        assertTrue(searchRoot.isVisible());

        // The handleClose method calls viewModel.hide()
        robot.interact(() -> {
            try {
                var method = SearchViewController.class
                        .getDeclaredMethod("handleClose");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });

        assertFalse(viewModel.visibleProperty().get(),
                "Close button should hide search bar");
    }

    // --- Results list visibility ---

    @Test
    @DisplayName("results list becomes visible when results "
            + "are added")
    void resultsList_visibleWhenResultsAdded(FxRobot robot) {
        assertFalse(resultsList.isVisible());

        robot.interact(() -> viewModel.search("Apple"));

        assertTrue(resultsList.isVisible(),
                "Results list should be visible with results");
        assertTrue(resultsList.isManaged(),
                "Results list should be managed with results");
    }

    @Test
    @DisplayName("results list hides when results become empty")
    void resultsList_hidesWhenEmpty(FxRobot robot) {
        robot.interact(() -> viewModel.search("Apple"));
        assertTrue(resultsList.isVisible());

        robot.interact(() -> viewModel.getResults().clear());
        assertFalse(resultsList.isVisible(),
                "Results list should hide when empty");
    }

    // --- Result click selection ---

    @Test
    @DisplayName("clicking a result selects that note")
    void clickResult_selectsNote(FxRobot robot) {
        viewModel.visibleProperty().set(true);
        robot.interact(() -> viewModel.search("Apple"));

        UUID firstId =
                viewModel.getResults().get(0).getId();
        robot.interact(() -> resultsList.getSelectionModel()
                .select(0));

        // Simulate mouse click by triggering the handler
        robot.interact(() -> resultsList.fireEvent(
                new javafx.scene.input.MouseEvent(
                        javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                        0, 0, 0, 0,
                        javafx.scene.input.MouseButton.PRIMARY,
                        1,
                        false, false, false, false,
                        true, false, false,
                        false, false, false,
                        null)));

        assertEquals(firstId,
                viewModel.selectedNoteIdProperty().get(),
                "Clicking result should select that note");
    }

    // --- Managed property binding ---

    @Test
    @DisplayName("search root managed property follows "
            + "visibility")
    void searchRoot_managedFollowsVisibility(FxRobot robot) {
        assertFalse(searchRoot.isManaged(),
                "Should not be managed when hidden");

        robot.interact(
                () -> viewModel.visibleProperty().set(true));
        assertTrue(searchRoot.isManaged(),
                "Should be managed when visible");
    }

    // --- Query bidirectional binding ---

    @Test
    @DisplayName("setting viewModel query updates search field")
    void viewModelQuery_updatesSearchField(FxRobot robot) {
        robot.interact(
                () -> viewModel.queryProperty().set("test"));
        assertEquals("test", searchField.getText(),
                "Field should reflect viewModel query");
    }

    @Test
    @DisplayName("typing in search field updates viewModel "
            + "query")
    void searchField_updatesViewModelQuery(FxRobot robot) {
        robot.interact(
                () -> searchField.setText("typed"));
        assertEquals("typed",
                viewModel.queryProperty().get(),
                "ViewModel query should reflect field text");
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
