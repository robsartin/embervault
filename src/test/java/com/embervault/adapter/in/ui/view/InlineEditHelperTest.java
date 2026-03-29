package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link InlineEditHelper} — inline title editing.
 *
 * <p>Covers commit, cancel, and focus-lost paths.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class InlineEditHelperTest {

    private NoteService noteService;
    private MapViewModel viewModel;
    private Pane mapCanvas;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repo = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repo);
        SimpleStringProperty noteTitle = new SimpleStringProperty("Test");
        viewModel = new MapViewModel(noteTitle, noteService);

        parentId = noteService.createNote("Parent", "").getId();
        viewModel.setBaseNoteId(parentId);
        mapCanvas = new Pane();
    }

    /**
     * Builds a minimal StackPane that mirrors what MapViewController creates:
     * child 0 = Rectangle, child 1 = VBox whose first child is the title Label.
     */
    private StackPane buildNotePane(Label titleLabel, double width) {
        Rectangle rect = new Rectangle(width, 60);
        VBox textBox = new VBox(titleLabel);
        return new StackPane(rect, textBox);
    }

    // --- commit path --------------------------------------------------------

    @Test
    @DisplayName("startInlineEdit replaces label with text field")
    void startInlineEdit_shouldReplaceLabel() {
        NoteDisplayItem item = viewModel.createChildNote("Original");
        Label titleLabel = new Label("Original");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEdit(
                notePane, titleLabel, (Rectangle) notePane.getChildren().get(0),
                item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        assertInstanceOf(TextField.class, textBox.getChildren().get(0),
                "Title label should be replaced by a TextField");
    }

    @Test
    @DisplayName("Enter commits a valid rename")
    void enter_shouldCommitRename() {
        NoteDisplayItem item = viewModel.createChildNote("Original");
        Label titleLabel = new Label("Original");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEdit(
                notePane, titleLabel, (Rectangle) notePane.getChildren().get(0),
                item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        TextField tf = (TextField) textBox.getChildren().get(0);
        javafx.application.Platform.runLater(() -> {
            tf.setText("Renamed");
            tf.fireEvent(new javafx.event.ActionEvent());
        });
        TestFxHelper.waitForFx();

        assertEquals("Renamed", titleLabel.getText(),
                "Label should reflect the committed title");
    }

    @Test
    @DisplayName("focus-lost with empty text does not rename")
    void focusLost_emptyText_shouldNotRename() {
        NoteDisplayItem item = viewModel.createChildNote("Original");
        Label titleLabel = new Label("Original");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEdit(
                notePane, titleLabel, (Rectangle) notePane.getChildren().get(0),
                item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        TextField tf = (TextField) textBox.getChildren().get(0);
        tf.setText("   ");

        javafx.application.Platform.runLater(
                () -> tf.getParent().requestFocus());
        TestFxHelper.waitForFx();
        TestFxHelper.waitForFx();

        assertEquals("Original", titleLabel.getText(),
                "Label should keep original title when new text is blank");
    }

    @Test
    @DisplayName("cancel (Escape) restores the original label without renaming")
    void escape_shouldCancelEdit() {
        NoteDisplayItem item = viewModel.createChildNote("Original");
        Label titleLabel = new Label("Original");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEdit(
                notePane, titleLabel, (Rectangle) notePane.getChildren().get(0),
                item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        TextField tf = (TextField) textBox.getChildren().get(0);
        tf.setText("Changed");

        // Fire Escape key event
        var escEvent = new javafx.scene.input.KeyEvent(
                javafx.scene.input.KeyEvent.KEY_PRESSED, "", "",
                KeyCode.ESCAPE, false, false, false, false);
        tf.getOnKeyPressed().handle(escEvent);
        TestFxHelper.waitForFx();

        // The label should be restored in the VBox
        assertTrue(textBox.getChildren().contains(titleLabel),
                "Original label should be restored after Escape");
        assertFalse(textBox.getChildren().contains(tf),
                "TextField should be removed after Escape");
        assertEquals("Original", titleLabel.getText(),
                "Title text should remain unchanged after cancel");
    }

    @Test
    @DisplayName("commit restores label after successful rename")
    void commit_shouldRestoreLabel() {
        NoteDisplayItem item = viewModel.createChildNote("Original");
        Label titleLabel = new Label("Original");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEdit(
                notePane, titleLabel, (Rectangle) notePane.getChildren().get(0),
                item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        TextField tf = (TextField) textBox.getChildren().get(0);
        javafx.application.Platform.runLater(() -> {
            tf.setText("NewTitle");
            tf.fireEvent(new javafx.event.ActionEvent());
        });
        TestFxHelper.waitForFx();

        assertTrue(textBox.getChildren().contains(titleLabel),
                "Label should be restored after commit");
        assertFalse(textBox.getChildren().contains(tf),
                "TextField should be removed after commit");
    }

    @Test
    @DisplayName("startInlineEditOnNode extracts label and rect from pane")
    void startInlineEditOnNode_shouldStartEditing() {
        NoteDisplayItem item = viewModel.createChildNote("NodeTitle");
        Label titleLabel = new Label("NodeTitle");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEditOnNode(
                notePane, item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        assertInstanceOf(TextField.class, textBox.getChildren().get(0),
                "startInlineEditOnNode should replace label with TextField");
    }

    @Test
    @DisplayName("text field is pre-populated with current title and selected")
    void textField_shouldBePrePopulated() {
        NoteDisplayItem item = viewModel.createChildNote("MyNote");
        Label titleLabel = new Label("MyNote");
        StackPane notePane = buildNotePane(titleLabel, 120);

        InlineEditHelper.startInlineEdit(
                notePane, titleLabel, (Rectangle) notePane.getChildren().get(0),
                item, viewModel, mapCanvas);

        VBox textBox = (VBox) notePane.getChildren().get(1);
        TextField tf = (TextField) textBox.getChildren().get(0);
        assertEquals("MyNote", tf.getText(),
                "TextField should contain the original title");
    }
}
