package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * FXML controller for the Text Pane view.
 *
 * <p>Binds the title and text fields to the SelectedNoteViewModel.
 * Shows a placeholder when no note is selected, and saves changes
 * on focus-lost or Enter key press.</p>
 */
public class TextPaneViewController {

    @FXML private VBox textPaneRoot;
    @FXML private Label placeholderLabel;
    @FXML private TextField titleField;
    @FXML private TextArea textArea;

    private SelectedNoteViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     *
     * @param viewModel the selected note view model
     */
    public void initViewModel(SelectedNoteViewModel viewModel) {
        this.viewModel = viewModel;

        // Show/hide placeholder vs editor based on selection
        viewModel.selectedNoteIdProperty().addListener(
                (obs, oldVal, newVal) -> updateVisibility(newVal != null));
        updateVisibility(viewModel.selectedNoteIdProperty().get() != null);

        // Bind title field
        titleField.setText(viewModel.titleProperty().get());
        viewModel.titleProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!titleField.getText().equals(newVal)) {
                        titleField.setText(newVal);
                    }
                });

        // Save title on focus lost
        titleField.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        viewModel.saveTitle(titleField.getText());
                    }
                });

        // Save title on Enter
        titleField.setOnAction(
                e -> viewModel.saveTitle(titleField.getText()));

        // Bind text area
        textArea.setText(viewModel.textProperty().get());
        viewModel.textProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!textArea.getText().equals(newVal)) {
                        textArea.setText(newVal);
                    }
                });

        // Save text on focus lost
        textArea.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        viewModel.saveText(textArea.getText());
                    }
                });
    }

    /** Returns the associated ViewModel. */
    public SelectedNoteViewModel getViewModel() {
        return viewModel;
    }

    private void updateVisibility(boolean noteSelected) {
        placeholderLabel.setVisible(!noteSelected);
        placeholderLabel.setManaged(!noteSelected);
        titleField.setVisible(noteSelected);
        titleField.setManaged(noteSelected);
        textArea.setVisible(noteSelected);
        textArea.setManaged(noteSelected);
    }
}
