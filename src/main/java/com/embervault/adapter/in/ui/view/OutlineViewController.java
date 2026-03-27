package com.embervault.adapter.in.ui.view;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Outline view.
 *
 * <p>Renders notes as a hierarchical tree. Single-clicking an already-selected
 * note starts inline editing. Double-clicking drills down into a note.
 * Focus lost on the edit field commits the change; Escape cancels.</p>
 */
public class OutlineViewController {

    private static final Logger LOG = LoggerFactory.getLogger(OutlineViewController.class);

    @FXML private TreeView<NoteDisplayItem> outlineTreeView;
    @FXML private VBox outlineRoot;

    private OutlineViewModel viewModel;
    private Button backButton;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(OutlineViewModel viewModel) {
        this.viewModel = viewModel;

        // Back navigation button
        backButton = new Button("\u2190 Back");
        backButton.setVisible(false);
        backButton.setOnAction(e -> viewModel.navigateBack());
        viewModel.canNavigateBackProperty().addListener(
                (obs, oldVal, newVal) -> backButton.setVisible(newVal));
        outlineRoot.getChildren().add(0, backButton);

        // Do NOT use TreeView's built-in edit mode
        outlineTreeView.setEditable(false);

        // Set up cell factory with custom click handling
        outlineTreeView.setCellFactory(tv -> new ClickToEditNoteTreeCell());

        // Load initial data
        viewModel.loadNotes();
        buildTree();

        // Re-build tree when root items change
        viewModel.getRootItems().addListener(
                (ListChangeListener<NoteDisplayItem>) change -> buildTree());

        // Selection listener
        outlineTreeView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        viewModel.selectNote(newVal.getValue().getId());
                    } else {
                        viewModel.selectNote(null);
                    }
                });

        // Key handling: Enter creates child, Escape navigates back
        outlineTreeView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                createChildUnderSelected();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE
                    && viewModel.canNavigateBackProperty().get()) {
                viewModel.navigateBack();
                event.consume();
            }
        });

        // Context menu
        outlineTreeView.setContextMenu(createContextMenu());
    }

    /** Returns the associated ViewModel. */
    public OutlineViewModel getViewModel() {
        return viewModel;
    }

    private ContextMenu createContextMenu() {
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setOnAction(e -> createChildUnderSelected());

        MenuItem mapView = new MenuItem("Map View");
        mapView.setOnAction(e -> LOG.debug("Map View placeholder selected"));

        return new ContextMenu(createNote, new SeparatorMenuItem(), mapView);
    }

    private void buildTree() {
        // Root item represents the base note (hidden)
        TreeItem<NoteDisplayItem> rootTreeItem = new TreeItem<>();
        rootTreeItem.setExpanded(true);

        for (NoteDisplayItem item : viewModel.getRootItems()) {
            TreeItem<NoteDisplayItem> treeItem = buildTreeItem(item);
            rootTreeItem.getChildren().add(treeItem);
        }

        outlineTreeView.setRoot(rootTreeItem);
        outlineTreeView.setShowRoot(false);
    }

    private TreeItem<NoteDisplayItem> buildTreeItem(NoteDisplayItem item) {
        TreeItem<NoteDisplayItem> treeItem = new TreeItem<>(item);
        treeItem.setExpanded(true);

        if (item.isHasChildren()) {
            for (NoteDisplayItem child : viewModel.getChildren(item.getId())) {
                treeItem.getChildren().add(buildTreeItem(child));
            }
        }

        return treeItem;
    }

    private void createChildUnderSelected() {
        TreeItem<NoteDisplayItem> selected = outlineTreeView.getSelectionModel()
                .getSelectedItem();
        UUID parentId;
        if (selected != null && selected.getValue() != null) {
            parentId = selected.getValue().getId();
        } else {
            parentId = viewModel.getBaseNoteId();
        }
        if (parentId != null) {
            viewModel.createChildNote(parentId, "Untitled");
        }
    }

    /**
     * Custom TreeCell that uses single-click on a selected item to edit,
     * and double-click to drill down. Focus lost on the edit field commits changes.
     */
    private final class ClickToEditNoteTreeCell extends TreeCell<NoteDisplayItem> {

        private TextField textField;
        private boolean editing;

        ClickToEditNoteTreeCell() {
            setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY || isEmpty() || getItem() == null) {
                    return;
                }

                if (event.getClickCount() == 2) {
                    // Double-click -> drill down
                    viewModel.drillDown(getItem().getId());
                    event.consume();
                } else if (event.getClickCount() == 1 && isSelected() && !editing) {
                    // Single click on already-selected item -> start editing
                    startInlineEdit();
                    event.consume();
                }
            });
        }

        private void startInlineEdit() {
            if (getItem() == null) {
                return;
            }
            editing = true;
            textField = new TextField(getItem().getTitle());
            textField.selectAll();

            // Commit on Enter
            textField.setOnAction(e -> commitInlineEdit());

            // Cancel on Escape
            textField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    cancelInlineEdit();
                    e.consume();
                }
            });

            // Commit on focus lost
            textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused && editing) {
                    commitInlineEdit();
                }
            });

            setText(null);
            setGraphic(textField);
            textField.requestFocus();
        }

        private void commitInlineEdit() {
            if (!editing) {
                return;
            }
            editing = false;
            NoteDisplayItem item = getItem();
            if (item != null && textField != null) {
                String newTitle = textField.getText().trim();
                if (!newTitle.isEmpty()) {
                    viewModel.renameNote(item.getId(), newTitle);
                }
            }
            if (item != null) {
                setText(item.getTitle());
            }
            setGraphic(null);
            textField = null;
        }

        private void cancelInlineEdit() {
            if (!editing) {
                return;
            }
            editing = false;
            if (getItem() != null) {
                setText(getItem().getTitle());
            }
            setGraphic(null);
            textField = null;
        }

        @Override
        protected void updateItem(NoteDisplayItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                editing = false;
            } else if (editing && textField != null) {
                setText(null);
                setGraphic(textField);
            } else {
                setText(item.getTitle());
                setGraphic(null);
            }
        }
    }
}
