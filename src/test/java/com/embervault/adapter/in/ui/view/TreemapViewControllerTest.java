package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link TreemapViewController}.
 *
 * <p>Verifies layout rendering, note node creation, click-to-select,
 * drill-down navigation, and back button behavior.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class TreemapViewControllerTest {

    private TreemapViewController controller;
    private TreemapViewModel viewModel;
    private NoteService noteService;
    private Pane treemapCanvas;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        parentId = noteService.createNote("Parent", "").getId();
        SimpleStringProperty noteTitle = new SimpleStringProperty("Parent");
        viewModel = new TreemapViewModel(noteTitle, noteService,
                noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new com.embervault.adapter.in.ui.viewmodel.EventBus());
        viewModel.setBaseNoteId(parentId);

        controller = new TreemapViewController();
        treemapCanvas = new Pane();

        injectField("treemapCanvas", treemapCanvas);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("canvas contains only back button when no children exist")
    void noChildren_canvasHasBackButtonOnly() {
        // With zero-size canvas, only back button is added
        assertEquals(1, treemapCanvas.getChildren().size(),
                "Canvas should contain only back button when no children");
    }

    @Test
    @DisplayName("adding notes creates note nodes on canvas after resize")
    void addNotes_createsNodesAfterResize() {
        viewModel.createChildNote("Note A");
        viewModel.createChildNote("Note B");

        // Simulate canvas resize to trigger layout
        resizeCanvas(400, 300);

        // Count StackPane children (note nodes) — back button is a Button
        long noteNodes = treemapCanvas.getChildren().stream()
                .filter(n -> n instanceof StackPane)
                .count();
        assertEquals(2, noteNodes,
                "Canvas should have 2 note nodes after adding 2 notes");
    }

    @Test
    @DisplayName("note nodes have userData set to note UUID")
    void noteNodes_haveUserData() {
        NoteDisplayItem item = viewModel.createChildNote("Tagged Note");
        resizeCanvas(400, 300);

        StackPane noteNode = findNodeByUserData(item.getId());
        assertNotNull(noteNode,
                "Should find note node by its UUID userData");
    }

    @Test
    @DisplayName("clicking a note node sets viewModel selectedNoteId")
    void clickNote_setsSelection() {
        NoteDisplayItem item = viewModel.createChildNote("Clickable");
        resizeCanvas(400, 300);

        StackPane noteNode = findNodeByUserData(item.getId());
        assertNotNull(noteNode);

        // Simulate mouse press which triggers selection
        noteNode.fireEvent(new javafx.scene.input.MouseEvent(
                javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                0, 0, 0, 0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false,
                false, false, false, null));

        assertEquals(item.getId(),
                viewModel.selectedNoteIdProperty().get(),
                "ViewModel should reflect clicked note");
    }

    @Test
    @DisplayName("selected note has highlight border")
    void selectedNote_hasHighlightBorder() {
        NoteDisplayItem item = viewModel.createChildNote("Highlighted");
        viewModel.selectNote(item.getId());
        resizeCanvas(400, 300);

        StackPane noteNode = findNodeByUserData(item.getId());
        assertNotNull(noteNode);

        // The first child of the StackPane is the Rectangle
        Rectangle rect = (Rectangle) noteNode.getChildren().get(0);
        assertEquals(3.0, rect.getStrokeWidth(),
                "Selected note should have thicker border");
    }

    @Test
    @DisplayName("drill-down replaces canvas with child's children")
    void drillDown_replacesCanvasContent() {
        NoteDisplayItem container = viewModel.createChildNote("Container");
        noteService.createChildNote(container.getId(), "GrandchildA");
        noteService.createChildNote(container.getId(), "GrandchildB");

        viewModel.drillDown(container.getId());
        resizeCanvas(400, 300);

        long noteNodes = treemapCanvas.getChildren().stream()
                .filter(n -> n instanceof StackPane)
                .count();
        assertEquals(2, noteNodes,
                "After drill-down, canvas should show grandchildren");
    }

    @Test
    @DisplayName("canNavigateBack is false at root, true after drill-down")
    void canNavigateBack_togglesOnDrillDown() {
        assertFalse(viewModel.canNavigateBackProperty().get());

        NoteDisplayItem child = viewModel.createChildNote("Container");
        viewModel.drillDown(child.getId());

        assertTrue(viewModel.canNavigateBackProperty().get(),
                "Should be able to navigate back after drill-down");
    }

    @Test
    @DisplayName("navigateBack restores parent's notes on canvas")
    void navigateBack_restoresParentNotes() {
        NoteDisplayItem container = viewModel.createChildNote("Container");
        viewModel.createChildNote("Sibling");
        noteService.createChildNote(container.getId(), "Grandchild");

        viewModel.drillDown(container.getId());
        resizeCanvas(400, 300);

        long afterDrillDown = treemapCanvas.getChildren().stream()
                .filter(n -> n instanceof StackPane)
                .count();
        assertEquals(1, afterDrillDown);

        viewModel.navigateBack();
        resizeCanvas(400, 300);

        long afterBack = treemapCanvas.getChildren().stream()
                .filter(n -> n instanceof StackPane)
                .count();
        assertEquals(2, afterBack,
                "After back, canvas should show original children");
    }

    @Test
    @DisplayName("canvas is focusable for keyboard events")
    void canvas_isFocusTraversable() {
        assertTrue(treemapCanvas.isFocusTraversable(),
                "Canvas should be focus-traversable for keyboard events");
    }

    @Test
    @DisplayName("getViewModel returns the injected viewModel")
    void getViewModel_returnsInjectedViewModel() {
        assertEquals(viewModel, controller.getViewModel(),
                "getViewModel should return the initialized ViewModel");
    }

    /**
     * Simulates a canvas resize which triggers re-rendering.
     */
    private void resizeCanvas(double width, double height) {
        treemapCanvas.resize(width, height);
        // Force the width/height properties to fire listeners
        treemapCanvas.setMinSize(width, height);
        treemapCanvas.setPrefSize(width, height);
        treemapCanvas.setMaxSize(width, height);
    }

    private StackPane findNodeByUserData(UUID id) {
        for (Node child : treemapCanvas.getChildren()) {
            if (child instanceof StackPane sp
                    && id.equals(sp.getUserData())) {
                return sp;
            }
        }
        return null;
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = TreemapViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
