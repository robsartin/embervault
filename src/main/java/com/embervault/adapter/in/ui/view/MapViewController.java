package com.embervault.adapter.in.ui.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.ZoomTier;
import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** FXML controller for the Map view. */
public class MapViewController {

    private static final Logger LOG = LoggerFactory.getLogger(MapViewController.class);
    private static final double SELECTED_BORDER_WIDTH = 3.0;
    private static final double NORMAL_BORDER_WIDTH = 1.0;
    private static final double TITLE_FONT_SIZE = 14.0;
    private static final double CONTENT_FONT_SIZE = 11.0;

    private static final double BACK_BUTTON_PADDING = 5.0;
    private static final double SCROLL_ZOOM_FACTOR = 1.1;
    private static final double DETAILED_CONTENT_FONT_SIZE = 14.0;
    private static final double ZOOM_PERCENTAGE = 100.0;

    @FXML private Pane mapCanvas;

    private MapViewModel viewModel;
    private Button backButton;
    private final Map<UUID, StackPane> nodeMap = new HashMap<>();
    private Scale zoomScale;
    private Label zoomLabel;
    private PauseTransition zoomRenderDebounce;
    private boolean rendering;

    /** Injects the ViewModel and binds UI controls. */
    public void initViewModel(MapViewModel viewModel) {
        this.viewModel = viewModel;
        setupZoom();

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
                (ListChangeListener<NoteDisplayItem>) this::onNoteItemsChanged);

        zoomRenderDebounce = new PauseTransition(Duration.millis(150));
        zoomRenderDebounce.setOnFinished(e -> renderAllNotes());
        viewModel.currentTierProperty().addListener(
                (obs, oldTier, newTier) -> zoomRenderDebounce.playFromStart());

        mapCanvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY
                    && event.getTarget() == mapCanvas) {
                viewModel.createChildNoteAt("Untitled", event.getX(), event.getY());
            }
        });

        // Scroll wheel zoom toward cursor
        mapCanvas.setOnScroll(event -> {
            double factor = event.getDeltaY() > 0
                    ? SCROLL_ZOOM_FACTOR : 1.0 / SCROLL_ZOOM_FACTOR;
            double newZoom = viewModel.zoomLevelProperty().get() * factor;
            zoomScale.setPivotX(event.getX());
            zoomScale.setPivotY(event.getY());
            viewModel.setZoomLevel(newZoom);
            event.consume();
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

    private void setupZoom() {
        zoomScale = new Scale(1.0, 1.0);
        mapCanvas.getTransforms().add(zoomScale);
        viewModel.zoomLevelProperty().addListener((obs, oldVal, newVal) -> {
            zoomScale.setX(newVal.doubleValue());
            zoomScale.setY(newVal.doubleValue());
            if (zoomLabel != null) {
                zoomLabel.setText(String.format("%.0f%%",
                        newVal.doubleValue() * ZOOM_PERCENTAGE));
            }
        });
        createZoomToolbar();
    }

    private void createZoomToolbar() {
        Button zoomInBtn = new Button("+");
        zoomInBtn.setOnAction(e -> viewModel.zoomIn());

        Button zoomOutBtn = new Button("\u2212");
        zoomOutBtn.setOnAction(e -> viewModel.zoomOut());

        Button fitAllBtn = new Button("Fit All");
        fitAllBtn.setOnAction(e -> viewModel.fitAll(
                mapCanvas.getWidth(), mapCanvas.getHeight()));

        zoomLabel = new Label("100%");

        HBox toolbar = new HBox(4, zoomOutBtn, zoomInBtn, fitAllBtn, zoomLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(4));
        toolbar.setStyle("-fx-background-color: rgba(245,245,245,0.9);");
        toolbar.setLayoutX(BACK_BUTTON_PADDING);
        toolbar.setLayoutY(BACK_BUTTON_PADDING);
        toolbar.setMouseTransparent(false);
        toolbar.setId("zoomToolbar");

        // Add toolbar directly to mapCanvas so it floats on top
        mapCanvas.getChildren().add(toolbar);
    }

    private void renderAllNotes() {
        if (rendering) {
            return;
        }
        rendering = true;
        try {
            mapCanvas.getChildren().clear();
            nodeMap.clear();
            for (NoteDisplayItem item : viewModel.getNoteItems()) {
                StackPane n = createNoteNode(item);
                nodeMap.put(item.getId(), n);
                mapCanvas.getChildren().add(n);
            }
            mapCanvas.getChildren().add(backButton);
        } finally {
            rendering = false;
        }
    }

    private void onNoteItemsChanged(
            ListChangeListener.Change<? extends NoteDisplayItem> change) {
        if (rendering) {
            return;
        }
        while (change.next()) {
            if (change.wasPermutated()) {
                renderAllNotes();
                return;
            }
            if (change.wasReplaced()) {
                // Collect added IDs so we can detect which removed items are stale
                Set<UUID> addedIds = new HashSet<>();
                for (NoteDisplayItem item : change.getAddedSubList()) {
                    addedIds.add(item.getId());
                    StackPane existing = nodeMap.get(item.getId());
                    if (existing != null) {
                        updateNoteNode(existing, item);
                    } else {
                        renderAllNotes();
                        return;
                    }
                }
                // Remove nodes for items that were removed but not re-added
                for (NoteDisplayItem removed : change.getRemoved()) {
                    if (!addedIds.contains(removed.getId())) {
                        StackPane node = nodeMap.remove(removed.getId());
                        if (node != null) {
                            mapCanvas.getChildren().remove(node);
                        }
                    }
                }
            } else {
                if (change.wasRemoved()) {
                    for (NoteDisplayItem removed : change.getRemoved()) {
                        StackPane node = nodeMap.remove(removed.getId());
                        if (node != null) {
                            mapCanvas.getChildren().remove(node);
                        }
                    }
                }
                if (change.wasAdded()) {
                    int backIdx = mapCanvas.getChildren().indexOf(backButton);
                    if (backIdx < 0) {
                        backIdx = mapCanvas.getChildren().size();
                    }
                    for (NoteDisplayItem item : change.getAddedSubList()) {
                        StackPane noteNode = createNoteNode(item);
                        nodeMap.put(item.getId(), noteNode);
                        mapCanvas.getChildren().add(backIdx, noteNode);
                        backIdx++;
                    }
                }
            }
        }
    }

    /** Updates an existing node in-place. */
    private void updateNoteNode(StackPane notePane, NoteDisplayItem item) {
        // Update position
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());

        // Update rectangle (child 0)
        if (notePane.getChildren().get(0) instanceof Rectangle rect) {
            rect.setWidth(item.getWidth());
            rect.setHeight(item.getHeight());
            rect.setFill(Color.web(item.getColorHex()));
        }

        // Update text labels (child 1 is VBox) — only present in non-OVERVIEW tiers
        if (notePane.getChildren().size() > 1
                && notePane.getChildren().get(1) instanceof VBox textBox) {
            textBox.setMaxWidth(item.getWidth());
            textBox.setMaxHeight(item.getHeight());

            // Update clip
            if (textBox.getClip() instanceof Rectangle clip) {
                clip.setWidth(item.getWidth());
                clip.setHeight(item.getHeight());
            }

            // Update title label (first child of VBox)
            if (!textBox.getChildren().isEmpty()
                    && textBox.getChildren().get(0) instanceof Label titleLabel) {
                titleLabel.setText(item.getTitle());
                titleLabel.setMaxWidth(item.getWidth() - 8);
            }

            // Update content label if present (second child of VBox)
            if (textBox.getChildren().size() > 1
                    && textBox.getChildren().get(1)
                            instanceof Label contentLabel) {
                contentLabel.setText(
                        item.getContent() != null ? item.getContent() : "");
                contentLabel.setMaxWidth(item.getWidth() - 8);
            }
        }

        // Update badge label if present (child 2)
        if (notePane.getChildren().size() > 2
                && notePane.getChildren().get(2) instanceof Label badgeLabel) {
            String badge = item.getBadge();
            badgeLabel.setText(badge != null ? badge : "");
        }

        // Update selection highlight
        if (item.getId().equals(viewModel.selectedNoteIdProperty().get())) {
            if (notePane.getChildren().get(0) instanceof Rectangle rect) {
                rect.setStrokeWidth(SELECTED_BORDER_WIDTH);
                rect.setStroke(Color.DODGERBLUE);
            }
        }
    }

    private StackPane createNoteNode(NoteDisplayItem item) {
        ZoomTier tier = viewModel.getCurrentTier();

        Rectangle rect = new Rectangle(item.getWidth(), item.getHeight());
        rect.setFill(Color.web(item.getColorHex()));
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(NORMAL_BORDER_WIDTH);
        rect.setArcWidth(4);
        rect.setArcHeight(4);

        StackPane notePane;

        if (!tier.isShowTitle()) {
            // OVERVIEW: rectangle only, no labels
            notePane = new StackPane(rect);
        } else {
            double fontSize = tier.getTitleFontSize();
            Label titleLabel = new Label(item.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
            titleLabel.setTextAlignment(TextAlignment.LEFT);
            titleLabel.setAlignment(Pos.TOP_LEFT);
            titleLabel.setMaxWidth(item.getWidth() - 8);
            titleLabel.setWrapText(true);
            titleLabel.setMouseTransparent(false);
            titleLabel.setPadding(new Insets(4, 4, 2, 4));

            VBox textBox = new VBox(titleLabel);

            if (tier.isShowContent()) {
                String content = item.getContent();
                if (content != null && !content.isEmpty()) {
                    double contentSize = tier == ZoomTier.DETAILED
                            ? DETAILED_CONTENT_FONT_SIZE : CONTENT_FONT_SIZE;
                    Label contentLabel = new Label(content);
                    contentLabel.setFont(Font.font("System", contentSize));
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
            }

            textBox.setMaxWidth(item.getWidth());
            textBox.setMaxHeight(item.getHeight());
            textBox.setAlignment(Pos.TOP_LEFT);

            // Clip the text container to the rectangle bounds
            Rectangle clip = new Rectangle(item.getWidth(), item.getHeight());
            textBox.setClip(clip);

            notePane = new StackPane(rect, textBox);

            // Badge label in top-right corner
            if (tier.isShowBadge()) {
                String badge = item.getBadge();
                if (badge != null && !badge.isEmpty()) {
                    Label badgeLabel = new Label(badge);
                    badgeLabel.setFont(Font.font("System", fontSize));
                    badgeLabel.setMouseTransparent(true);
                    badgeLabel.setPadding(new Insets(2, 4, 0, 0));
                    StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
                    notePane.getChildren().add(badgeLabel);
                }
            }
        }

        notePane.setUserData(item.getId());
        notePane.setAlignment(Pos.TOP_LEFT);
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        notePane.setCursor(Cursor.HAND);

        // Drag support – returns a flag array so click handlers can check
        // whether the gesture was a drag rather than a click.
        final boolean[] dragging = enableDrag(notePane, item);

        // Double-click handler at the notePane (StackPane) level so the VBox
        // cannot intercept events that should reach the rectangle.
        // For tiers with title labels, clicking on title starts inline edit;
        // anything else drills down.
        notePane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY
                    && !dragging[0]) {
                if (tier.isShowTitle() && notePane.getChildren().size() > 1) {
                    VBox tb = (VBox) notePane.getChildren().get(1);
                    Label tl = (Label) tb.getChildren().get(0);
                    Rectangle rc = (Rectangle) notePane.getChildren().get(0);
                    if (event.getTarget() == tl
                            || isDescendantOf(event.getTarget(), tl)) {
                        InlineEditHelper.startInlineEdit(
                                notePane, tl, rc, item, viewModel,
                                mapCanvas);
                    } else {
                        viewModel.drillDown(item.getId());
                    }
                } else {
                    viewModel.drillDown(item.getId());
                }
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

    /** Installs drag handlers; returns flag array true during drag. */
    private boolean[] enableDrag(StackPane notePane, NoteDisplayItem item) {
        final double[] dragDelta = new double[2];
        final boolean[] dragging = {false};

        notePane.setOnMousePressed(event -> {
            dragDelta[0] = notePane.getLayoutX() - event.getSceneX();
            dragDelta[1] = notePane.getLayoutY() - event.getSceneY();
            dragging[0] = false;
            notePane.toFront();
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

    /** Checks if target is a descendant of ancestor. */
    private static boolean isDescendantOf(Object target, javafx.scene.Node ancestor) {
        if (!(target instanceof javafx.scene.Node node)) {
            return false;
        }
        javafx.scene.Node current = node.getParent();
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void highlightSelected(StackPane selected) {
        for (Node child : mapCanvas.getChildren()) {
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
