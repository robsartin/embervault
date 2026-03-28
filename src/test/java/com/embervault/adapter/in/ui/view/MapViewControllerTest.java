package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

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
        // The parent has no children initially, so nodeMap should be empty
        // (only the back button is on the canvas)
        assertEquals(1, mapCanvas.getChildren().size(),
                "Only back button should be present with no notes");
    }

    @Test
    @DisplayName("adding a note creates only one new node without rebuilding others")
    void addNote_shouldCreateOnlyNewNode() {
        viewModel.createChildNote("First");
        // Canvas: 1 note node + back button = 2
        assertEquals(2, mapCanvas.getChildren().size());

        StackPane firstNode = findNodeByTitle("First");
        assertNotNull(firstNode, "Should find node for 'First'");

        // Add a second note — the first node instance must be the same object
        viewModel.createChildNote("Second");
        assertEquals(3, mapCanvas.getChildren().size());

        StackPane firstNodeAfterAdd = findNodeByTitle("First");
        assertSame(firstNode, firstNodeAfterAdd,
                "Existing node should not be recreated when a new note is added");
    }

    @Test
    @DisplayName("removing a note removes only that node, leaving others intact")
    void removeNote_shouldRemoveOnlyThatNode() {
        NoteDisplayItem first = viewModel.createChildNote("First");
        viewModel.createChildNote("Second");
        assertEquals(3, mapCanvas.getChildren().size());

        StackPane secondNode = findNodeByTitle("Second");
        assertNotNull(secondNode);

        // Remove the first note via the observable list
        viewModel.getNoteItems().remove(first);
        assertEquals(2, mapCanvas.getChildren().size());

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
        assertEquals(2, mapCanvas.getChildren().size());

        // loadNotes uses setAll which fires a full replacement change
        viewModel.loadNotes();

        assertEquals(2, mapCanvas.getChildren().size(),
                "Should have 1 note node + back button after reload");
        assertNotNull(findNodeByTitle("Before"),
                "Reloaded note should be present");
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
}
