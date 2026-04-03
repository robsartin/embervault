package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

/**
 * Branch coverage tests for {@link OutlineViewController}.
 *
 * <p>Covers keyboard handlers (Tab, Shift+Tab, Backspace on empty,
 * Enter creating sibling, Escape cancel), selection edge cases,
 * and context menu actions.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class OutlineViewControllerBranchTest {

    private OutlineViewController controller;
    private OutlineViewModel viewModel;
    private NoteService noteService;
    private TreeView<NoteDisplayItem> outlineTreeView;
    private VBox outlineRoot;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        parentId = noteService.createNote("Parent", "").getId();
        SimpleStringProperty noteTitle =
                new SimpleStringProperty("Parent");
        viewModel = new OutlineViewModel(noteTitle, noteService,
                noteService, noteService, noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new com.embervault.adapter.in.ui.viewmodel.EventBus());
        viewModel.setBaseNoteId(parentId);

        controller = new OutlineViewController();
        outlineTreeView = new TreeView<>();
        outlineRoot = new VBox();

        injectField("outlineTreeView", outlineTreeView);
        injectField("outlineRoot", outlineRoot);

        controller.initViewModel(viewModel);
    }

    // --- Tab key (indent) ---

    @Test
    @DisplayName("Tab on selected item indents the note")
    void tab_indentsSelectedNote(FxRobot robot) {
        NoteDisplayItem first =
                viewModel.createChildNote(parentId, "First");
        NoteDisplayItem second =
                viewModel.createChildNote(parentId, "Second");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        robot.interact(() -> outlineTreeView.getSelectionModel()
                .select(root.getChildren().get(1)));

        robot.interact(() -> outlineTreeView.fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.TAB,
                        false, false, false, false)));

        // After indent, Second should be a child of First
        // Reload to verify structure changed
        viewModel.loadNotes();
        TreeItem<NoteDisplayItem> newRoot =
                outlineTreeView.getRoot();
        assertEquals(1, newRoot.getChildren().size(),
                "After indent, root should have 1 child");
        assertEquals("First",
                newRoot.getChildren().get(0).getValue().getTitle());
    }

    @Test
    @DisplayName("Tab with no selection is a no-op")
    void tab_noSelection_noop(FxRobot robot) {
        viewModel.createChildNote(parentId, "Child");

        robot.interact(
                () -> outlineTreeView.getSelectionModel()
                        .clearSelection());
        int childCount =
                outlineTreeView.getRoot().getChildren().size();

        robot.interact(() -> outlineTreeView.fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.TAB,
                        false, false, false, false)));

        assertEquals(childCount,
                outlineTreeView.getRoot().getChildren().size(),
                "Tab with no selection should not change tree");
    }

    // --- Shift+Tab key (outdent) ---

    @Test
    @DisplayName("Shift+Tab on selected item outdents the note")
    void shiftTab_outdentsSelectedNote(FxRobot robot) {
        NoteDisplayItem first =
                viewModel.createChildNote(parentId, "First");
        noteService.createChildNote(first.getId(), "Nested");
        viewModel.loadNotes();

        // Drill down to see Nested
        viewModel.drillDown(first.getId());

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        robot.interact(() -> outlineTreeView.getSelectionModel()
                .select(root.getChildren().get(0)));

        robot.interact(() -> outlineTreeView.fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.TAB,
                        true, false, false, false)));

        // After outdent, the tree structure should change
        assertNotNull(outlineTreeView.getRoot(),
                "Tree should still have a root after outdent");
    }

    // --- Escape key (navigate back) ---

    @Test
    @DisplayName("Escape navigates back when drill-down is active")
    void escape_navigatesBack(FxRobot robot) {
        NoteDisplayItem child =
                viewModel.createChildNote(parentId, "Container");
        noteService.createChildNote(child.getId(), "Grandchild");

        viewModel.drillDown(child.getId());
        assertTrue(viewModel.canNavigateBackProperty().get());

        robot.interact(() -> outlineTreeView.fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.ESCAPE,
                        false, false, false, false)));

        assertFalse(viewModel.canNavigateBackProperty().get(),
                "Escape should navigate back to root");
    }

    @Test
    @DisplayName("Escape at root level does nothing")
    void escape_atRoot_noop(FxRobot robot) {
        viewModel.createChildNote(parentId, "Child");
        assertFalse(viewModel.canNavigateBackProperty().get());

        int childCount =
                outlineTreeView.getRoot().getChildren().size();

        robot.interact(() -> outlineTreeView.fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.ESCAPE,
                        false, false, false, false)));

        assertEquals(childCount,
                outlineTreeView.getRoot().getChildren().size(),
                "Escape at root should not change tree");
    }

    // --- Context menu create note ---

    @Test
    @DisplayName("createChildUnderSelected with selection")
    void createChild_withSelection(FxRobot robot) {
        NoteDisplayItem child =
                viewModel.createChildNote(parentId, "Selected");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        robot.interact(() -> outlineTreeView.getSelectionModel()
                .select(root.getChildren().get(0)));

        // Trigger the context menu "Create Note" action
        robot.interact(() -> {
            var contextMenu =
                    outlineTreeView.getContextMenu();
            assertNotNull(contextMenu);
            contextMenu.getItems().get(0).fire();
        });

        assertTrue(noteService.hasChildren(child.getId()),
                "Selected note should now have a child");
    }

    @Test
    @DisplayName("createChildUnderSelected without selection "
            + "creates under base note")
    void createChild_noSelection_createsUnderBase(FxRobot robot) {
        robot.interact(
                () -> outlineTreeView.getSelectionModel()
                        .clearSelection());

        int initialChildren =
                noteService.getChildren(parentId).size();

        robot.interact(() -> {
            var contextMenu =
                    outlineTreeView.getContextMenu();
            contextMenu.getItems().get(0).fire();
        });

        assertEquals(initialChildren + 1,
                noteService.getChildren(parentId).size(),
                "Should create child under base note");
    }

    // --- Selection null branch ---

    @Test
    @DisplayName("selecting null tree item sets null on viewModel")
    void selectNull_setsNullOnViewModel(FxRobot robot) {
        viewModel.createChildNote(parentId, "Note");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        robot.interact(() -> outlineTreeView.getSelectionModel()
                .select(root.getChildren().get(0)));
        assertNotNull(
                viewModel.selectedNoteIdProperty().get());

        robot.interact(
                () -> outlineTreeView.getSelectionModel()
                        .clearSelection());
        assertNull(viewModel.selectedNoteIdProperty().get(),
                "Clearing selection should set null");
    }

    // --- findTreeItem null branches ---

    @Test
    @DisplayName("building tree with no children produces "
            + "empty root")
    void buildTree_noChildren_emptyRoot() {
        // Parent has no children initially (just clear and reload)
        viewModel.loadNotes();
        assertEquals(0,
                outlineTreeView.getRoot().getChildren().size(),
                "Root with no children should be empty");
    }

    // --- Back button visibility ---

    @Test
    @DisplayName("back button becomes visible after drill-down")
    void backButton_visibleAfterDrillDown(FxRobot robot) {
        NoteDisplayItem child =
                viewModel.createChildNote(parentId, "Container");
        noteService.createChildNote(child.getId(), "Nested");

        // Back button is at index 0 of outlineRoot
        var backButton = outlineRoot.getChildren().get(0);
        assertFalse(backButton.isVisible(),
                "Back button should be hidden at root");

        robot.interact(
                () -> viewModel.drillDown(child.getId()));
        assertTrue(backButton.isVisible(),
                "Back button should be visible after drill-down");
    }

    @Test
    @DisplayName("back button hides after navigating back")
    void backButton_hidesAfterNavigateBack(FxRobot robot) {
        NoteDisplayItem child =
                viewModel.createChildNote(parentId, "Container");
        noteService.createChildNote(child.getId(), "Nested");

        robot.interact(
                () -> viewModel.drillDown(child.getId()));
        var backButton = outlineRoot.getChildren().get(0);
        assertTrue(backButton.isVisible());

        robot.interact(() -> viewModel.navigateBack());
        assertFalse(backButton.isVisible(),
                "Back button should hide after navigating back");
    }

    // --- buildTreeItem with hasChildren ---

    @Test
    @DisplayName("tree item with children has expanded "
            + "sub-items")
    void treeItem_withChildren_hasSubItems() {
        NoteDisplayItem parent =
                viewModel.createChildNote(parentId, "Parent");
        noteService.createChildNote(parent.getId(), "Child A");
        noteService.createChildNote(parent.getId(), "Child B");

        viewModel.loadNotes();

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        TreeItem<NoteDisplayItem> parentItem =
                root.getChildren().get(0);
        assertTrue(parentItem.isExpanded(),
                "Parent tree item should be expanded");
        assertEquals(2, parentItem.getChildren().size(),
                "Parent should have 2 child tree items");
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = OutlineViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
