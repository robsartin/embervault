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

/**
 * Branch coverage tests for {@link TextPaneViewController}.
 *
 * <p>Covers null/missing note handling, title-unchanged skip,
 * text-unchanged skip, placeholder toggle, managed property,
 * switching between notes, and Enter-key save.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class TextPaneViewControllerBranchTest {

    private TextPaneViewController controller;
    private SelectedNoteViewModel viewModel;
    private NoteService noteService;
    private VBox textPaneRoot;
    private Label placeholderLabel;
    private TextField titleField;
    private TextArea textArea;
    private UUID noteId;
    private UUID secondNoteId;

    @Start
    private void start(Stage stage) {
        textPaneRoot = new VBox();
        placeholderLabel = new Label("Select a note");
        titleField = new TextField();
        textArea = new TextArea();
        textPaneRoot.getChildren().addAll(
                placeholderLabel, titleField, textArea);
        stage.setScene(
                new javafx.scene.Scene(textPaneRoot, 400, 300));
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository =
                new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);

        noteId = noteService.createNote(
                "First Note", "First content").getId();
        secondNoteId = noteService.createNote(
                "Second Note", "Second content").getId();

        viewModel = new SelectedNoteViewModel(
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState());

        controller = new TextPaneViewController();

        injectField("textPaneRoot", textPaneRoot);
        injectField("placeholderLabel", placeholderLabel);
        injectField("titleField", titleField);
        injectField("textArea", textArea);

        controller.initViewModel(viewModel);
    }

    // --- Null note handling ---

    @Test
    @DisplayName("null note shows placeholder and hides fields")
    void nullNote_showsPlaceholder(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(null));

        assertTrue(placeholderLabel.isVisible(),
                "Placeholder should show for null note");
        assertFalse(titleField.isVisible(),
                "Title should hide for null note");
        assertFalse(textArea.isVisible(),
                "Text area should hide for null note");
    }

    @Test
    @DisplayName("selecting null after a note reverts to "
            + "placeholder")
    void selectNull_afterNote_revertsToPlaceholder(
            FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        assertTrue(titleField.isVisible());

        robot.interact(
                () -> viewModel.setSelectedNoteId(null));
        assertTrue(placeholderLabel.isVisible(),
                "Placeholder should appear after deselection");
        assertFalse(titleField.isVisible(),
                "Title should hide after deselection");
    }

    // --- Title Enter key ---

    @Test
    @DisplayName("Enter on title field saves the title")
    void titleEnter_savesTitle(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));

        robot.interact(() -> {
            titleField.requestFocus();
            titleField.setText("Enter Title");
            titleField.fireEvent(
                    new javafx.event.ActionEvent());
        });

        assertEquals("Enter Title",
                viewModel.titleProperty().get(),
                "Enter should save the title");
    }

    // --- Switching notes ---

    @Test
    @DisplayName("switching notes updates title and text")
    void switchNotes_updatesTitleAndText(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        assertEquals("First Note", titleField.getText());
        assertEquals("First content", textArea.getText());

        robot.interact(
                () -> viewModel.setSelectedNoteId(
                        secondNoteId));
        assertEquals("Second Note", titleField.getText(),
                "Title should reflect second note");
        assertEquals("Second content", textArea.getText(),
                "Text should reflect second note");
    }

    // --- Title property listener no-update branch ---

    @Test
    @DisplayName("title property change to same value "
            + "does not re-set field")
    void titleProperty_sameValueNoop(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        String current = titleField.getText();

        robot.interact(
                () -> viewModel.titleProperty().set(current));

        assertEquals(current, titleField.getText(),
                "Field should not change for same value");
    }

    // --- Text property listener no-update branch ---

    @Test
    @DisplayName("text property change to same value "
            + "does not re-set area")
    void textProperty_sameValueNoop(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        String current = textArea.getText();

        robot.interact(
                () -> viewModel.textProperty().set(current));

        assertEquals(current, textArea.getText(),
                "Area should not change for same value");
    }

    // --- Title property listener update branch ---

    @Test
    @DisplayName("title property change to different value "
            + "updates field")
    void titleProperty_differentValueUpdates(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));

        robot.interact(() -> viewModel.titleProperty()
                .set("External Change"));

        assertEquals("External Change",
                titleField.getText(),
                "Field should reflect external title change");
    }

    // --- Text property listener update branch ---

    @Test
    @DisplayName("text property change to different value "
            + "updates area")
    void textProperty_differentValueUpdates(FxRobot robot) {
        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));

        robot.interact(() -> viewModel.textProperty()
                .set("External body change"));

        assertEquals("External body change",
                textArea.getText(),
                "Area should reflect external text change");
    }

    // --- Visibility toggling ---

    @Test
    @DisplayName("managed property follows visibility "
            + "for placeholder")
    void placeholder_managedFollowsVisibility(FxRobot robot) {
        assertTrue(placeholderLabel.isManaged(),
                "Placeholder should be managed when visible");

        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        assertFalse(placeholderLabel.isManaged(),
                "Placeholder not managed when hidden");
    }

    @Test
    @DisplayName("managed property follows visibility "
            + "for title field")
    void titleField_managedFollowsVisibility(FxRobot robot) {
        assertFalse(titleField.isManaged(),
                "Title should not be managed when hidden");

        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        assertTrue(titleField.isManaged(),
                "Title should be managed when visible");
    }

    @Test
    @DisplayName("managed property follows visibility "
            + "for text area")
    void textArea_managedFollowsVisibility(FxRobot robot) {
        assertFalse(textArea.isManaged(),
                "Text area not managed when hidden");

        robot.interact(
                () -> viewModel.setSelectedNoteId(noteId));
        assertTrue(textArea.isManaged(),
                "Text area should be managed when visible");
    }

    // --- Initial state ---

    @Test
    @DisplayName("initial title field text is empty")
    void initial_titleFieldEmpty(FxRobot robot) {
        // Before any note is selected, title should be
        // the viewModel's initial value
        assertEquals("", viewModel.titleProperty().get(),
                "Initial title should be empty");
    }

    @Test
    @DisplayName("initial text area is empty")
    void initial_textAreaEmpty(FxRobot robot) {
        assertEquals("", viewModel.textProperty().get(),
                "Initial text should be empty");
    }

    // --- getViewModel ---

    @Test
    @DisplayName("getViewModel returns the injected viewModel")
    void getViewModel_returnsInjectedViewModel() {
        assertEquals(viewModel, controller.getViewModel(),
                "Should return the initialized ViewModel");
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
