package com.embervault.adapter.in.ui.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
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
import javafx.scene.effect.DropShadow;
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
    private static final double BADGE_FONT_SIZE = 16.0;

    private static final double BACK_BUTTON_PADDING = 5.0;
    private static final double SCROLL_ZOOM_FACTOR = 1.1;
    private static final double DETAILED_CONTENT_FONT_SIZE = 14.0;
    private static final double ZOOM_PERCENTAGE = 100.0;

    @FXML private Pane mapCanvas;

    private MapViewModel viewModel;
    private Button backButton;
    private HBox zoomToolbar;
    private final Map<UUID, StackPane> nodeMap = new HashMap<>();
    private Scale zoomScale;
    private Label zoomLabel;
    private PauseTransition zoomRenderDebounce;
    private boolean rendering;
    private ViewColorConfig currentColors;
    private Consumer<String> onViewSwitch;

    /** Sets the callback invoked when the user selects a view-switch menu item. */
    public void setOnViewSwitch(Consumer<String> callback) {
        this.onViewSwitch = callback;
    }

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

        mapCanvas.setOnScroll(event -> {
            double factor = event.getDeltaY() > 0
                    ? SCROLL_ZOOM_FACTOR : 1.0 / SCROLL_ZOOM_FACTOR;
            double newZoom = viewModel.zoomLevelProperty().get() * factor;
            zoomScale.setPivotX(event.getX());
            zoomScale.setPivotY(event.getY());
            viewModel.setZoomLevel(newZoom);
            event.consume();
        });

        mapCanvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                viewModel.createChildNote("Untitled");
            } else if (event.getCode() == KeyCode.ESCAPE
                    && viewModel.canNavigateBackProperty().get()) {
                viewModel.navigateBack();
            }
        });

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

        ContextMenu menu = new ContextMenu(createNote);
        menu.getItems().addAll(
                ViewSwitchMenuHelper.createViewSwitchItems(
                        ViewType.MAP, onViewSwitch));
        return menu;
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

        zoomToolbar = new HBox(4, zoomOutBtn, zoomInBtn, fitAllBtn, zoomLabel);
        zoomToolbar.setAlignment(Pos.CENTER_LEFT);
        zoomToolbar.setPadding(new Insets(4));
        zoomToolbar.setStyle("-fx-background-color: rgba(245,245,245,0.9);");
        zoomToolbar.setLayoutX(BACK_BUTTON_PADDING);
        zoomToolbar.setLayoutY(BACK_BUTTON_PADDING);
        zoomToolbar.setMouseTransparent(false);
        zoomToolbar.setId("zoomToolbar");

        mapCanvas.getChildren().add(zoomToolbar);
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
            mapCanvas.getChildren().addAll(backButton, zoomToolbar);
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

    private void updateNoteNode(StackPane notePane, NoteDisplayItem item) {
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        if (notePane.getChildren().get(0) instanceof Rectangle rect) {
            rect.setWidth(item.getWidth());
            rect.setHeight(item.getHeight());
            rect.setFill(Color.web(item.getColorHex()));
        }
        if (notePane.getChildren().size() > 1
                && notePane.getChildren().get(1) instanceof VBox textBox) {
            textBox.setMaxWidth(item.getWidth());
            textBox.setMaxHeight(item.getHeight());
            if (textBox.getClip() instanceof Rectangle clip) {
                clip.setWidth(item.getWidth());
                clip.setHeight(item.getHeight());
            }
            if (!textBox.getChildren().isEmpty()
                    && textBox.getChildren().get(0) instanceof Label titleLabel) {
                titleLabel.setText(item.getTitle());
                titleLabel.setMaxWidth(item.getWidth() - 8);
            }
            if (textBox.getChildren().size() > 1
                    && textBox.getChildren().get(1)
                            instanceof Label contentLabel) {
                contentLabel.setText(
                        item.getContent() != null ? item.getContent() : "");
                contentLabel.setMaxWidth(item.getWidth() - 8);
            }
        }
        String badge = item.getBadge();
        ZoomTier tier = viewModel.getCurrentTier();
        if (notePane.getChildren().size() > 2
                && notePane.getChildren().get(2) instanceof Label badgeLabel) {
            badgeLabel.setText(badge != null ? badge : "");
        } else if (tier.isShowBadge() && badge != null && !badge.isEmpty()) {
            notePane.getChildren().add(createBadgeLabel(badge, item));
        }
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
        rect.setStroke(currentColors != null
                ? Color.web(currentColors.borderColor()) : Color.BLACK);
        rect.setStrokeWidth(NORMAL_BORDER_WIDTH);
        rect.setArcWidth(4);
        rect.setArcHeight(4);

        StackPane notePane;
        if (!tier.isShowTitle()) {
            notePane = new StackPane(rect);
            notePane.setAlignment(Pos.TOP_LEFT);
        } else {
            double fontSize = tier.getTitleFontSize();
            Label titleLabel = new Label(item.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, fontSize));
            titleLabel.setTextFill(Color.web(
                    ViewColorConfig.contrastTextColor(item.getColorHex())));
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
                    contentLabel.setTextFill(Color.web(
                            ViewColorConfig.contrastTextColor(
                                    item.getColorHex())));
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
            Rectangle clip = new Rectangle(item.getWidth(), item.getHeight());
            textBox.setClip(clip);
            notePane = new StackPane(rect, textBox);
            notePane.setAlignment(Pos.TOP_LEFT);
            String badge = item.getBadge();
            if (tier.isShowBadge() && badge != null && !badge.isEmpty()) {
                notePane.getChildren().add(createBadgeLabel(badge, item));
            }
        }
        notePane.setUserData(item.getId());
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        notePane.setCursor(Cursor.HAND);
        final boolean[] dragging = enableDrag(notePane, item);
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
        if (item.getId().equals(viewModel.selectedNoteIdProperty().get())) {
            rect.setStrokeWidth(SELECTED_BORDER_WIDTH);
            rect.setStroke(currentColors != null
                    ? Color.web(currentColors.selectionColor())
                    : Color.DODGERBLUE);
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

    private Label createBadgeLabel(String badge, NoteDisplayItem item) {
        Label l = new Label(badge);
        l.setFont(Font.font(BADGE_FONT_SIZE));
        l.setTextFill(Color.web(ViewColorConfig.contrastTextColor(item.getColorHex())));
        l.setEffect(new DropShadow(2, Color.gray(0.3, 0.6)));
        l.setMouseTransparent(true);
        StackPane.setAlignment(l, Pos.TOP_RIGHT);
        StackPane.setMargin(l, new Insets(2, 4, 0, 0));
        return l;
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
        Color borderCol = currentColors != null
                ? Color.web(currentColors.borderColor()) : Color.BLACK;
        Color selCol = currentColors != null
                ? Color.web(currentColors.selectionColor())
                : Color.DODGERBLUE;
        for (Node child : mapCanvas.getChildren()) {
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
     * Applies a color scheme to the map view.
     * @param colors the view color config to apply
     */
    public void applyColorScheme(ViewColorConfig colors) {
        this.currentColors = colors;
        mapCanvas.setStyle("-fx-background-color: "
                + colors.canvasBackground() + ";");
        zoomToolbar.setStyle("-fx-background-color: "
                + colors.toolbarBackground() + ";");
        renderAllNotes();
    }
}
