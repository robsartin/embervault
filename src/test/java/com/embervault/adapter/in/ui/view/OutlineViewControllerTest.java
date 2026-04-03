package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link OutlineViewController}.
 *
 * <p>Verifies tree building from notes, selection propagation to the ViewModel,
 * drill-down and back navigation, and child note creation.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class OutlineViewControllerTest {

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
        SimpleStringProperty noteTitle = new SimpleStringProperty("Parent");
        viewModel = new OutlineViewModel(
                noteTitle, noteService, noteService,
                noteService, noteService, noteService,
                noteService, new AppState());
        viewModel.setBaseNoteId(parentId);

        controller = new OutlineViewController();
        outlineTreeView = new TreeView<>();
        outlineRoot = new VBox();

        injectField("outlineTreeView", outlineTreeView);
        injectField("outlineRoot", outlineRoot);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("tree is built with root items after initViewModel")
    void initViewModel_buildsTree() {
        assertNotNull(outlineTreeView.getRoot(),
                "Tree should have a root after init");
        assertFalse(outlineTreeView.isShowRoot(),
                "Root should be hidden");
    }

    @Test
    @DisplayName("tree contains children after adding notes")
    void addNotes_treeContainsChildren() {
        viewModel.createChildNote(parentId, "Child A");
        viewModel.createChildNote(parentId, "Child B");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        assertEquals(2, root.getChildren().size(),
                "Tree root should have 2 children");
    }

    @Test
    @DisplayName("tree item titles match note titles")
    void treeItems_matchNoteTitles() {
        viewModel.createChildNote(parentId, "First Note");
        viewModel.createChildNote(parentId, "Second Note");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        assertEquals("First Note",
                root.getChildren().get(0).getValue().getTitle());
        assertEquals("Second Note",
                root.getChildren().get(1).getValue().getTitle());
    }

    @Test
    @DisplayName("selecting a tree item sets viewModel selectedNoteId")
    void selectTreeItem_setsViewModelSelection() {
        NoteDisplayItem child = viewModel.createChildNote(parentId,
                "Selectable");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        outlineTreeView.getSelectionModel().select(
                root.getChildren().get(0));

        assertEquals(child.getId(),
                viewModel.selectedNoteIdProperty().get(),
                "ViewModel should reflect tree selection");
    }

    @Test
    @DisplayName("clearing selection sets viewModel selectedNoteId to null")
    void clearSelection_setsViewModelNull() {
        viewModel.createChildNote(parentId, "Temp");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        outlineTreeView.getSelectionModel().select(
                root.getChildren().get(0));
        assertNotNull(viewModel.selectedNoteIdProperty().get());

        outlineTreeView.getSelectionModel().clearSelection();
        assertNull(viewModel.selectedNoteIdProperty().get(),
                "ViewModel selection should be null after clear");
    }

    @Test
    @DisplayName("drill-down rebuilds tree with child's children")
    void drillDown_rebuildsTree() {
        NoteDisplayItem child = viewModel.createChildNote(parentId,
                "Container");
        noteService.createChildNote(child.getId(), "Grandchild A");
        noteService.createChildNote(child.getId(), "Grandchild B");

        viewModel.drillDown(child.getId());

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        assertEquals(2, root.getChildren().size(),
                "After drill-down, tree should show grandchildren");
        assertEquals("Grandchild A",
                root.getChildren().get(0).getValue().getTitle());
    }

    @Test
    @DisplayName("back button is inserted into outlineRoot")
    void backButton_insertedIntoRoot() {
        // The back button is added at index 0
        assertTrue(outlineRoot.getChildren().size() >= 1,
                "outlineRoot should contain back button");
    }

    @Test
    @DisplayName("canNavigateBack is false at root level")
    void canNavigateBack_falseAtRoot() {
        assertFalse(viewModel.canNavigateBackProperty().get(),
                "Should not be able to navigate back at root");
    }

    @Test
    @DisplayName("canNavigateBack is true after drill-down")
    void canNavigateBack_trueAfterDrillDown() {
        NoteDisplayItem child = viewModel.createChildNote(parentId,
                "Container");

        viewModel.drillDown(child.getId());

        assertTrue(viewModel.canNavigateBackProperty().get(),
                "Should be able to navigate back after drill-down");
    }

    @Test
    @DisplayName("navigateBack restores parent's children")
    void navigateBack_restoresParentChildren() {
        NoteDisplayItem child = viewModel.createChildNote(parentId,
                "Container");
        viewModel.createChildNote(parentId, "Sibling");
        noteService.createChildNote(child.getId(), "Grandchild");

        viewModel.drillDown(child.getId());
        assertEquals(1, outlineTreeView.getRoot().getChildren().size());

        viewModel.navigateBack();

        assertEquals(2, outlineTreeView.getRoot().getChildren().size(),
                "After navigating back, should show parent's children");
    }

    @Test
    @DisplayName("renaming a note updates the tree item title")
    void renameNote_updatesTreeItem() {
        NoteDisplayItem child = viewModel.createChildNote(parentId,
                "Original");

        viewModel.renameNote(child.getId(), "Renamed");

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        assertEquals("Renamed",
                root.getChildren().get(0).getValue().getTitle(),
                "Tree item should show renamed title");
    }

    @Test
    @DisplayName("getViewModel returns the injected viewModel")
    void getViewModel_returnsInjectedViewModel() {
        assertEquals(viewModel, controller.getViewModel(),
                "getViewModel should return the initialized ViewModel");
    }

    @Test
    @DisplayName("nested children appear in tree hierarchy")
    void nestedChildren_appearInHierarchy() {
        NoteDisplayItem child = viewModel.createChildNote(parentId,
                "Parent Note");
        noteService.createChildNote(child.getId(), "Nested Child");

        // Reload to pick up the nested child
        viewModel.loadNotes();

        TreeItem<NoteDisplayItem> root = outlineTreeView.getRoot();
        TreeItem<NoteDisplayItem> parentItem = root.getChildren().get(0);
        assertTrue(parentItem.getValue().isHasChildren(),
                "Parent note should be marked as having children");
        assertEquals(1, parentItem.getChildren().size(),
                "Parent tree item should have nested child");
        assertEquals("Nested Child",
                parentItem.getChildren().get(0).getValue().getTitle());
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
