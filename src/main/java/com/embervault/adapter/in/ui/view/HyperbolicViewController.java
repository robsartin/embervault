package com.embervault.adapter.in.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicEdge;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.PositionedNode;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Hyperbolic view.
 *
 * <p>Renders notes as circles on a Poincare disk projection. Linked notes
 * are connected by lines. The focus note appears at the center with the
 * largest radius; peripheral notes shrink with distance.</p>
 */
public class HyperbolicViewController {

    private static final Logger LOG = LoggerFactory.getLogger(HyperbolicViewController.class);
    private static final double SMALL_NODE_THRESHOLD = 12.0;
    private static final double BACK_BUTTON_PADDING = 5.0;
    private static final double SELECTED_STROKE_WIDTH = 3.0;
    private static final double NORMAL_STROKE_WIDTH = 1.5;

    @FXML private Pane hyperbolicCanvas;

    private HyperbolicViewModel viewModel;
    private Button backButton;
    private Consumer<String> onViewSwitch;
    private double dragStartX;
    private double dragStartY;
    private double panOffsetX;
    private double panOffsetY;

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
     * @param viewModel the hyperbolic view model
     */
    public void initViewModel(HyperbolicViewModel viewModel) {
        this.viewModel = viewModel;

        // Back navigation button
        backButton = new Button("\u2190 Back");
        backButton.setVisible(false);
        backButton.setOnAction(e -> viewModel.navigateBack());
        backButton.setLayoutX(BACK_BUTTON_PADDING);
        backButton.setLayoutY(BACK_BUTTON_PADDING);
        viewModel.canNavigateBackProperty().addListener(
                (obs, oldVal, newVal) -> backButton.setVisible(newVal));

        // Update viewport radius when canvas resizes
        hyperbolicCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateViewportAndRerender();
        });
        hyperbolicCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateViewportAndRerender();
        });

        // Re-render when nodes change
        viewModel.getNodes().addListener(
                (ListChangeListener<PositionedNode>) change -> renderAll());

        // Drag background to pan
        hyperbolicCanvas.setOnMousePressed(event -> {
            if (event.getTarget() == hyperbolicCanvas) {
                dragStartX = event.getSceneX() - panOffsetX;
                dragStartY = event.getSceneY() - panOffsetY;
            }
        });
        hyperbolicCanvas.setOnMouseDragged(event -> {
            if (event.getTarget() == hyperbolicCanvas) {
                panOffsetX = event.getSceneX() - dragStartX;
                panOffsetY = event.getSceneY() - dragStartY;
                renderAll();
                event.consume();
            }
        });

        // Escape to navigate back
        hyperbolicCanvas.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE
                    && viewModel.canNavigateBackProperty().get()) {
                viewModel.navigateBack();
            }
        });

        // Context menu on canvas background
        ContextMenu contextMenu = createContextMenu();
        hyperbolicCanvas.setOnContextMenuRequested(event -> {
            if (event.getTarget() == hyperbolicCanvas) {
                contextMenu.show(hyperbolicCanvas,
                        event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });

        hyperbolicCanvas.setFocusTraversable(true);
        renderAll();
    }

    /** Returns the associated ViewModel. */
    public HyperbolicViewModel getViewModel() {
        return viewModel;
    }

    private void updateViewportAndRerender() {
        double width = hyperbolicCanvas.getWidth();
        double height = hyperbolicCanvas.getHeight();
        double radius = Math.min(width, height) / 2.0;
        if (radius > 0) {
            viewModel.setViewportRadius(radius);
            if (viewModel.getFocusNoteId() != null) {
                viewModel.setFocusNote(viewModel.getFocusNoteId());
            }
        }
    }

    private ContextMenu createContextMenu() {
        MenuItem focusItem = new MenuItem("Focus on Note");
        focusItem.setOnAction(e -> {
            UUID selected = viewModel.selectedNoteIdProperty().get();
            if (selected != null) {
                viewModel.drillDown(selected);
            }
        });

        MenuItem createLinkItem = new MenuItem("Create Link");
        createLinkItem.setOnAction(e ->
                LOG.debug("Create Link placeholder"));

        ContextMenu menu = new ContextMenu(focusItem, createLinkItem);
        menu.getItems().addAll(
                ViewSwitchMenuHelper.createViewSwitchItems(
                        ViewType.HYPERBOLIC, onViewSwitch));
        return menu;
    }

    private void renderAll() {
        hyperbolicCanvas.getChildren().clear();

        double centerX = hyperbolicCanvas.getWidth() / 2.0 + panOffsetX;
        double centerY = hyperbolicCanvas.getHeight() / 2.0 + panOffsetY;

        // Build position map for edge drawing
        Map<UUID, double[]> positionMap = new HashMap<>();
        for (PositionedNode node : viewModel.getNodes()) {
            positionMap.put(node.noteId(),
                    new double[]{centerX + node.x(), centerY + node.y()});
        }

        // Draw edges first (under nodes)
        for (HyperbolicEdge edge : viewModel.getEdges()) {
            double[] src = positionMap.get(edge.sourceId());
            double[] dst = positionMap.get(edge.destinationId());
            if (src != null && dst != null) {
                Line line = HyperbolicNodeFactory.createEdgeLine(
                        src[0], src[1], dst[0], dst[1]);
                hyperbolicCanvas.getChildren().add(line);
            }
        }

        // Draw nodes
        for (PositionedNode node : viewModel.getNodes()) {
            double nx = centerX + node.x();
            double ny = centerY + node.y();

            Circle circle = HyperbolicNodeFactory.createNodeCircle(
                    nx, ny, node.size(), "#4A90D9");
            circle.setUserData(node.noteId());

            // Click to select
            circle.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 2) {
                        viewModel.drillDown(node.noteId());
                    } else {
                        viewModel.selectNote(node.noteId());
                        highlightSelected(node.noteId());
                    }
                    event.consume();
                }
            });

            // Context menu on node
            circle.setOnContextMenuRequested(event -> {
                viewModel.selectNote(node.noteId());
                ContextMenu nodeMenu = createNodeContextMenu(node.noteId());
                nodeMenu.show(circle, event.getScreenX(), event.getScreenY());
                event.consume();
            });

            hyperbolicCanvas.getChildren().add(circle);

            // Label (only if node is large enough)
            if (node.size() >= SMALL_NODE_THRESHOLD) {
                String title = noteTitle(node.noteId());
                String badge = noteBadge(node.noteId());
                String labelText = badge.isEmpty() ? title : badge + " " + title;
                Label label = HyperbolicNodeFactory.createNodeLabel(
                        labelText, nx, ny, node.size());
                hyperbolicCanvas.getChildren().add(label);
            } else {
                // Tooltip for small nodes
                String title = noteTitle(node.noteId());
                String badge = noteBadge(node.noteId());
                String tipText = badge.isEmpty() ? title : badge + " " + title;
                Tooltip.install(circle, new Tooltip(tipText));
            }
        }

        // Keep back button on top
        hyperbolicCanvas.getChildren().add(backButton);
    }

    private ContextMenu createNodeContextMenu(UUID noteId) {
        MenuItem focusItem = new MenuItem("Focus on Note");
        focusItem.setOnAction(e -> viewModel.drillDown(noteId));

        MenuItem createLinkItem = new MenuItem("Create Link");
        createLinkItem.setOnAction(e ->
                LOG.debug("Create Link to {} placeholder", noteId));

        ContextMenu menu = new ContextMenu(focusItem, createLinkItem);
        menu.getItems().addAll(
                ViewSwitchMenuHelper.createViewSwitchItems(
                        ViewType.HYPERBOLIC, onViewSwitch));
        return menu;
    }

    private void highlightSelected(UUID selectedId) {
        for (javafx.scene.Node child : hyperbolicCanvas.getChildren()) {
            if (child instanceof Circle c) {
                if (selectedId.equals(c.getUserData())) {
                    c.setStrokeWidth(SELECTED_STROKE_WIDTH);
                    c.setStroke(Color.GOLD);
                } else {
                    c.setStrokeWidth(NORMAL_STROKE_WIDTH);
                    c.setStroke(Color.WHITE);
                }
            }
        }
    }

    private String noteTitle(UUID noteId) {
        String title = viewModel.getNoteTitle(noteId);
        return title.isEmpty() ? noteId.toString().substring(0, 8) : title;
    }

    private String noteBadge(UUID noteId) {
        return viewModel.getNoteBadge(noteId);
    }

    /**
     * Applies a color scheme to the hyperbolic view.
     *
     * @param colors the view color config to apply
     */
    public void applyColorScheme(ViewColorConfig colors) {
        hyperbolicCanvas.setStyle("-fx-background-color: "
                + colors.canvasBackground() + ";");
        renderAll();
    }
}
