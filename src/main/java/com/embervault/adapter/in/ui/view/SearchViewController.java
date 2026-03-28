package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the search bar.
 *
 * <p>Binds to a {@link SearchViewModel} and triggers search on each keystroke.
 * Escape closes the bar, Enter selects the first result, and clicking a result
 * selects it in the active view.</p>
 */
public class SearchViewController {

    private static final Logger LOG =
            LoggerFactory.getLogger(SearchViewController.class);

    @FXML private VBox searchRoot;
    @FXML private TextField searchField;
    @FXML private Button closeButton;
    @FXML private ListView<NoteDisplayItem> resultsList;

    private SearchViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     *
     * @param viewModel the search view model
     */
    public void initViewModel(SearchViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind visibility
        searchRoot.visibleProperty().bind(viewModel.visibleProperty());
        searchRoot.managedProperty().bind(viewModel.visibleProperty());

        // Bind search field text to query
        searchField.textProperty().bindBidirectional(
                viewModel.queryProperty());

        // Search on each keystroke
        searchField.textProperty().addListener(
                (obs, oldVal, newVal) -> {
                    LOG.debug("Search query changed: {}", newVal);
                    viewModel.search(newVal);
                });

        // Enter selects first result
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    && !viewModel.getResults().isEmpty()) {
                NoteDisplayItem first = viewModel.getResults().get(0);
                viewModel.selectResult(first.getId());
            } else if (event.getCode() == KeyCode.ESCAPE) {
                viewModel.hide();
            }
        });

        // Bind results list
        resultsList.setItems(viewModel.getResults());

        // Custom cell factory for result display
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NoteDisplayItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String badge = item.getBadge().isEmpty()
                            ? "" : item.getBadge() + " ";
                    setText(badge + item.getTitle());
                }
            }
        });

        // Click result to select
        resultsList.setOnMouseClicked(event -> {
            NoteDisplayItem selected = resultsList.getSelectionModel()
                    .getSelectedItem();
            if (selected != null) {
                viewModel.selectResult(selected.getId());
            }
        });

        // Hide results list when empty
        viewModel.getResults().addListener(
                (ListChangeListener<NoteDisplayItem>) change -> {
                    boolean hasResults = !viewModel.getResults().isEmpty();
                    resultsList.setVisible(hasResults);
                    resultsList.setManaged(hasResults);
                });
        resultsList.setVisible(false);
        resultsList.setManaged(false);

        // When search bar becomes visible, focus the text field
        viewModel.visibleProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.TRUE.equals(newVal)) {
                searchField.requestFocus();
            }
        });
    }

    /** Handles the close button click. */
    @FXML
    private void handleClose() {
        viewModel.hide();
    }
}
