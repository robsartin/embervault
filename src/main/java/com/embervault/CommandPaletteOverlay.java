package com.embervault;

import com.embervault.adapter.in.ui.viewmodel.CommandPaletteViewModel;
import com.embervault.adapter.in.ui.viewmodel.ShortcutAction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Overlay UI for the command palette.
 *
 * <p>A centered popup with a search field and a list of matching
 * shortcut actions. Bound to a {@link CommandPaletteViewModel}.</p>
 */
final class CommandPaletteOverlay {

    private static final double PALETTE_WIDTH = 400;
    private static final double PALETTE_MAX_HEIGHT = 300;

    private final StackPane root;

    CommandPaletteOverlay(CommandPaletteViewModel viewModel) {
        TextField searchField = new TextField();
        searchField.setPromptText("Type a command...");
        searchField.textProperty()
                .bindBidirectional(viewModel.queryProperty());

        ListView<ShortcutAction> listView = new ListView<>();
        listView.setItems(viewModel.getFilteredActions());
        listView.setMaxHeight(PALETTE_MAX_HEIGHT);
        listView.setCellFactory(lv -> new ShortcutCell());

        VBox palette = new VBox(4, searchField, listView);
        palette.setPadding(new Insets(8));
        palette.setMaxWidth(PALETTE_WIDTH);
        palette.setMaxHeight(PALETTE_MAX_HEIGHT + 50);
        palette.setStyle(
                "-fx-background-color: -fx-background;"
                + " -fx-border-color: -fx-accent;"
                + " -fx-border-radius: 4;"
                + " -fx-background-radius: 4;"
                + " -fx-effect: dropshadow(gaussian, "
                + "rgba(0,0,0,0.3), 10, 0, 0, 4);");

        root = new StackPane(palette);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(80, 0, 0, 0));
        root.visibleProperty().bind(viewModel.visibleProperty());
        root.managedProperty().bind(viewModel.visibleProperty());

        // Focus search field when shown
        viewModel.visibleProperty().addListener((obs, was, is) -> {
            if (is) {
                root.setPickOnBounds(true);
                searchField.requestFocus();
            } else {
                root.setPickOnBounds(false);
            }
        });

        // Escape hides the palette
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                viewModel.hide();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                if (!listView.getItems().isEmpty()) {
                    ShortcutAction selected =
                            listView.getSelectionModel()
                                    .getSelectedItem();
                    if (selected == null) {
                        selected = listView.getItems().get(0);
                    }
                    viewModel.executeSelected(selected);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                listView.requestFocus();
                listView.getSelectionModel().selectFirst();
                event.consume();
            }
        });

        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                viewModel.hide();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                ShortcutAction selected =
                        listView.getSelectionModel()
                                .getSelectedItem();
                if (selected != null) {
                    viewModel.executeSelected(selected);
                }
                event.consume();
            }
        });

        // Click on backdrop hides palette
        root.setOnMouseClicked(event -> {
            if (event.getTarget() == root) {
                viewModel.hide();
            }
        });
    }

    Node getNode() {
        return root;
    }

    private static final class ShortcutCell
            extends ListCell<ShortcutAction> {
        @Override
        protected void updateItem(ShortcutAction item,
                boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                Label nameLabel = new Label(item.name());
                nameLabel.setStyle("-fx-font-weight: bold;");
                Label keyLabel =
                        new Label(item.keyCombination());
                keyLabel.setStyle(
                        "-fx-text-fill: -fx-mid-text-color;");
                HBox row = new HBox(nameLabel);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                nameLabel.setMaxWidth(Double.MAX_VALUE);
                row.getChildren().add(keyLabel);
                row.setSpacing(8);
                setGraphic(row);
            }
        }
    }
}
