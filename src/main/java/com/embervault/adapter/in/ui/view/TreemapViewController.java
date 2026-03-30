package com.embervault.adapter.in.ui.view;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.TreemapRect;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Treemap view.
 *
 * <p>Renders notes as colored rectangles laid out using a treemap algorithm.
 * Notes are selectable via click and drillable via double-click. The layout
 * recalculates on pane resize.</p>
 */
public class TreemapViewController {

    private static final Logger LOG = LoggerFactory.getLogger(TreemapViewController.class);
    private static final double SELECTED_BORDER_WIDTH = 3.0;
    private static final double NORMAL_BORDER_WIDTH = 1.0;
    private static final double TITLE_FONT_SIZE = 13.0;
    private static final double RECT_PADDING = 2.0;
    private static final double BACK_BUTTON_PADDING = 5.0;

    @FXML private Pane treemapCanvas;

    private TreemapViewModel viewModel;
    private Button backButton;
    private ViewColorConfig currentColors;
    private Consumer<String> onViewSwitch;

    /**
     * Sets the callback invoked when the user selects a view-switch
     * menu item. The callback receives the {@link ViewType} name.
     *
     * @param callback the view-switch callback
     */
    public void setOnViewSwitch(Consumer<String> callback) {
        this.onViewSwitch = callback;
    }

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     *
     * @param treemapViewModel the view model to bind
     */
    public void initViewModel(TreemapViewModel treemapViewModel) {
        this.viewModel = treemapViewModel;

        backButton = new Button("\u2190 Back");
        backButton.setVisible(false);
        backButton.setOnAction(e -> viewModel.navigateBack());
        backButton.setLayoutX(BACK_BUTTON_PADDING);
        backButton.setLayoutY(BACK_BUTTON_PADDING);
        viewModel.canNavigateBackProperty().addListener(
                (obs, oldVal, newVal) -> backButton.setVisible(newVal));

        viewModel.loadNotes();
        renderAllNotes();

        viewModel.getNoteItems().addListener(
                (ListChangeListener<NoteDisplayItem>) change -> renderAllNotes());

        // Re-layout on resize
        treemapCanvas.widthProperty().addListener(
                (obs, oldVal, newVal) -> renderAllNotes());
        treemapCanvas.heightProperty().addListener(
                (obs, oldVal, newVal) -> renderAllNotes());

        // Escape to navigate back
        treemapCanvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE
                    && viewModel.canNavigateBackProperty().get()) {
                viewModel.navigateBack();
            }
        });

        // Context menu
        ContextMenu contextMenu = createContextMenu();
        treemapCanvas.setOnContextMenuRequested(event -> {
            contextMenu.show(treemapCanvas, event.getScreenX(),
                    event.getScreenY());
            event.consume();
        });

        treemapCanvas.setFocusTraversable(true);
    }

    /** Returns the associated ViewModel. */
    public TreemapViewModel getViewModel() {
        return viewModel;
    }

    /** Handles a single click on a treemap cell, selecting the note. */
    void handleCellClick(NoteDisplayItem item) {
        viewModel.selectNote(item.getId());
    }

    /** Handles a double-click on a treemap cell, drilling down into the note. */
    void handleCellDoubleClick(NoteDisplayItem item) {
        viewModel.drillDown(item.getId());
    }

    private ContextMenu createContextMenu() {
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setOnAction(e -> viewModel.createChildNote("Untitled"));

        ContextMenu menu = new ContextMenu(createNote);
        menu.getItems().addAll(
                ViewSwitchMenuHelper.createViewSwitchItems(
                        ViewType.TREEMAP, onViewSwitch));
        return menu;
    }

    private void renderAllNotes() {
        treemapCanvas.getChildren().clear();
        double width = treemapCanvas.getWidth();
        double height = treemapCanvas.getHeight();
        if (width <= 0 || height <= 0) {
            treemapCanvas.getChildren().add(backButton);
            return;
        }

        List<TreemapRect> rects = viewModel.getTreemapRects(width, height);
        Map<UUID, NoteDisplayItem> itemMap = viewModel.getNoteItems().stream()
                .collect(Collectors.toMap(NoteDisplayItem::getId, n -> n));

        for (TreemapRect tr : rects) {
            NoteDisplayItem item = itemMap.get(tr.id());
            if (item != null) {
                StackPane noteNode = createNoteNode(item, tr);
                treemapCanvas.getChildren().add(noteNode);
            }
        }
        treemapCanvas.getChildren().add(backButton);
    }

    private StackPane createNoteNode(NoteDisplayItem item, TreemapRect tr) {
        double rectWidth = Math.max(0, tr.width() - RECT_PADDING * 2);
        double rectHeight = Math.max(0, tr.height() - RECT_PADDING * 2);

        Rectangle rect = new Rectangle(rectWidth, rectHeight);
        rect.setFill(Color.web(item.getColorHex()));
        rect.setStroke(currentColors != null
                ? Color.web(currentColors.borderColor()) : Color.BLACK);
        rect.setStrokeWidth(NORMAL_BORDER_WIDTH);
        rect.setArcWidth(4);
        rect.setArcHeight(4);

        String labelText = TreemapNodeFactory.truncateLabel(
                item.getTitle(), rectWidth);
        Label titleLabel = new Label(labelText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD,
                TITLE_FONT_SIZE));
        titleLabel.setTextFill(Color.web(
                ViewColorConfig.contrastTextColor(item.getColorHex())));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(rectWidth - 4);
        titleLabel.setMaxHeight(rectHeight - 4);
        titleLabel.setWrapText(true);
        titleLabel.setPadding(new Insets(2));
        titleLabel.setMouseTransparent(true);

        Rectangle clip = new Rectangle(rectWidth, rectHeight);
        StackPane notePane = new StackPane(rect, titleLabel);

        // Badge in top-right corner if space permits
        String badge = item.getBadge();
        if (badge != null && !badge.isEmpty() && rectWidth > 30 && rectHeight > 20) {
            Label badgeLabel = new Label(badge);
            badgeLabel.setFont(Font.font("System", TITLE_FONT_SIZE));
            badgeLabel.setMouseTransparent(true);
            badgeLabel.setPadding(new Insets(2, 4, 0, 0));
            StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
            notePane.getChildren().add(badgeLabel);
        }

        notePane.setClip(clip);
        notePane.setUserData(item.getId());
        notePane.setAlignment(Pos.CENTER);
        notePane.setLayoutX(tr.x() + RECT_PADDING);
        notePane.setLayoutY(tr.y() + RECT_PADDING);

        // Click to select
        notePane.setOnMousePressed(event -> {
            handleCellClick(item);
            highlightSelected(notePane);
        });

        // Double-click to drill down
        notePane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY) {
                handleCellDoubleClick(item);
                event.consume();
            }
        });

        // Highlight if currently selected
        if (item.getId().equals(viewModel.selectedNoteIdProperty().get())) {
            rect.setStrokeWidth(SELECTED_BORDER_WIDTH);
            rect.setStroke(currentColors != null
                    ? Color.web(currentColors.selectionColor())
                    : Color.DODGERBLUE);
        }

        return notePane;
    }


    private void highlightSelected(StackPane selected) {
        Color borderCol = currentColors != null
                ? Color.web(currentColors.borderColor()) : Color.BLACK;
        Color selCol = currentColors != null
                ? Color.web(currentColors.selectionColor())
                : Color.DODGERBLUE;
        for (Node child : treemapCanvas.getChildren()) {
            if (child instanceof StackPane sp && !sp.getChildren().isEmpty()
                    && sp.getChildren().get(0) instanceof Rectangle r) {
                r.setStrokeWidth(NORMAL_BORDER_WIDTH);
                r.setStroke(borderCol);
            }
        }
        if (!selected.getChildren().isEmpty()
                && selected.getChildren().get(0) instanceof Rectangle r) {
            r.setStrokeWidth(SELECTED_BORDER_WIDTH);
            r.setStroke(selCol);
        }
    }

    /**
     * Applies a color scheme to the treemap view.
     *
     * @param colors the view color config to apply
     */
    public void applyColorScheme(ViewColorConfig colors) {
        this.currentColors = colors;
        treemapCanvas.setStyle("-fx-background-color: "
                + colors.canvasBackground() + ";");
        renderAllNotes();
    }
}
