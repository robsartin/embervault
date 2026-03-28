package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Map view.
 *
 * <p>Renders notes as colored rectangles on a spatial canvas. Notes are
 * draggable and selectable. New notes can be created by pressing Return
 * or double-clicking the background.</p>
 */
public class MapViewController {

    private static final Logger LOG = LoggerFactory.getLogger(MapViewController.class);
    private static final double SELECTED_BORDER_WIDTH = 3.0;
    private static final double NORMAL_BORDER_WIDTH = 1.0;
    private static final double TITLE_FONT_SIZE = 14.0;
    private static final double CONTENT_FONT_SIZE = 11.0;

    private static final double BACK_BUTTON_PADDING = 5.0;

    @FXML private Pane mapCanvas;

    private MapViewModel viewModel;
    private Button backButton;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(MapViewModel viewModel) {
        this.viewModel = viewModel;

        // Back navigation button
        backButton = new Button("\u2190 Back");
        backButton.setVisible(false);
        backButton.setOnAction(e -> viewModel.navigateBack());
        backButton.setLayoutX(BACK_BUTTON_PADDING);
        backButton.setLayoutY(BACK_BUTTON_PADDING);
        viewModel.canNavigateBackProperty().addListener(
                (obs, oldVal, newVal) -> backButton.setVisible(newVal));

        // Render existing notes
        viewModel.loadNotes();
        renderAllNotes();

        // Re-render when note list changes
        viewModel.getNoteItems().addListener(
                (ListChangeListener<NoteDisplayItem>) change -> renderAllNotes());

        // Double-click background to create new note
        mapCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY
                    && event.getTarget() == mapCanvas) {
                viewModel.createChildNote("Untitled");
            }
        });

        // Return key to create new note; Escape to navigate back
        mapCanvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                viewModel.createChildNote("Untitled");
            } else if (event.getCode() == KeyCode.ESCAPE
                    && viewModel.canNavigateBackProperty().get()) {
                viewModel.navigateBack();
            }
        });

        // Context menu
        ContextMenu contextMenu = createContextMenu();
        mapCanvas.setOnContextMenuRequested(event -> {
            contextMenu.getItems().get(0).setOnAction(e ->
                    viewModel.createChildNoteAt("Untitled", event.getX(), event.getY()));
            contextMenu.show(mapCanvas, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        mapCanvas.setFocusTraversable(true);
    }

    /** Returns the associated ViewModel. */
    public MapViewModel getViewModel() {
        return viewModel;
    }

    private ContextMenu createContextMenu() {
        MenuItem createNote = new MenuItem("Create Note");
        // Action is set dynamically in the context menu request handler to capture coordinates

        MenuItem outlineView = new MenuItem("Outline View");
        outlineView.setOnAction(e -> LOG.debug("Outline View placeholder selected"));

        return new ContextMenu(createNote, new SeparatorMenuItem(), outlineView);
    }

    private void renderAllNotes() {
        mapCanvas.getChildren().clear();
        for (NoteDisplayItem item : viewModel.getNoteItems()) {
            StackPane noteNode = createNoteNode(item);
            mapCanvas.getChildren().add(noteNode);
        }
        // Keep back button on top
        mapCanvas.getChildren().add(backButton);
    }

    private StackPane createNoteNode(NoteDisplayItem item) {
        Rectangle rect = new Rectangle(item.getWidth(), item.getHeight());
        rect.setFill(Color.web(item.getColorHex()));
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(NORMAL_BORDER_WIDTH);
        rect.setArcWidth(4);
        rect.setArcHeight(4);

        Label titleLabel = new Label(item.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, TITLE_FONT_SIZE));
        titleLabel.setTextAlignment(TextAlignment.LEFT);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        titleLabel.setMaxWidth(item.getWidth() - 8);
        titleLabel.setWrapText(true);
        titleLabel.setMouseTransparent(false);
        titleLabel.setPadding(new Insets(4, 4, 2, 4));

        VBox textBox = new VBox(titleLabel);

        String content = item.getContent();
        if (content != null && !content.isEmpty()) {
            Label contentLabel = new Label(content);
            contentLabel.setFont(Font.font("System", CONTENT_FONT_SIZE));
            contentLabel.setTextAlignment(TextAlignment.LEFT);
            contentLabel.setAlignment(Pos.TOP_LEFT);
            contentLabel.setMaxWidth(item.getWidth() - 8);
            contentLabel.setMaxHeight(Double.MAX_VALUE);
            contentLabel.setWrapText(true);
            contentLabel.setMouseTransparent(true);
            contentLabel.setPadding(new Insets(0, 4, 4, 4));
            VBox.setVgrow(contentLabel, Priority.ALWAYS);
            textBox.getChildren().add(contentLabel);
        }

        textBox.setMaxWidth(item.getWidth());
        textBox.setMaxHeight(item.getHeight());
        textBox.setAlignment(Pos.TOP_LEFT);

        // Clip the text container to the rectangle bounds
        Rectangle clip = new Rectangle(item.getWidth(), item.getHeight());
        textBox.setClip(clip);

        StackPane notePane = new StackPane(rect, textBox);
        notePane.setAlignment(Pos.TOP_LEFT);
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        notePane.setCursor(Cursor.HAND);

        // Drag support – returns a flag array so click handlers can check
        // whether the gesture was a drag rather than a click.
        final boolean[] dragging = enableDrag(notePane, item);

        // Double-click on title label -> inline edit
        titleLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY
                    && !dragging[0]) {
                startInlineEdit(notePane, titleLabel, rect, item);
                event.consume();
            }
        });

        // Double-click on rectangle body (not title) -> drill down
        rect.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY
                    && !dragging[0]) {
                viewModel.drillDown(item.getId());
                event.consume();
            }
        });

        // Highlight if currently selected
        if (item.getId().equals(viewModel.selectedNoteIdProperty().get())) {
            rect.setStrokeWidth(SELECTED_BORDER_WIDTH);
            rect.setStroke(Color.DODGERBLUE);
        }

        return notePane;
    }

    private void startInlineEdit(StackPane notePane, Label titleLabel,
            Rectangle rect, NoteDisplayItem item) {
        String originalTitle = titleLabel.getText();
        TextField textField = new TextField(originalTitle);
        textField.setFont(Font.font("System", FontWeight.BOLD, TITLE_FONT_SIZE));
        textField.setAlignment(Pos.CENTER_LEFT);
        textField.setMaxWidth(rect.getWidth() - 8);
        textField.selectAll();

        // The VBox containing labels is the second child of the StackPane
        VBox textBox = (VBox) notePane.getChildren().get(1);
        int titleIndex = textBox.getChildren().indexOf(titleLabel);

        // Replace title label with text field inside the VBox
        textBox.getChildren().set(titleIndex, textField);
        textField.requestFocus();

        Runnable commitEdit = () -> {
            String newTitle = textField.getText().trim();
            if (!newTitle.isEmpty() && viewModel.renameNote(item.getId(), newTitle)) {
                titleLabel.setText(newTitle);
            }
            if (textBox.getChildren().contains(textField)) {
                textBox.getChildren().set(
                        textBox.getChildren().indexOf(textField), titleLabel);
            }
        };

        Runnable cancelEdit = () -> {
            if (textBox.getChildren().contains(textField)) {
                textBox.getChildren().set(
                        textBox.getChildren().indexOf(textField), titleLabel);
            }
        };

        // Commit on Enter
        textField.setOnAction(e -> commitEdit.run());

        // Cancel on Escape
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit.run();
                e.consume();
            }
        });

        // Commit on focus lost (same as pressing Enter)
        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitEdit.run();
            }
        });
    }

    /**
     * Installs drag handlers on the note pane while allowing click events to
     * propagate to children (title label, rectangle).
     *
     * @return a single-element boolean array whose value is {@code true} while
     *         a drag gesture is in progress; click handlers check this to avoid
     *         treating the end of a drag as a click.
     */
    private boolean[] enableDrag(StackPane notePane, NoteDisplayItem item) {
        final double[] dragDelta = new double[2];
        final boolean[] dragging = {false};

        notePane.setOnMousePressed(event -> {
            dragDelta[0] = notePane.getLayoutX() - event.getSceneX();
            dragDelta[1] = notePane.getLayoutY() - event.getSceneY();
            dragging[0] = false;
            viewModel.selectNote(item.getId());
            highlightSelected(notePane);
            // Do NOT consume – let the event reach child click handlers
        });

        notePane.setOnMouseDragged(event -> {
            dragging[0] = true;
            double newX = Math.max(0, event.getSceneX() + dragDelta[0]);
            double newY = Math.max(0, event.getSceneY() + dragDelta[1]);
            notePane.setLayoutX(newX);
            notePane.setLayoutY(newY);
            event.consume();
        });

        notePane.setOnMouseReleased(event -> {
            if (dragging[0]) {
                viewModel.updateNotePosition(
                        item.getId(), notePane.getLayoutX(), notePane.getLayoutY());
                event.consume();
            }
        });

        return dragging;
    }

    private void highlightSelected(StackPane selected) {
        for (javafx.scene.Node child : mapCanvas.getChildren()) {
            if (child instanceof StackPane sp && !sp.getChildren().isEmpty()
                    && sp.getChildren().get(0) instanceof Rectangle r) {
                r.setStrokeWidth(NORMAL_BORDER_WIDTH);
                r.setStroke(Color.BLACK);
            }
        }
        if (!selected.getChildren().isEmpty()
                && selected.getChildren().get(0) instanceof Rectangle r) {
            r.setStrokeWidth(SELECTED_BORDER_WIDTH);
            r.setStroke(Color.DODGERBLUE);
        }
    }
}
