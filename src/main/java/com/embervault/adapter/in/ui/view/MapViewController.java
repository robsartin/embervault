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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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
    private static final double FONT_SIZE = 12.0;

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
        titleLabel.setFont(Font.font(FONT_SIZE));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(item.getWidth() - 8);
        titleLabel.setWrapText(true);
        titleLabel.setMouseTransparent(false);
        titleLabel.setPadding(new Insets(2, 4, 2, 4));

        StackPane notePane = new StackPane(rect, titleLabel);
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        notePane.setCursor(Cursor.HAND);

        // Double-click on title label -> inline edit
        titleLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                startInlineEdit(notePane, titleLabel, rect, item);
                event.consume();
            } else {
                viewModel.selectNote(item.getId());
                highlightSelected(notePane);
                event.consume();
            }
        });

        // Double-click on rectangle body (not title) -> drill down
        rect.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                viewModel.drillDown(item.getId());
                event.consume();
            } else {
                viewModel.selectNote(item.getId());
                highlightSelected(notePane);
                event.consume();
            }
        });

        // Drag support
        enableDrag(notePane, item);

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
        textField.setFont(Font.font(FONT_SIZE));
        textField.setAlignment(Pos.CENTER);
        textField.setMaxWidth(rect.getWidth() - 8);
        textField.selectAll();

        // Replace label with text field
        notePane.getChildren().remove(titleLabel);
        notePane.getChildren().add(textField);
        textField.requestFocus();

        // Commit on Enter
        textField.setOnAction(e -> {
            String newTitle = textField.getText().trim();
            if (!newTitle.isEmpty() && viewModel.renameNote(item.getId(), newTitle)) {
                titleLabel.setText(newTitle);
            }
            notePane.getChildren().remove(textField);
            notePane.getChildren().add(titleLabel);
        });

        // Cancel on Escape
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                notePane.getChildren().remove(textField);
                notePane.getChildren().add(titleLabel);
                e.consume();
            }
        });

        // Commit on focus lost (same as pressing Enter)
        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && notePane.getChildren().contains(textField)) {
                String newTitle = textField.getText().trim();
                if (!newTitle.isEmpty() && viewModel.renameNote(item.getId(), newTitle)) {
                    titleLabel.setText(newTitle);
                }
                notePane.getChildren().remove(textField);
                if (!notePane.getChildren().contains(titleLabel)) {
                    notePane.getChildren().add(titleLabel);
                }
            }
        });
    }

    private void enableDrag(StackPane notePane, NoteDisplayItem item) {
        final double[] dragDelta = new double[2];

        notePane.setOnMousePressed(event -> {
            dragDelta[0] = notePane.getLayoutX() - event.getSceneX();
            dragDelta[1] = notePane.getLayoutY() - event.getSceneY();
            viewModel.selectNote(item.getId());
            highlightSelected(notePane);
            event.consume();
        });

        notePane.setOnMouseDragged(event -> {
            double newX = Math.max(0, event.getSceneX() + dragDelta[0]);
            double newY = Math.max(0, event.getSceneY() + dragDelta[1]);
            notePane.setLayoutX(newX);
            notePane.setLayoutY(newY);
            event.consume();
        });

        notePane.setOnMouseReleased(event -> {
            viewModel.updateNotePosition(
                    item.getId(), notePane.getLayoutX(), notePane.getLayoutY());
            event.consume();
        });
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
