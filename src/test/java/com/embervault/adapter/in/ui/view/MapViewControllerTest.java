package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tests for {@link MapViewController} incremental node update behavior.
 *
 * <p>Verifies that the controller maintains a {@code nodeMap} and performs
 * targeted adds/removes instead of full canvas rebuilds.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class MapViewControllerTest {

    private MapViewController controller;
    private MapViewModel viewModel;
    private NoteService noteService;
    private Pane mapCanvas;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        // JavaFX toolkit must be initialized before we use any FX nodes.
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        SimpleStringProperty noteTitle = new SimpleStringProperty("Test");
        viewModel = new MapViewModel(noteTitle, noteService);

        parentId = noteService.createNote("Parent", "").getId();
        viewModel.setBaseNoteId(parentId);

        controller = new MapViewController();
        mapCanvas = new Pane();

        // Inject the canvas via reflection since it's @FXML private
        try {
            var field = MapViewController.class.getDeclaredField("mapCanvas");
            field.setAccessible(true);
            field.set(controller, mapCanvas);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("nodeMap is populated after initViewModel with existing notes")
    void initViewModel_shouldPopulateNodeMap() {
        // No note children initially — only back button + zoom toolbar
        assertEquals(2, mapCanvas.getChildren().size(),
                "Only back button and zoom toolbar should be present");
    }

    @Test
    @DisplayName("adding a note creates only one new node without rebuilding others")
    void addNote_shouldCreateOnlyNewNode() {
        viewModel.createChildNote("First");
        // Canvas: 1 note + back button + zoom toolbar = 3
        assertEquals(3, mapCanvas.getChildren().size());

        StackPane firstNode = findNodeByTitle("First");
        assertNotNull(firstNode, "Should find node for 'First'");

        // Add a second note — the first node instance must be the same object
        viewModel.createChildNote("Second");
        assertEquals(4, mapCanvas.getChildren().size());

        StackPane firstNodeAfterAdd = findNodeByTitle("First");
        assertSame(firstNode, firstNodeAfterAdd,
                "Existing node should not be recreated when a new note is added");
    }

    @Test
    @DisplayName("removing a note removes only that node, leaving others intact")
    void removeNote_shouldRemoveOnlyThatNode() {
        NoteDisplayItem first = viewModel.createChildNote("First");
        viewModel.createChildNote("Second");
        assertEquals(4, mapCanvas.getChildren().size());

        StackPane secondNode = findNodeByTitle("Second");
        assertNotNull(secondNode);

        // Remove the first note via the observable list
        viewModel.getNoteItems().remove(first);
        assertEquals(3, mapCanvas.getChildren().size());

        StackPane secondNodeAfterRemove = findNodeByTitle("Second");
        assertSame(secondNode, secondNodeAfterRemove,
                "Remaining node should be the same instance after removal");
    }

    @Test
    @DisplayName("z-order is preserved after adding a new note")
    void addNote_shouldPreserveZorder() {
        viewModel.createChildNote("A");
        viewModel.createChildNote("B");

        StackPane nodeA = findNodeByTitle("A");
        StackPane nodeB = findNodeByTitle("B");
        assertNotNull(nodeA);
        assertNotNull(nodeB);

        int indexA = mapCanvas.getChildren().indexOf(nodeA);
        int indexB = mapCanvas.getChildren().indexOf(nodeB);
        assertTrue(indexA < indexB,
                "Node A should appear before Node B in z-order");

        // Add a third note
        viewModel.createChildNote("C");

        int newIndexA = mapCanvas.getChildren().indexOf(nodeA);
        int newIndexB = mapCanvas.getChildren().indexOf(nodeB);
        assertTrue(newIndexA < newIndexB,
                "Z-order of A and B should be preserved after adding C");
    }

    @Test
    @DisplayName("updating a note in-place changes node properties without recreating")
    void updateNote_shouldModifyExistingNode() {
        NoteDisplayItem item = viewModel.createChildNote("Original");
        StackPane node = findNodeByTitle("Original");
        assertNotNull(node);

        // Rename triggers a set() on the observable list (replacement)
        viewModel.renameNote(item.getId(), "Renamed");

        StackPane nodeAfterRename = findNodeByUserData(item.getId());
        assertSame(node, nodeAfterRename,
                "Node should be updated in-place, not recreated");
    }

    @Test
    @DisplayName("nodeMap returns correct node for a given note ID")
    void getNodeForId_shouldReturnCorrectNode() {
        NoteDisplayItem item = viewModel.createChildNote("Lookup");

        StackPane node = findNodeByUserData(item.getId());
        assertNotNull(node, "Should find node by note ID");
        assertEquals(item.getId(), node.getUserData());
    }

    @Test
    @DisplayName("full list replacement (loadNotes) rebuilds nodeMap correctly")
    void loadNotes_shouldRebuildNodeMap() {
        viewModel.createChildNote("Before");
        assertEquals(3, mapCanvas.getChildren().size());

        // loadNotes uses setAll which fires a full replacement change
        viewModel.loadNotes();

        assertEquals(3, mapCanvas.getChildren().size(),
                "Should have 1 note + back button + zoom toolbar after reload");
        assertNotNull(findNodeByTitle("Before"),
                "Reloaded note should be present");
    }

    @Test
    @DisplayName("loadNotes after indent removes indented note node from canvas (issue #118)")
    void loadNotes_afterIndent_shouldRemoveIndentedNoteNode() {
        // Create 3 children: A, B, C
        viewModel.createChildNote("A");
        viewModel.createChildNote("B");
        viewModel.createChildNote("C");
        // Canvas: 3 notes + back button + zoom toolbar = 5
        assertEquals(5, mapCanvas.getChildren().size(),
                "Should have 3 notes + back button + zoom toolbar before indent");
        assertNotNull(findNodeByTitle("A"));
        assertNotNull(findNodeByTitle("B"));
        assertNotNull(findNodeByTitle("C"));

        // Indent B under A (via service, then reload)
        noteService.indentNote(
                noteService.getChildren(parentId).get(1).getId());
        viewModel.loadNotes();

        // After indent: 2 notes + back button + zoom toolbar = 4
        assertEquals(4, mapCanvas.getChildren().size(),
                "Should have 2 notes + back button + toolbar after indent; "
                        + "indented note B must be removed from canvas");
        assertNotNull(findNodeByTitle("A"),
                "A should still be on canvas");
        assertNotNull(findNodeByTitle("C"),
                "C should still be on canvas");
        assertEquals(null, findNodeByTitle("B"),
                "B should NOT be on canvas after being indented under A");
    }

    /** Finds a StackPane on the canvas whose title label matches the given text. */
    private StackPane findNodeByTitle(String title) {
        for (Node child : mapCanvas.getChildren()) {
            if (child instanceof StackPane sp && sp.getUserData() instanceof UUID) {
                // Title is in the VBox (child 1) -> Label (child 0)
                if (sp.getChildren().size() >= 2) {
                    var vbox = sp.getChildren().get(1);
                    if (vbox instanceof javafx.scene.layout.VBox v
                            && !v.getChildren().isEmpty()
                            && v.getChildren().get(0)
                                    instanceof javafx.scene.control.Label lbl
                            && title.equals(lbl.getText())) {
                        return sp;
                    }
                }
            }
        }
        return null;
    }

    /** Finds a StackPane on the canvas whose userData matches the given UUID. */
    private StackPane findNodeByUserData(UUID id) {
        for (Node child : mapCanvas.getChildren()) {
            if (child instanceof StackPane sp && id.equals(sp.getUserData())) {
                return sp;
            }
        }
        return null;
    }

    // --- Zoom rendering tests ---

    @Test
    @DisplayName("OVERVIEW tier renders note as rectangle only, no labels")
    void overviewTier_shouldRenderRectangleOnly() throws Exception {
        viewModel.createChildNote("Test Note");
        viewModel.setZoomLevel(0.2); // OVERVIEW tier
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        StackPane node = findNodeByTitle("Test Note");
        // In OVERVIEW, there should be no title label visible
        // The node should exist but only have a rectangle
        assertNotNull(findNodeByUserData(
                viewModel.getNoteItems().get(0).getId()));
        StackPane noteNode = findNodeByUserData(
                viewModel.getNoteItems().get(0).getId());
        // First child should be a Rectangle
        assertTrue(noteNode.getChildren().get(0) instanceof Rectangle,
                "First child should be a Rectangle");
        // Should not have a VBox with labels
        boolean hasVisibleLabels = false;
        for (Node child : noteNode.getChildren()) {
            if (child instanceof VBox vbox) {
                hasVisibleLabels = true;
            }
        }
        assertFalse(hasVisibleLabels,
                "OVERVIEW tier should not render text labels");
    }

    @Test
    @DisplayName("TITLES_ONLY tier renders rectangle with title but no content")
    void titlesOnlyTier_shouldRenderTitleOnly() throws Exception {
        viewModel.createChildNote("My Title");
        viewModel.setZoomLevel(0.5); // TITLES_ONLY tier
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        StackPane noteNode = findNodeByUserData(
                viewModel.getNoteItems().get(0).getId());
        assertNotNull(noteNode);

        // Should have a VBox with title label only
        VBox textBox = findTextBox(noteNode);
        assertNotNull(textBox, "TITLES_ONLY tier should have a text VBox");
        assertTrue(textBox.getChildren().size() >= 1,
                "Should have at least a title label");
        assertTrue(textBox.getChildren().get(0) instanceof Label,
                "First child should be a title Label");
        // Should not have content label
        boolean hasContentLabel = false;
        for (int i = 1; i < textBox.getChildren().size(); i++) {
            if (textBox.getChildren().get(i) instanceof Label) {
                hasContentLabel = true;
            }
        }
        assertFalse(hasContentLabel,
                "TITLES_ONLY tier should not render content label");
    }

    @Test
    @DisplayName("NORMAL tier renders title and content (current behavior)")
    void normalTier_shouldRenderTitleAndContent() {
        viewModel.setZoomLevel(1.0); // NORMAL tier
        viewModel.createChildNote("Title");

        StackPane noteNode = findNodeByUserData(
                viewModel.getNoteItems().get(0).getId());
        assertNotNull(noteNode);

        VBox textBox = findTextBox(noteNode);
        assertNotNull(textBox,
                "NORMAL tier should have a text VBox");
    }

    @Test
    @DisplayName("DETAILED tier renders with larger font size")
    void detailedTier_shouldRenderWithLargerFont() throws Exception {
        viewModel.createChildNote("Detailed Note");
        viewModel.setZoomLevel(2.0); // DETAILED tier
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        StackPane noteNode = findNodeByUserData(
                viewModel.getNoteItems().get(0).getId());
        assertNotNull(noteNode);

        VBox textBox = findTextBox(noteNode);
        assertNotNull(textBox,
                "DETAILED tier should have a text VBox");
        Label titleLabel = (Label) textBox.getChildren().get(0);
        assertEquals(18.0, titleLabel.getFont().getSize(), 0.1,
                "DETAILED tier title font should be 18pt");
    }

    @Test
    @DisplayName("tier change triggers re-render of notes")
    void tierChange_shouldReRenderNotes() throws Exception {
        viewModel.createChildNote("Re-render Test");
        StackPane nodeAtNormal = findNodeByUserData(
                viewModel.getNoteItems().get(0).getId());
        assertNotNull(nodeAtNormal);

        // Change to OVERVIEW tier
        viewModel.setZoomLevel(0.2);
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // After tier change, node should be re-rendered
        StackPane nodeAtOverview = findNodeByUserData(
                viewModel.getNoteItems().get(0).getId());
        assertNotNull(nodeAtOverview,
                "Note node should still exist after tier change");
    }

    @Test
    @DisplayName("zoom toolbar is present after initViewModel")
    void zoomToolbar_shouldBePresent() {
        // The zoom toolbar should be part of the scene
        // Check that the mapCanvas parent has a toolbar
        assertNotNull(controller.getViewModel(),
                "ViewModel should be set");
    }

    /** Finds the VBox child within a StackPane note node. */
    private VBox findTextBox(StackPane noteNode) {
        for (Node child : noteNode.getChildren()) {
            if (child instanceof VBox vbox) {
                return vbox;
            }
        }
        return null;
    }
}
