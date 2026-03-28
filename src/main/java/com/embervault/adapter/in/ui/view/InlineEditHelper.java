package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Handles inline title editing for note nodes in the Map view.
 *
 * <p>Extracted from {@link MapViewController} to keep the controller
 * within the file length limit.</p>
 */
final class InlineEditHelper {

    private static final double TITLE_FONT_SIZE = 14.0;

    private InlineEditHelper() {
        // utility class
    }

    /**
     * Starts inline editing of a note's title label.
     */
    static void startInlineEdit(StackPane notePane, Label titleLabel,
            Rectangle rect, NoteDisplayItem item,
            MapViewModel viewModel, Pane mapCanvas) {
        TextField textField = new TextField(titleLabel.getText());
        textField.setFont(Font.font("System", FontWeight.BOLD, TITLE_FONT_SIZE));
        textField.setAlignment(Pos.CENTER_LEFT);
        textField.setMaxWidth(rect.getWidth() - 8);
        textField.selectAll();

        VBox textBox = (VBox) notePane.getChildren().get(1);
        int titleIndex = textBox.getChildren().indexOf(titleLabel);
        textBox.getChildren().set(titleIndex, textField);
        textField.requestFocus();

        Runnable commitEdit = () -> {
            String newTitle = textField.getText().trim();
            if (!newTitle.isEmpty()
                    && viewModel.renameNote(item.getId(), newTitle)) {
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

        textField.setOnAction(e -> {
            commitEdit.run();
            NoteDisplayItem newItem = viewModel.createSiblingNote(
                    item.getId(), "");
            Platform.runLater(() -> {
                for (Node child : mapCanvas.getChildren()) {
                    if (child instanceof StackPane sp
                            && newItem.getId().equals(sp.getUserData())) {
                        startInlineEditOnNode(sp, newItem, viewModel,
                                mapCanvas);
                        break;
                    }
                }
            });
        });

        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit.run();
                e.consume();
            }
        });

        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitEdit.run();
            }
        });
    }

    /**
     * Starts inline editing on a note pane found by ID after a re-render.
     */
    static void startInlineEditOnNode(StackPane notePane,
            NoteDisplayItem item, MapViewModel viewModel, Pane mapCanvas) {
        VBox textBox = (VBox) notePane.getChildren().get(1);
        Label titleLabel = (Label) textBox.getChildren().get(0);
        Rectangle rect = (Rectangle) notePane.getChildren().get(0);
        startInlineEdit(notePane, titleLabel, rect, item, viewModel,
                mapCanvas);
    }
}
