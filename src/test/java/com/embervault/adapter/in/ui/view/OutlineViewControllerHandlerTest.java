package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for extracted event handler methods in {@link OutlineViewController}.
 *
 * <p>Verifies that {@code handleTreeKeyPress} and {@code handleTreeSelection}
 * can be called directly without needing a TestFX robot.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class OutlineViewControllerHandlerTest {

    private OutlineViewController controller;
    private OutlineViewModel viewModel;
    private NoteService noteService;
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
        viewModel = new OutlineViewModel(noteTitle, noteService,
                noteService, noteService, noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new com.embervault.adapter.in.ui.viewmodel.EventBus());
        viewModel.setBaseNoteId(parentId);

        controller = new OutlineViewController();
        TreeView<NoteDisplayItem> treeView = new TreeView<>();
        VBox outlineRoot = new VBox(treeView);

        injectField("outlineTreeView", treeView);
        injectField("outlineRoot", outlineRoot);

        controller.initViewModel(viewModel);
    }

    // --- handleTreeKeyPress tests ---

    @Test
    @DisplayName("handleTreeKeyPress with ESCAPE navigates back when possible")
    void handleTreeKeyPress_escape_navigatesBack() {
        NoteDisplayItem child = viewModel.createChildNote(parentId, "Child");
        viewModel.drillDown(child.getId());
        assertTrue(viewModel.canNavigateBackProperty().get(),
                "Should be able to navigate back after drill-down");

        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);

        controller.handleTreeKeyPress(event);

        assertFalse(viewModel.canNavigateBackProperty().get(),
                "ESCAPE should navigate back to root");
    }

    @Test
    @DisplayName("handleTreeKeyPress with ESCAPE does nothing at root level")
    void handleTreeKeyPress_escape_doesNothingAtRoot() {
        assertFalse(viewModel.canNavigateBackProperty().get());

        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);
        controller.handleTreeKeyPress(event);

        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("handleTreeKeyPress with unrelated key does nothing")
    void handleTreeKeyPress_unrelatedKey_doesNothing() {
        KeyEvent event = createKeyEvent(KeyCode.A);

        controller.handleTreeKeyPress(event);

        // Should not throw; no state change
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    // --- handleTreeSelection tests ---

    @Test
    @DisplayName("handleTreeSelection selects note from tree item")
    void handleTreeSelection_selectsNote() {
        NoteDisplayItem item = viewModel.createChildNote(parentId, "Selected");
        TreeItem<NoteDisplayItem> treeItem = new TreeItem<>(item);

        controller.handleTreeSelection(treeItem);

        assertEquals(item.getId(),
                viewModel.selectedNoteIdProperty().get(),
                "Selection should match the tree item's note");
    }

    @Test
    @DisplayName("handleTreeSelection clears selection when tree item is null")
    void handleTreeSelection_nullTreeItem_clearsSelection() {
        NoteDisplayItem item = viewModel.createChildNote(parentId, "Selected");
        viewModel.selectNote(item.getId());

        controller.handleTreeSelection(null);

        assertNull(viewModel.selectedNoteIdProperty().get(),
                "Null tree item should clear selection");
    }

    @Test
    @DisplayName("handleTreeSelection clears selection when tree item value is null")
    void handleTreeSelection_nullValue_clearsSelection() {
        NoteDisplayItem item = viewModel.createChildNote(parentId, "Selected");
        viewModel.selectNote(item.getId());
        TreeItem<NoteDisplayItem> emptyItem = new TreeItem<>(null);

        controller.handleTreeSelection(emptyItem);

        assertNull(viewModel.selectedNoteIdProperty().get(),
                "Null-valued tree item should clear selection");
    }

    // --- Helper methods ---

    private KeyEvent createKeyEvent(KeyCode code) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", code,
                false, false, false, false);
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
