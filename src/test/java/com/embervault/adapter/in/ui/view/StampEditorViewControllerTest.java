package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import com.embervault.adapter.in.ui.viewmodel.StampEditorViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.StampService;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link StampEditorViewController}.
 *
 * <p>Covers create, delete, save, and selection paths.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class StampEditorViewControllerTest {

    private StampEditorViewController controller;
    private StampEditorViewModel viewModel;
    private StampService stampService;
    private ListView<String> stampListView;
    private TextField nameField;
    private TextField actionField;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() throws Exception {
        InMemoryStampRepository stampRepo = new InMemoryStampRepository();
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        stampService = new StampServiceImpl(stampRepo, noteRepo);
        viewModel = new StampEditorViewModel(stampService);

        controller = new StampEditorViewController();
        stampListView = new ListView<>();
        nameField = new TextField();
        actionField = new TextField();
        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");
        Button saveButton = new Button("Save");
        SplitPane editorRoot = new SplitPane();

        inject(controller, "stampListView", stampListView);
        inject(controller, "nameField", nameField);
        inject(controller, "actionField", actionField);
        inject(controller, "addButton", addButton);
        inject(controller, "removeButton", removeButton);
        inject(controller, "saveButton", saveButton);
        inject(controller, "editorRoot", editorRoot);

        controller.initViewModel(viewModel);
    }

    private static void inject(Object target, String fieldName, Object value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // --- create stamp -------------------------------------------------------

    @Test
    @DisplayName("onAddStamp creates a stamp and clears fields")
    void onAddStamp_shouldCreateStamp() {
        nameField.setText("MyStamp");
        actionField.setText("$Color=red");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        assertEquals(1, stampListView.getItems().size());
        assertEquals("MyStamp", stampListView.getItems().get(0));
        assertEquals("", nameField.getText(), "Name field should be cleared");
        assertEquals("", actionField.getText(),
                "Action field should be cleared");
    }

    @Test
    @DisplayName("onAddStamp with blank name does not create stamp")
    void onAddStamp_blankName_shouldNotCreate() {
        nameField.setText("   ");
        actionField.setText("$Color=red");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        assertEquals(0, stampListView.getItems().size());
    }

    @Test
    @DisplayName("onAddStamp with blank action does not create stamp")
    void onAddStamp_blankAction_shouldNotCreate() {
        nameField.setText("MyStamp");
        actionField.setText("");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        assertEquals(0, stampListView.getItems().size());
    }

    // --- delete stamp -------------------------------------------------------

    @Test
    @DisplayName("onRemoveStamp deletes the selected stamp")
    void onRemoveStamp_shouldDeleteStamp() {
        nameField.setText("ToDelete");
        actionField.setText("$Color=blue");
        controller.onAddStamp();
        TestFxHelper.waitForFx();
        assertEquals(1, stampListView.getItems().size());

        // Select the stamp to populate selectedStampId
        stampListView.getSelectionModel().select(0);
        TestFxHelper.waitForFx();

        controller.onRemoveStamp();
        TestFxHelper.waitForFx();

        assertEquals(0, stampListView.getItems().size());
    }

    @Test
    @DisplayName("onRemoveStamp with no selection does nothing")
    void onRemoveStamp_noSelection_shouldDoNothing() {
        nameField.setText("Keep");
        actionField.setText("$Color=green");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        // Do not select — selectedStampId remains null
        controller.onRemoveStamp();
        TestFxHelper.waitForFx();

        assertEquals(1, stampListView.getItems().size(),
                "Stamp should not be removed without selection");
    }

    // --- save stamp (update) ------------------------------------------------

    @Test
    @DisplayName("onSaveStamp with selection updates existing stamp")
    void onSaveStamp_withSelection_shouldUpdate() {
        nameField.setText("Original");
        actionField.setText("$Color=red");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        stampListView.getSelectionModel().select(0);
        TestFxHelper.waitForFx();

        nameField.setText("Updated");
        actionField.setText("$Color=blue");
        controller.onSaveStamp();
        TestFxHelper.waitForFx();

        assertTrue(stampListView.getItems().contains("Updated"),
                "Updated stamp name should appear in list");
    }

    @Test
    @DisplayName("onSaveStamp without selection delegates to onAddStamp")
    void onSaveStamp_noSelection_shouldAdd() {
        nameField.setText("NewStamp");
        actionField.setText("$Checked=true");
        controller.onSaveStamp();
        TestFxHelper.waitForFx();

        assertEquals(1, stampListView.getItems().size());
        assertEquals("NewStamp", stampListView.getItems().get(0));
    }

    // --- selection -----------------------------------------------------------

    @Test
    @DisplayName("selecting a stamp populates name and action fields")
    void selectStamp_shouldPopulateFields() {
        nameField.setText("Stamp1");
        actionField.setText("$Color=red");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        stampListView.getSelectionModel().select(0);
        TestFxHelper.waitForFx();

        assertEquals("Stamp1", nameField.getText());
        assertEquals("$Color=red", actionField.getText());
    }

    @Test
    @DisplayName("deselecting clears selectedStampId gracefully")
    void deselectStamp_shouldHandleNull() throws Exception {
        nameField.setText("Stamp1");
        actionField.setText("$Color=red");
        controller.onAddStamp();
        TestFxHelper.waitForFx();

        stampListView.getSelectionModel().select(0);
        TestFxHelper.waitForFx();

        stampListView.getSelectionModel().clearSelection();
        TestFxHelper.waitForFx();

        // selectedStampId should be null — onRemoveStamp should be a no-op
        controller.onRemoveStamp();
        TestFxHelper.waitForFx();

        assertEquals(1, stampListView.getItems().size(),
                "Stamp should not be removed after deselection");
    }
}
