package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class StampEditorViewControllerTest {

    private StampEditorViewController controller;
    private StampEditorViewModel viewModel;
    private TextField nameField;
    private TextField actionField;
    private ListView<String> stampListView;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryStampRepository stampRepo = new InMemoryStampRepository();
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        StampService stampService = new StampServiceImpl(stampRepo, noteRepo);
        viewModel = new StampEditorViewModel(stampService);

        controller = new StampEditorViewController();
        nameField = new TextField();
        actionField = new TextField();
        stampListView = new ListView<>();
        SplitPane editorRoot = new SplitPane();
        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");
        Button saveButton = new Button("Save");

        injectField("nameField", nameField);
        injectField("actionField", actionField);
        injectField("stampListView", stampListView);
        injectField("editorRoot", editorRoot);
        injectField("addButton", addButton);
        injectField("removeButton", removeButton);
        injectField("saveButton", saveButton);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("stamp list is initially empty")
    void initViewModel_stampListEmpty() {
        assertTrue(stampListView.getItems().isEmpty(),
                "Stamp list should start empty");
    }

    @Test
    @DisplayName("onAddStamp creates stamp and clears fields")
    void onAddStamp_createsAndClears() {
        nameField.setText("Priority");
        actionField.setText("$Priority=high");
        controller.onAddStamp();

        assertFalse(stampListView.getItems().isEmpty(),
                "Stamp list should contain the new stamp");
        assertEquals("", nameField.getText(),
                "Name field should be cleared");
        assertEquals("", actionField.getText(),
                "Action field should be cleared");
    }

    @Test
    @DisplayName("onRemoveStamp removes selected stamp")
    void onRemoveStamp_removesSelected() {
        nameField.setText("ToRemove");
        actionField.setText("$Color=red");
        controller.onAddStamp();
        int countAfterAdd = stampListView.getItems().size();

        // Select the stamp in the list
        stampListView.getSelectionModel().select(0);

        controller.onRemoveStamp();
        assertEquals(countAfterAdd - 1, stampListView.getItems().size(),
                "Stamp should be removed");
    }

    @Test
    @DisplayName("onRemoveStamp does nothing when nothing selected")
    void onRemoveStamp_nothingSelectedDoesNothing() {
        nameField.setText("Keep");
        actionField.setText("$Color=blue");
        controller.onAddStamp();
        int count = stampListView.getItems().size();

        // No selection — selectedStampId is null
        controller.onRemoveStamp();
        assertEquals(count, stampListView.getItems().size(),
                "Nothing should be removed");
    }

    @Test
    @DisplayName("onSaveStamp delegates to onAddStamp when no selection")
    void onSaveStamp_delegatesToAddWhenNoSelection() {
        nameField.setText("NewStamp");
        actionField.setText("$Checked=true");
        controller.onSaveStamp();

        assertFalse(stampListView.getItems().isEmpty(),
                "Stamp should be created via save");
    }

    @Test
    @DisplayName("selecting stamp populates name and action fields")
    void selectStamp_populatesFields() {
        nameField.setText("MyStamp");
        actionField.setText("$Color=green");
        controller.onAddStamp();

        stampListView.getSelectionModel().select(0);

        assertEquals("MyStamp", nameField.getText());
        assertEquals("$Color=green", actionField.getText());
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = StampEditorViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
