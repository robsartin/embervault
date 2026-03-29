package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
 * Tests for {@link TextPaneViewController}.
 *
 * <p>Verifies placeholder visibility, title/text binding to the ViewModel,
 * save-on-focus-lost behavior, and save-on-Enter for the title field.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class TextPaneViewControllerTest {

    private TextPaneViewController controller;
    private SelectedNoteViewModel viewModel;
    private NoteService noteService;
    private VBox textPaneRoot;
    private Label placeholderLabel;
    private TextField titleField;
    private TextArea textArea;
    private UUID noteId;

    @Start
    private void start(Stage stage) {
        textPaneRoot = new VBox();
        placeholderLabel = new Label("Select a note to edit");
        titleField = new TextField();
        textArea = new TextArea();
        textPaneRoot.getChildren().addAll(placeholderLabel, titleField,
                textArea);
        stage.setScene(new javafx.scene.Scene(textPaneRoot, 400, 300));
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        noteId = noteService.createNote("Test Note", "Test content").getId();

        viewModel = new SelectedNoteViewModel(noteService);

        controller = new TextPaneViewController();

        injectField("textPaneRoot", textPaneRoot);
        injectField("placeholderLabel", placeholderLabel);
        injectField("titleField", titleField);
        injectField("textArea", textArea);

        controller.initViewModel(viewModel);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("placeholder is shown and editor hidden when no note selected")
    void noNoteSelected_showsPlaceholder() {
        assertTrue(placeholderLabel.isVisible(),
                "Placeholder should be visible with no note selected");
        assertFalse(titleField.isVisible(),
                "Title field should be hidden with no note selected");
        assertFalse(textArea.isVisible(),
                "Text area should be hidden with no note selected");
    }

    @Test
    @DisplayName("editor fields shown when a note is selected")
    void noteSelected_showsEditorFields() {
        viewModel.setSelectedNoteId(noteId);

        assertFalse(placeholderLabel.isVisible(),
                "Placeholder should be hidden when note selected");
        assertTrue(titleField.isVisible(),
                "Title field should be visible when note selected");
        assertTrue(textArea.isVisible(),
                "Text area should be visible when note selected");
    }

    @Test
    @DisplayName("title field populated from viewModel when note selected")
    void noteSelected_titleFieldPopulated() {
        viewModel.setSelectedNoteId(noteId);

        assertEquals("Test Note", titleField.getText(),
                "Title field should show note title");
    }

    @Test
    @DisplayName("text area populated from viewModel when note selected")
    void noteSelected_textAreaPopulated() {
        viewModel.setSelectedNoteId(noteId);

        assertEquals("Test content", textArea.getText(),
                "Text area should show note content");
    }

    @Test
    @DisplayName("title updates in field when viewModel title changes")
    void viewModelTitleChange_updatesField() {
        viewModel.setSelectedNoteId(noteId);
        assertEquals("Test Note", titleField.getText());

        viewModel.titleProperty().set("Updated Title");
        assertEquals("Updated Title", titleField.getText(),
                "Title field should reflect viewModel title change");
    }

    @Test
    @DisplayName("text updates in area when viewModel text changes")
    void viewModelTextChange_updatesArea() {
        viewModel.setSelectedNoteId(noteId);
        assertEquals("Test content", textArea.getText());

        viewModel.textProperty().set("Updated text");
        assertEquals("Updated text", textArea.getText(),
                "Text area should reflect viewModel text change");
    }

    @Test
    @DisplayName("title saved on focus lost")
    void titleField_savesOnFocusLost(FxRobot robot) {
        robot.interact(() -> viewModel.setSelectedNoteId(noteId));
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> {
            titleField.requestFocus();
            titleField.setText("Renamed Note");
        });
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> textArea.requestFocus());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Renamed Note", viewModel.titleProperty().get(),
                "Title should be saved after focus lost");

        // Verify persistence
        String persistedTitle = noteService.getNote(noteId)
                .map(n -> n.getTitle())
                .orElse("");
        assertEquals("Renamed Note", persistedTitle,
                "Title should be persisted via NoteService");
    }

    @Test
    @DisplayName("title saved on Enter key")
    void titleField_savesOnEnter(FxRobot robot) {
        robot.interact(() -> viewModel.setSelectedNoteId(noteId));
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> {
            titleField.requestFocus();
            titleField.setText("Enter Title");
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() ->
                titleField.fireEvent(new javafx.event.ActionEvent()));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Enter Title", viewModel.titleProperty().get(),
                "Title should be saved after Enter");
    }

    @Test
    @DisplayName("text saved on focus lost")
    void textArea_savesOnFocusLost(FxRobot robot) {
        robot.interact(() -> viewModel.setSelectedNoteId(noteId));
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> {
            textArea.requestFocus();
            textArea.setText("Updated body text");
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> titleField.requestFocus());

        assertEquals("Updated body text", viewModel.textProperty().get(),
                "Text should be saved after focus lost");
    }

    @Test
    @DisplayName("clearing selection hides editor and shows placeholder")
    void clearSelection_showsPlaceholder() {
        javafx.application.Platform.runLater(
                () -> viewModel.setSelectedNoteId(noteId));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(titleField.isVisible());

        javafx.application.Platform.runLater(
                () -> viewModel.setSelectedNoteId(null));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(placeholderLabel.isVisible(),
                "Placeholder should reappear after clearing selection");
        assertFalse(titleField.isVisible(),
                "Title field should hide after clearing selection");
        assertFalse(textArea.isVisible(),
                "Text area should hide after clearing selection");
    }

    @Test
    @DisplayName("getViewModel returns the injected viewModel")
    void getViewModel_returnsInjectedViewModel() {
        assertEquals(viewModel, controller.getViewModel(),
                "getViewModel should return the initialized ViewModel");
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = TextPaneViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
