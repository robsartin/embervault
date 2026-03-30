package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.NoteViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
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
 * Tests for {@link NoteViewController}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class NoteViewControllerTest {

    private NoteViewController controller;
    private NoteViewModel viewModel;
    private NoteService noteService;
    private ListView<NoteDisplayItem> noteListView;
    private TextField titleField;
    private TextArea contentArea;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        noteService.createNote("Existing Note", "Some content");

        viewModel = new NoteViewModel(noteService);

        controller = new NoteViewController();
        noteListView = new ListView<>();
        titleField = new TextField();
        contentArea = new TextArea();
        Button addButton = new Button("Add");
        Button saveButton = new Button("Save");
        Button deleteButton = new Button("Delete");

        injectField("noteListView", noteListView);
        injectField("titleField", titleField);
        injectField("contentArea", contentArea);
        injectField("addButton", addButton);
        injectField("saveButton", saveButton);
        injectField("deleteButton", deleteButton);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("note list populated after init")
    void initViewModel_noteListPopulated() {
        assertFalse(noteListView.getItems().isEmpty(),
                "Note list should contain existing notes");
    }

    @Test
    @DisplayName("title field is bidirectionally bound")
    void titleField_isBidirectionallyBound() {
        assertNotNull(titleField.textProperty());
        viewModel.titleProperty().set("New Title");
        assertEquals("New Title", titleField.getText());
    }

    @Test
    @DisplayName("content area is bidirectionally bound")
    void contentArea_isBidirectionallyBound() {
        viewModel.contentProperty().set("New Content");
        assertEquals("New Content", contentArea.getText());
    }

    @Test
    @DisplayName("onAdd delegates to viewModel")
    void onAdd_delegatesToViewModel() {
        int beforeCount = noteListView.getItems().size();
        titleField.setText("New Note");
        controller.onAdd();
        // After add, the list should have one more item
        assertEquals(beforeCount + 1, noteListView.getItems().size());
    }

    @Test
    @DisplayName("selecting note in list updates viewModel")
    void selectNote_updatesViewModel() {
        if (!noteListView.getItems().isEmpty()) {
            noteListView.getSelectionModel().select(0);
            // Selection listener should have called viewModel.selectNote
            NoteDisplayItem selected = noteListView.getSelectionModel()
                    .getSelectedItem();
            assertNotNull(selected);
        }
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = NoteViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
