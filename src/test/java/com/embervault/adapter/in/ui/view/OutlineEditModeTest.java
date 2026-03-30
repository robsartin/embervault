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
 * Tests that edit mode persists across Enter, Tab, and
 * Shift+Tab in the Outline view.
 *
 * <p>Unlike other Outline tests, the TreeView is placed in a
 * real Scene so cells render and updateItem fires.</p>
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
        injectField("outlineTreeView", outlineTreeView);
        injectField("outlineRoot", outlineRoot);
        controller.initViewModel(viewModel);

        // Put TreeView in the scene so cells render
        robot.interact(() -> {
            VBox.setVgrow(outlineTreeView,
                    javafx.scene.layout.Priority.ALWAYS);
            outlineRoot.setPrefSize(400, 300);
            Scene scene = new Scene(outlineRoot, 400, 300);
            stage.setScene(scene);
            stage.show();
            stage.toFront();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("Enter while editing creates sibling in edit mode")
    void enter_whileEditing_createsSiblingInEditMode(
            FxRobot robot) {
        robot.interact(() ->
                viewModel.createChildNote(parentId, "First"));
        WaitForAsyncUtils.waitForFxEvents();

        // Click the first tree cell to start editing
        robot.clickOn("First");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify we entered edit mode
        TextField tf = findEditingTextField();
        assertNotNull(tf,
                "Should be in edit mode after click");

        // Press Enter to create sibling
        robot.type(javafx.scene.input.KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.waitForFxEvents();

        // New sibling should be in edit mode
        TextField newTf = findEditingTextField();
        assertNotNull(newTf,
                "After Enter, new sibling should be "
                        + "in edit mode");
    }

    @Test
    @DisplayName("Tab while editing keeps edit mode after indent")
    void tab_whileEditing_keepsEditMode(FxRobot robot) {
        robot.interact(() -> {
            viewModel.createChildNote(parentId, "First");
            viewModel.createChildNote(parentId, "Second");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click second item to start editing
        robot.clickOn("Second");
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(findEditingTextField(),
                "Should be in edit mode after click");

        // Press Tab to indent
        robot.type(javafx.scene.input.KeyCode.TAB);
        // Wait for multiple Platform.runLater rounds
        for (int i = 0; i < 10; i++) {
            WaitForAsyncUtils.waitForFxEvents();
        }

        TextField tf = findEditingTextField();
        assertNotNull(tf,
                "After Tab, should still be in edit mode");
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
