package com.embervault.adapter.in.ui.view;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Outline view.
 *
 * <p>Renders notes as a hierarchical tree. Notes are selectable.
 * New child notes can be created by pressing Return.</p>
 */
public class OutlineViewController {

    private static final Logger LOG = LoggerFactory.getLogger(OutlineViewController.class);

    @FXML private TreeView<NoteDisplayItem> outlineTreeView;

    private OutlineViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(OutlineViewModel viewModel) {
        this.viewModel = viewModel;

        // Enable editing on the tree view
        outlineTreeView.setEditable(true);

        // Set up cell factory with editable support
        outlineTreeView.setCellFactory(tv -> new EditableNoteTreeCell());

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

        // Return key to create new child note under selected
        outlineTreeView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                createChildUnderSelected();
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
     * Custom TreeCell that supports inline editing of note titles.
     */
    private final class EditableNoteTreeCell extends TreeCell<NoteDisplayItem> {

        private TextField textField;

        @Override
        public void startEdit() {
            super.startEdit();
            if (getItem() == null) {
                return;
            }
            textField = new TextField(getItem().getTitle());
            textField.selectAll();
            textField.setOnAction(e -> commitEdit(getItem()));
            textField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                    e.consume();
                }
            });
            textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    cancelEdit();
                }
            });

            setText(null);
            setGraphic(textField);
            textField.requestFocus();
        }

        @Override
        public void commitEdit(NoteDisplayItem item) {
            String newTitle = textField.getText().trim();
            if (!newTitle.isEmpty()) {
                viewModel.renameNote(item.getId(), newTitle);
            }
            super.commitEdit(item);
            setText(item.getTitle());
            setGraphic(null);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            if (getItem() != null) {
                setText(getItem().getTitle());
            }
            setGraphic(null);
        }

        @Override
        protected void updateItem(NoteDisplayItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                setText(null);
                setGraphic(textField);
            } else {
                setText(item.getTitle());
                setGraphic(null);
            }
        }
    }
}
