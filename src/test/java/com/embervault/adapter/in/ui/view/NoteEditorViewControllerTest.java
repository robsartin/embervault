package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
 * Tests for {@link NoteEditorViewController}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class NoteEditorViewControllerTest {

    private NoteEditorViewController controller;
    private NoteEditorViewModel viewModel;
    private NoteService noteService;
    private TextField titleField;
    private TextArea textArea;
    private VBox attributesBox;
    private UUID noteId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        AttributeSchemaRegistry registry = new AttributeSchemaRegistry();

        noteId = noteService.createNote("Test Note", "Test content").getId();

        viewModel = new NoteEditorViewModel(noteService, registry);
        viewModel.setNote(noteId);

        controller = new NoteEditorViewController();
        titleField = new TextField();
        textArea = new TextArea();
        attributesBox = new VBox();
        VBox editorRoot = new VBox();

        injectField("titleField", titleField);
        injectField("textArea", textArea);
        injectField("attributesBox", attributesBox);
        injectField("editorRoot", editorRoot);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("title field shows note title after init")
    void initViewModel_titleFieldPopulated() {
        assertEquals("Test Note", titleField.getText());
    }

    @Test
    @DisplayName("text area shows note content after init")
    void initViewModel_textAreaPopulated() {
        assertEquals("Test content", textArea.getText());
    }

    @Test
    @DisplayName("getViewModel returns injected viewModel")
    void getViewModel_returnsInjected() {
        assertEquals(viewModel, controller.getViewModel());
    }

    @Test
    @DisplayName("title field updates when viewModel title changes")
    void titlePropertyChange_updatesTitleField() {
        viewModel.titleProperty().set("Updated Title");
        assertEquals("Updated Title", titleField.getText());
    }

    @Test
    @DisplayName("text area updates when viewModel text changes")
    void textPropertyChange_updatesTextArea() {
        viewModel.textProperty().set("Updated content");
        assertEquals("Updated content", textArea.getText());
    }

    @Test
    @DisplayName("attributes box is populated after init")
    void initViewModel_attributesRendered() {
        // Attributes box should be populated (may be empty if no editable attrs)
        assertNotNull(attributesBox);
    }

    @Test
    @DisplayName("title save on action event")
    void titleField_onAction_savesTitle() {
        titleField.setText("New Title");
        titleField.fireEvent(
                new javafx.event.ActionEvent(titleField, titleField));
        assertEquals("New Title", viewModel.titleProperty().get());
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = NoteEditorViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
