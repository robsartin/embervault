package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.NoteViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML controller that binds UI controls to the {@link NoteViewModel}.
 *
 * <p>Contains no business logic; all actions delegate to the ViewModel.
 * Uses {@link NoteDisplayItem} (from the viewmodel package) rather than
 * domain entities, satisfying ADR-0013.</p>
 */
public class NoteViewController {

    @FXML private ListView<NoteDisplayItem> noteListView;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private NoteViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(NoteViewModel viewModel) {
        this.viewModel = viewModel;

        noteListView.setItems(viewModel.getNotes());
        titleField.textProperty().bindBidirectional(viewModel.titleProperty());
        contentArea.textProperty().bindBidirectional(viewModel.contentProperty());

        noteListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> viewModel.selectNote(newVal));

        viewModel.loadNotes();
    }

    @FXML
    void onAdd() {
        viewModel.addNote();
    }

    @FXML
    void onSave() {
        viewModel.saveNote();
    }

    @FXML
    void onDelete() {
        viewModel.deleteNote();
    }
}
