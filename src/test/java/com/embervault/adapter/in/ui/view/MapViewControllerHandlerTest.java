package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for extracted event handler methods in {@link MapViewController}.
 *
 * <p>Verifies that {@code handleScrollZoom} and {@code handleKeyPress}
 * can be called directly with synthetic events, without needing a
 * TestFX robot.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class MapViewControllerHandlerTest {

    private MapViewController controller;
    private MapViewModel viewModel;
    private NoteService noteService;
    private Pane mapCanvas;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        SimpleStringProperty noteTitle = new SimpleStringProperty("Test");
        viewModel = new MapViewModel(noteTitle, noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState());

        parentId = noteService.createNote("Parent", "").getId();
        viewModel.setBaseNoteId(parentId);

        controller = new MapViewController();
        mapCanvas = new Pane();

        try {
            var field = MapViewController.class.getDeclaredField("mapCanvas");
            field.setAccessible(true);
            field.set(controller, mapCanvas);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        controller.initViewModel(viewModel);
    }

    // --- handleScrollZoom tests ---

    @Test
    @DisplayName("handleScrollZoom zooms in when deltaY is positive")
    void handleScrollZoom_positiveDeltaY_zoomsIn() {
        double initialZoom = viewModel.zoomLevelProperty().get();
        ScrollEvent event = createScrollEvent(1.0, 100, 100);

        controller.handleScrollZoom(event);

        assertTrue(viewModel.zoomLevelProperty().get() > initialZoom,
                "Zoom level should increase on positive scroll");
    }

    @Test
    @DisplayName("handleScrollZoom zooms out when deltaY is negative")
    void handleScrollZoom_negativeDeltaY_zoomsOut() {
        double initialZoom = viewModel.zoomLevelProperty().get();
        ScrollEvent event = createScrollEvent(-1.0, 100, 100);

        controller.handleScrollZoom(event);

        assertTrue(viewModel.zoomLevelProperty().get() < initialZoom,
                "Zoom level should decrease on negative scroll");
    }

    @Test
    @DisplayName("handleScrollZoom applies correct zoom factor (1.1x)")
    void handleScrollZoom_appliesCorrectFactor() {
        viewModel.setZoomLevel(1.0);
        ScrollEvent event = createScrollEvent(1.0, 50, 50);

        controller.handleScrollZoom(event);

        assertEquals(1.1, viewModel.zoomLevelProperty().get(), 0.01,
                "Zoom should multiply by 1.1 on scroll up");
    }

    @Test
    @DisplayName("handleScrollZoom applies inverse factor on scroll down")
    void handleScrollZoom_appliesInverseFactorOnScrollDown() {
        viewModel.setZoomLevel(1.0);
        ScrollEvent event = createScrollEvent(-1.0, 50, 50);

        controller.handleScrollZoom(event);

        assertEquals(1.0 / 1.1, viewModel.zoomLevelProperty().get(), 0.01,
                "Zoom should divide by 1.1 on scroll down");
    }

    // --- handleKeyPress tests ---

    @Test
    @DisplayName("handleKeyPress with ENTER creates a child note")
    void handleKeyPress_enter_createsChildNote() {
        int initialCount = viewModel.getNoteItems().size();
        KeyEvent event = createKeyEvent(KeyCode.ENTER);

        controller.handleKeyPress(event);

        assertEquals(initialCount + 1, viewModel.getNoteItems().size(),
                "ENTER should create a new child note");
    }

    @Test
    @DisplayName("handleKeyPress with ESCAPE navigates back when possible")
    void handleKeyPress_escape_navigatesBack() {
        // First drill down so we can navigate back
        NoteDisplayItem child = viewModel.createChildNote("Child");
        viewModel.drillDown(child.getId());
        assertTrue(viewModel.canNavigateBackProperty().get(),
                "Should be able to navigate back after drill-down");

        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);

        controller.handleKeyPress(event);

        assertFalse(viewModel.canNavigateBackProperty().get(),
                "ESCAPE should navigate back to root");
    }

    @Test
    @DisplayName("handleKeyPress with ESCAPE does nothing at root level")
    void handleKeyPress_escape_doesNothingAtRoot() {
        assertFalse(viewModel.canNavigateBackProperty().get());

        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);
        controller.handleKeyPress(event);

        // Should not throw and state should be unchanged
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("handleKeyPress with unrelated key does nothing")
    void handleKeyPress_unrelatedKey_doesNothing() {
        int initialCount = viewModel.getNoteItems().size();
        KeyEvent event = createKeyEvent(KeyCode.A);

        controller.handleKeyPress(event);

        assertEquals(initialCount, viewModel.getNoteItems().size(),
                "Pressing 'A' should not create notes");
    }

    // --- Helper methods ---

    private ScrollEvent createScrollEvent(double deltaY, double x,
            double y) {
        return new ScrollEvent(
                ScrollEvent.SCROLL,
                x, y, x, y,
                false, false, false, false,
                false, false,
                0, deltaY,
                0, 0,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                0, null);
    }

    private KeyEvent createKeyEvent(KeyCode code) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", code,
                false, false, false, false);
    }
}
