package com.embervault.adapter.in.ui.view;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.StampEditorViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Stamp Editor dialog.
 *
 * <p>Provides a simple CRUD interface for managing stamps. The left panel
 * shows a list of stamps, the right panel allows editing name and action.
 * All domain access is mediated through the {@link StampEditorViewModel}.</p>
 */
public class StampEditorViewController {

    private static final Logger LOG = LoggerFactory.getLogger(
            StampEditorViewController.class);

    @FXML private SplitPane editorRoot;
    @FXML private ListView<String> stampListView;
    @FXML private TextField nameField;
    @FXML private TextField actionField;
    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Button saveButton;

    private StampEditorViewModel viewModel;
    private UUID selectedStampId;

    /**
     * Injects the ViewModel and loads existing stamps.
     *
     * @param stampEditorViewModel the stamp editor view model
     */
    public void initViewModel(StampEditorViewModel stampEditorViewModel) {
        this.viewModel = stampEditorViewModel;
        stampListView.setItems(viewModel.getStampNames());

        stampListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onStampSelected(newVal));

        viewModel.refresh();
    }

    @FXML
    void onAddStamp() {
        String name = nameField.getText();
        String action = actionField.getText();
        if (name == null || name.isBlank()
                || action == null || action.isBlank()) {
            LOG.warn("Cannot create stamp: name and action "
                    + "must not be blank");
            return;
        }
        viewModel.createStamp(name, action);
        nameField.clear();
        actionField.clear();
        selectedStampId = null;
    }

    @FXML
    void onRemoveStamp() {
        if (selectedStampId != null) {
            viewModel.deleteStamp(selectedStampId);
            nameField.clear();
            actionField.clear();
            selectedStampId = null;
        }
    }

    @FXML
    void onSaveStamp() {
        if (selectedStampId == null) {
            onAddStamp();
            return;
        }
        String name = nameField.getText();
        String action = actionField.getText();
        if (name == null || name.isBlank()
                || action == null || action.isBlank()) {
            LOG.warn("Cannot save stamp: name and action "
                    + "must not be blank");
            return;
        }
        viewModel.updateStamp(selectedStampId, name, action);
        selectedStampId = null;
    }

    private void onStampSelected(String name) {
        if (name == null) {
            selectedStampId = null;
            return;
        }
        selectedStampId = viewModel.getIdForName(name);
        if (selectedStampId != null) {
            nameField.setText(name);
            actionField.setText(viewModel.getActionForName(name));
        }
    }
}
