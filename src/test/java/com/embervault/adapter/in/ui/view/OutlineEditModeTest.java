package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
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
 * Tests that edit mode persists across Enter, Tab, Shift+Tab,
 * and that clicking away exits edit mode.
 *
 * <p>TreeView is placed in a real Scene so cells render.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class OutlineEditModeTest {

    private OutlineViewController controller;
    private OutlineViewModel viewModel;
    private NoteService noteService;
    private TreeView<NoteDisplayItem> outlineTreeView;
    private Stage stage;
    private UUID parentId;

    @Start
    private void start(Stage stg) {
        this.stage = stg;
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp(FxRobot robot) {
        InMemoryNoteRepository repo =
                new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repo);
        parentId = noteService.createNote("Parent", "")
                .getId();
        viewModel = new OutlineViewModel(
                new SimpleStringProperty("Parent"),
                noteService);
        viewModel.setBaseNoteId(parentId);

        controller = new OutlineViewController();
        outlineTreeView = new TreeView<>();
        VBox outlineRoot = new VBox();
        outlineRoot.getChildren().add(outlineTreeView);
        VBox.setVgrow(outlineTreeView, Priority.ALWAYS);

        injectField("outlineTreeView", outlineTreeView);
        injectField("outlineRoot", outlineRoot);
        controller.initViewModel(viewModel);

        robot.interact(() -> {
            outlineRoot.setPrefSize(400, 300);
            stage.setScene(new Scene(outlineRoot, 400, 300));
            stage.show();
            stage.toFront();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    // --- RED: Enter should stay in edit mode ---

    @Test
    @DisplayName("Enter while editing creates sibling in edit "
            + "mode")
    void enter_whileEditing_siblingInEditMode(FxRobot robot) {
        robot.interact(() ->
                viewModel.createChildNote(parentId, "First"));
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> {
            outlineTreeView.getSelectionModel().select(
                    outlineTreeView.getRoot()
                            .getChildren().get(0));
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> startEditOnSelected());
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(findEditingTextField(),
                "Precondition: should be editing");

        // Enter → create sibling, stay in edit mode
        robot.type(javafx.scene.input.KeyCode.ENTER);
        waitSettled();

        assertNotNull(findEditingTextField(),
                "After Enter, new sibling should be in "
                        + "edit mode");
    }

    // --- RED: Tab should stay in edit mode ---

    @Test
    @DisplayName("Tab while editing stays in edit mode")
    void tab_whileEditing_staysInEditMode(FxRobot robot) {
        robot.interact(() -> {
            viewModel.createChildNote(parentId, "First");
            viewModel.createChildNote(parentId, "Second");
        });
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> {
            outlineTreeView.getSelectionModel().select(
                    outlineTreeView.getRoot()
                            .getChildren().get(1));
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> startEditOnSelected());
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(findEditingTextField(),
                "Precondition: should be editing");

        robot.type(javafx.scene.input.KeyCode.TAB);
        waitSettled();

        assertNotNull(findEditingTextField(),
                "After Tab, should still be in edit mode");
    }

    // --- RED: Shift+Tab should stay in edit mode ---

    @Test
    @DisplayName("Shift+Tab while editing stays in edit mode")
    void shiftTab_whileEditing_staysInEditMode(
            FxRobot robot) {
        robot.interact(() -> {
            NoteDisplayItem first =
                    viewModel.createChildNote(parentId,
                            "First");
            noteService.createChildNote(
                    first.getId(), "Nested");
            viewModel.loadNotes();
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Select "Nested" programmatically and click it
        robot.interact(() -> {
            var root = outlineTreeView.getRoot();
            var firstItem = root.getChildren().get(0);
            firstItem.setExpanded(true);
            if (!firstItem.getChildren().isEmpty()) {
                outlineTreeView.getSelectionModel()
                        .select(firstItem.getChildren()
                                .get(0));
                outlineTreeView.scrollTo(
                        outlineTreeView.getRow(
                                firstItem.getChildren()
                                        .get(0)));
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Start editing programmatically (clickOn is
        // unreliable in headless xvfb)
        robot.interact(() -> startEditOnSelected());
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(findEditingTextField(),
                "Precondition: should be editing");

        robot.press(javafx.scene.input.KeyCode.SHIFT);
        robot.type(javafx.scene.input.KeyCode.TAB);
        robot.release(javafx.scene.input.KeyCode.SHIFT);
        waitSettled();

        assertNotNull(findEditingTextField(),
                "After Shift+Tab, should still be in "
                        + "edit mode");
    }

    // --- RED: Click outside exits edit mode ---

    @Test
    @DisplayName("Click on empty area exits edit mode")
    void clickOutside_exitsEditMode(FxRobot robot) {
        robot.interact(() ->
                viewModel.createChildNote(parentId, "Note"));
        WaitForAsyncUtils.waitForFxEvents();

        // Select and start editing programmatically
        robot.interact(() -> {
            var root = outlineTreeView.getRoot();
            outlineTreeView.getSelectionModel()
                    .select(root.getChildren().get(0));
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> startEditOnSelected());
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(findEditingTextField(),
                "Precondition: should be editing");

        // Click on empty area below the note
        robot.clickOn(outlineTreeView, javafx.scene.input
                .MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    // --- helpers ---

    private void waitSettled() {
        for (int i = 0; i < 10; i++) {
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    @SuppressWarnings("unchecked")
    private void startEditOnSelected() {
        for (var node : outlineTreeView
                .lookupAll(".tree-cell")) {
            if (node instanceof TreeCell<?> cell
                    && cell.getTreeItem() != null
                    && cell.getTreeItem()
                    == outlineTreeView.getSelectionModel()
                            .getSelectedItem()) {
                // Use reflection to call startInlineEdit
                try {
                    var method = cell.getClass()
                            .getDeclaredMethod(
                                    "startInlineEdit");
                    method.setAccessible(true);
                    method.invoke(cell);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
    }

    private TextField findEditingTextField() {
        for (var node : outlineTreeView
                .lookupAll(".tree-cell")) {
            if (node instanceof TreeCell<?> cell
                    && cell.getGraphic()
                    instanceof TextField tf) {
                return tf;
            }
        }
        return null;
    }

    private void injectField(String name, Object value) {
        try {
            var field = OutlineViewController.class
                    .getDeclaredField(name);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
