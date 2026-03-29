package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
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
 * Tests for extracted event handler methods in {@link TreemapViewController}.
 *
 * <p>Verifies that {@code handleCellClick} and {@code handleCellDoubleClick}
 * can be called directly without needing a TestFX robot.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class TreemapViewControllerHandlerTest {

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
        viewModel = new TreemapViewModel(noteTitle, noteService);
        viewModel.setBaseNoteId(parentId);

        controller = new TreemapViewController();
        treemapCanvas = new Pane();

        injectField("treemapCanvas", treemapCanvas);
        controller.initViewModel(viewModel);
    }

    // --- handleCellClick tests ---

    @Test
    @DisplayName("handleCellClick selects the given note")
    void handleCellClick_selectsNote() {
        NoteDisplayItem item = viewModel.createChildNote("Clickable");

        controller.handleCellClick(item);

        assertEquals(item.getId(),
                viewModel.selectedNoteIdProperty().get(),
                "handleCellClick should select the note");
    }

    @Test
    @DisplayName("handleCellClick updates selection when clicking different note")
    void handleCellClick_updatesSelection() {
        NoteDisplayItem first = viewModel.createChildNote("First");
        NoteDisplayItem second = viewModel.createChildNote("Second");

        controller.handleCellClick(first);
        assertEquals(first.getId(),
                viewModel.selectedNoteIdProperty().get());

        controller.handleCellClick(second);
        assertEquals(second.getId(),
                viewModel.selectedNoteIdProperty().get(),
                "Selection should update to second note");
    }

    // --- handleCellDoubleClick tests ---

    @Test
    @DisplayName("handleCellDoubleClick drills down into the note")
    void handleCellDoubleClick_drillsDown() {
        NoteDisplayItem container = viewModel.createChildNote("Container");
        noteService.createChildNote(container.getId(), "Grandchild");

        assertFalse(viewModel.canNavigateBackProperty().get(),
                "Should start at root");

        controller.handleCellDoubleClick(container);

        assertTrue(viewModel.canNavigateBackProperty().get(),
                "handleCellDoubleClick should drill down");
    }

    @Test
    @DisplayName("handleCellDoubleClick changes base to drilled note's children")
    void handleCellDoubleClick_changesBaseNote() {
        NoteDisplayItem container = viewModel.createChildNote("Container");
        noteService.createChildNote(container.getId(), "GrandchildA");
        noteService.createChildNote(container.getId(), "GrandchildB");

        controller.handleCellDoubleClick(container);

        assertEquals(2, viewModel.getNoteItems().size(),
                "After drill-down, should show grandchildren");
    }

    @Test
    @DisplayName("handleCellDoubleClick then navigateBack restores original state")
    void handleCellDoubleClick_thenBack_restoresState() {
        NoteDisplayItem container = viewModel.createChildNote("Container");
        viewModel.createChildNote("Sibling");
        noteService.createChildNote(container.getId(), "Grandchild");

        int originalCount = viewModel.getNoteItems().size();

        controller.handleCellDoubleClick(container);
        viewModel.navigateBack();

        assertEquals(originalCount, viewModel.getNoteItems().size(),
                "After drill-down + back, should restore original notes");
    }

    // --- Helper methods ---

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
