package com.embervault.adapter.in.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.ItemChangeProcessor;
import com.embervault.adapter.in.ui.viewmodel.MapViewInteractionState;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import com.embervault.adapter.in.ui.viewmodel.ZoomTier;
import javafx.animation.PauseTransition;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** FXML controller for the Map view. */
public class MapViewController {

    private static final Logger LOG = LoggerFactory.getLogger(MapViewController.class);
    private static final double SELECTED_BORDER_WIDTH = 3.0;
    private static final double NORMAL_BORDER_WIDTH = 1.0;

    private static final double BACK_BUTTON_PADDING = 5.0;
    private static final double SCROLL_ZOOM_FACTOR = 1.1;
    private static final double ZOOM_PERCENTAGE = 100.0;

    @FXML private Pane mapCanvas;

    private MapViewModel viewModel;
    private BreadcrumbBar breadcrumbBar;
    private HBox zoomToolbar;
    private final Map<UUID, StackPane> nodeMap = new HashMap<>();
    private Scale zoomScale;
    private Label zoomLabel;
    private PauseTransition zoomRenderDebounce;
    private final MapViewInteractionState interactionState =
            new MapViewInteractionState();
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

        breadcrumbBar = new BreadcrumbBar(
                viewModel.getBreadcrumbs(),
                viewModel::navigateToBreadcrumb);
        breadcrumbBar.setLayoutX(BACK_BUTTON_PADDING);
        breadcrumbBar.setLayoutY(BACK_BUTTON_PADDING);

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

        mapCanvas.setOnScroll(this::handleScrollZoom);

        mapCanvas.setOnKeyPressed(this::handleKeyPress);

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

    /** Handles scroll-to-zoom, adjusting zoom level toward the cursor position. */
    void handleScrollZoom(ScrollEvent event) {
        double factor = event.getDeltaY() > 0
                ? SCROLL_ZOOM_FACTOR : 1.0 / SCROLL_ZOOM_FACTOR;
        double newZoom = viewModel.zoomLevelProperty().get() * factor;
        zoomScale.setPivotX(event.getX());
        zoomScale.setPivotY(event.getY());
        viewModel.setZoomLevel(newZoom);
        event.consume();
    }

    /** Handles key presses: Enter creates a note, Escape navigates back. */
    void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            viewModel.createChildNote("Untitled");
        } else if (event.getCode() == KeyCode.ESCAPE
                && viewModel.canNavigateBackProperty().get()) {
            viewModel.navigateBack();
        }
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
        zoomToolbar.setLayoutY(BACK_BUTTON_PADDING + 24);
        zoomToolbar.setMouseTransparent(false);
        zoomToolbar.setId("zoomToolbar");

        mapCanvas.getChildren().add(zoomToolbar);
    }

    private void renderAllNotes() {
        if (!interactionState.tryBeginRender()) {
            return;
        }
        try {
            mapCanvas.getChildren().clear();
            nodeMap.clear();
            for (NoteDisplayItem item : viewModel.getNoteItems()) {
                StackPane n = createNoteNode(item);
                nodeMap.put(item.getId(), n);
                mapCanvas.getChildren().add(n);
            }
            mapCanvas.getChildren().addAll(breadcrumbBar, zoomToolbar);
        } finally {
            interactionState.endRender();
        }
    }

    private void onNoteItemsChanged(
            ListChangeListener.Change<? extends NoteDisplayItem> change) {
        if (interactionState.isRendering()) {
            return;
        }
        while (change.next()) {
            if (change.wasPermutated()) {
                renderAllNotes();
                return;
            }
            if (change.wasReplaced()) {
                ItemChangeProcessor.ReplacementResult result =
                        ItemChangeProcessor.classifyReplacement(
                                change.getAddedSubList(),
                                change.getRemoved(),
                                nodeMap.keySet());
                if (result.requiresFullRender()) {
                    renderAllNotes();
                    return;
                }
                for (NoteDisplayItem item : result.updatedItems()) {
                    StackPane existing = nodeMap.get(item.getId());
                    if (existing != null) {
                        updateNoteNode(existing, item);
                    }
                }
                for (UUID staleId : result.staleIds()) {
                    StackPane node = nodeMap.remove(staleId);
                    if (node != null) {
                        mapCanvas.getChildren().remove(node);
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
                    int backIdx = mapCanvas.getChildren().indexOf(breadcrumbBar);
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
        NoteNodeFactory.updateNoteNode(
                notePane, item, viewModel.getCurrentTier());
        if (item.getId().equals(viewModel.selectedNoteIdProperty().get())) {
            if (notePane.getChildren().get(0) instanceof Rectangle rect) {
                rect.setStrokeWidth(SELECTED_BORDER_WIDTH);
                rect.setStroke(Color.DODGERBLUE);
            }
        }
    }

    private StackPane createNoteNode(NoteDisplayItem item) {
        ZoomTier tier = viewModel.getCurrentTier();
        String borderColor = currentColors != null
                ? currentColors.borderColor() : "#000000";
        StackPane notePane = NoteNodeFactory.createRenderedNotePane(
                item, tier, borderColor);
        enableDrag(notePane, item);
        notePane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2
                    && event.getButton() == MouseButton.PRIMARY
                    && !interactionState.isDragging()) {
                if (tier.isShowTitle()
                        && notePane.getChildren().size() > 1
                        && notePane.getChildren().get(1)
                                instanceof VBox tb
                        && !tb.getChildren().isEmpty()
                        && tb.getChildren().get(0)
                                instanceof Label tl) {
                    Rectangle rc =
                            (Rectangle) notePane
                                    .getChildren().get(0);
                    if (event.getTarget() == tl
                            || isDescendantOf(
                                    event.getTarget(), tl)) {
                        InlineEditHelper.startInlineEdit(
                                notePane, tl, rc, item,
                                viewModel, mapCanvas);
                    } else {
                        viewModel.drillDown(item.getId());
                    }
                } else {
                    viewModel.drillDown(item.getId());
                }
                event.consume();
            }
        });
        if (item.getId().equals(
                viewModel.selectedNoteIdProperty().get())) {
            Rectangle rect =
                    (Rectangle) notePane.getChildren().get(0);
            rect.setStrokeWidth(SELECTED_BORDER_WIDTH);
            rect.setStroke(currentColors != null
                    ? Color.web(currentColors.selectionColor())
                    : Color.DODGERBLUE);
        }

        return notePane;
    }

    /** Installs drag handlers using the shared interaction state. */
    private void enableDrag(StackPane notePane, NoteDisplayItem item) {
        notePane.setOnMousePressed(event -> {
            interactionState.beginDrag(
                    notePane.getLayoutX(), notePane.getLayoutY(),
                    event.getSceneX(), event.getSceneY());
            notePane.toFront();
            viewModel.selectNote(item.getId());
            highlightSelected(notePane);
        });
        notePane.setOnMouseDragged(event -> {
            interactionState.updateDrag(event.getSceneX(), event.getSceneY());
            notePane.setLayoutX(interactionState.getDragX());
            notePane.setLayoutY(interactionState.getDragY());
            event.consume();
        });
        notePane.setOnMouseReleased(event -> {
            if (interactionState.isDragging()) {
                viewModel.updateNotePosition(item.getId(),
                        notePane.getLayoutX(), notePane.getLayoutY());
                event.consume();
            }
        });
    }


    /** Checks if target is a descendant of ancestor. */
    private static boolean isDescendantOf(Object target, Node ancestor) {
        if (!(target instanceof Node node)) {
            return false;
        }
        for (Node cur = node.getParent(); cur != null; cur = cur.getParent()) {
            if (cur == ancestor) {
                return true;
            }
        }
        return false;
    }

    private void highlightSelected(StackPane selected) {
        Color borderCol = currentColors != null ? Color.web(currentColors.borderColor()) : Color.BLACK;
        Color selCol = currentColors != null ? Color.web(currentColors.selectionColor()) : Color.DODGERBLUE;
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

    /** Applies a color scheme to the map view. */
    public void applyColorScheme(ViewColorConfig colors) {
        this.currentColors = colors;
        mapCanvas.setStyle("-fx-background-color: "
                + colors.canvasBackground() + ";");
        zoomToolbar.setStyle("-fx-background-color: "
                + colors.toolbarBackground() + ";");
        renderAllNotes();
    }
}
