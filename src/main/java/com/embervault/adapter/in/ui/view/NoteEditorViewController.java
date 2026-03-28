package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.AttributeEntry;
import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXML controller for the Note Editor view.
 *
 * <p>Binds title, text, and attribute fields to the NoteEditorViewModel.
 * Changes are saved on focus-lost or Enter key press.</p>
 */
public class NoteEditorViewController {

    private static final double ATTR_LABEL_WIDTH = 120;

    @FXML private VBox editorRoot;
    @FXML private TextField titleField;
    @FXML private TextArea textArea;
    @FXML private VBox attributesBox;

    private NoteEditorViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     *
     * @param viewModel the note editor view model
     */
    public void initViewModel(NoteEditorViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind title field
        titleField.setText(viewModel.titleProperty().get());
        viewModel.titleProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!titleField.getText().equals(newVal)) {
                        titleField.setText(newVal);
                    }
                });

        // Save title on focus lost
        titleField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                viewModel.saveTitle(titleField.getText());
            }
        });

        // Save title on Enter
        titleField.setOnAction(e -> viewModel.saveTitle(titleField.getText()));

        // Bind text area
        textArea.setText(viewModel.textProperty().get());
        viewModel.textProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!textArea.getText().equals(newVal)) {
                        textArea.setText(newVal);
                    }
                });

        // Save text on focus lost
        textArea.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                viewModel.saveText(textArea.getText());
            }
        });

        // Render attribute editors when they change
        viewModel.getEditableAttributes().addListener(
                (ListChangeListener<AttributeEntry>) change -> renderAttributes());

        // Initial render
        renderAttributes();
    }

    /** Returns the associated ViewModel. */
    public NoteEditorViewModel getViewModel() {
        return viewModel;
    }

    private void renderAttributes() {
        attributesBox.getChildren().clear();

        for (AttributeEntry entry : viewModel.getEditableAttributes()) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(entry.getName());
            nameLabel.setMinWidth(ATTR_LABEL_WIDTH);
            nameLabel.setMaxWidth(ATTR_LABEL_WIDTH);

            TextField valueField = new TextField(entry.getValue());
            HBox.setHgrow(valueField, Priority.ALWAYS);

            // Save on focus lost
            String attrName = entry.getName();
            valueField.focusedProperty().addListener(
                    (obs, wasFocused, isFocused) -> {
                        if (!isFocused) {
                            viewModel.saveAttribute(
                                    attrName, valueField.getText());
                        }
                    });

            // Save on Enter
            valueField.setOnAction(e ->
                    viewModel.saveAttribute(attrName, valueField.getText()));

            row.getChildren().addAll(nameLabel, valueField);
            attributesBox.getChildren().add(row);
        }
    }
}
