package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
 *
 * <p>Covers attribute display, title/text save on focus-lost,
 * and ViewModel binding.</p>
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

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() throws Exception {
        InMemoryNoteRepository repo = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repo);
        AttributeSchemaRegistry registry = new AttributeSchemaRegistry();
        viewModel = new NoteEditorViewModel(noteService, registry);

        controller = new NoteEditorViewController();
        titleField = new TextField();
        textArea = new TextArea();
        attributesBox = new VBox();
        VBox editorRoot = new VBox();

        inject(controller, "titleField", titleField);
        inject(controller, "textArea", textArea);
        inject(controller, "attributesBox", attributesBox);
        inject(controller, "editorRoot", editorRoot);
    }

    private static void inject(Object target, String fieldName, Object value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // --- initViewModel / binding -------------------------------------------

    @Test
    @DisplayName("initViewModel populates title and text fields from note")
    void initViewModel_shouldPopulateFields() {
        Note note = noteService.createNote("Hello", "World");
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        assertEquals("Hello", titleField.getText());
        assertEquals("World", textArea.getText());
    }

    @Test
    @DisplayName("getViewModel returns the injected ViewModel")
    void getViewModel_shouldReturnViewModel() {
        controller.initViewModel(viewModel);
        assertSame(viewModel, controller.getViewModel());
    }

    @Test
    @DisplayName("title property change updates the title field")
    void titlePropertyChange_shouldUpdateField() {
        Note note = noteService.createNote("Before", "");
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        viewModel.titleProperty().set("After");
        TestFxHelper.waitForFx();
        assertEquals("After", titleField.getText());
    }

    @Test
    @DisplayName("text property change updates the text area")
    void textPropertyChange_shouldUpdateField() {
        Note note = noteService.createNote("Title", "Before");
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        viewModel.textProperty().set("After");
        TestFxHelper.waitForFx();
        assertEquals("After", textArea.getText());
    }

    // --- attribute display --------------------------------------------------

    @Test
    @DisplayName("attributes are rendered as label + text field rows")
    void initViewModel_shouldRenderAttributes() {
        Note note = noteService.createNote("Note", "");
        note.setAttribute("$Color",
                new AttributeValue.StringValue("#FF0000"));
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        assertTrue(attributesBox.getChildren().size() > 0,
                "Attributes box should have at least one row");

        HBox row = (HBox) attributesBox.getChildren().get(0);
        assertEquals(2, row.getChildren().size(),
                "Each row should have a label and text field");
        assertNotNull(((Label) row.getChildren().get(0)).getText(),
                "Label should have a name");
        assertNotNull(((TextField) row.getChildren().get(1)).getText(),
                "TextField should have a value");
    }

    @Test
    @DisplayName("multiple attributes produce multiple rows")
    void multipleAttributes_shouldProduceMultipleRows() {
        Note note = noteService.createNote("Note", "");
        note.setAttribute("$Color",
                new AttributeValue.StringValue("#FF0000"));
        note.setAttribute("$Subtitle",
                new AttributeValue.StringValue("sub"));
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        assertTrue(attributesBox.getChildren().size() >= 2,
                "Should have at least two attribute rows");
    }

    // --- save behavior ------------------------------------------------------

    @Test
    @DisplayName("title save on Enter persists the new title")
    void titleOnAction_shouldSaveTitle() {
        Note note = noteService.createNote("Old", "");
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        titleField.setText("New");
        titleField.getOnAction().handle(
                new javafx.event.ActionEvent());
        TestFxHelper.waitForFx();

        assertEquals("New", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("attribute save on Enter persists the new value")
    void attributeOnAction_shouldSaveAttribute() {
        Note note = noteService.createNote("Note", "");
        note.setAttribute("$Color",
                new AttributeValue.StringValue("#FF0000"));
        viewModel.setNote(note.getId());
        controller.initViewModel(viewModel);

        HBox row = (HBox) attributesBox.getChildren().get(0);
        TextField valueField = (TextField) row.getChildren().get(1);
        valueField.setText("#00FF00");
        valueField.getOnAction().handle(
                new javafx.event.ActionEvent());
        TestFxHelper.waitForFx();

        // Verify the attribute was saved by reloading
        Note updated = noteService.getNote(note.getId()).orElseThrow();
        String color = updated.getAttribute("$Color")
                .map(v -> ((AttributeValue.StringValue) v).value())
                .orElse("");
        assertEquals("#00FF00", color);
    }
}
