package com.embervault.adapter.in.ui.view;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.TextAlignment;

/**
 * FXML controller for the Outline view.
 *
 * <p>Renders notes as a hierarchical tree. Notes are selectable.
 * New child notes can be created by pressing Return.</p>
 */
public class OutlineViewController {

    @FXML private TreeView<NoteDisplayItem> outlineTreeView;

    private OutlineViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(OutlineViewModel viewModel) {
        this.viewModel = viewModel;

        // Set up cell factory for single-line display with ellipsis
        outlineTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(NoteDisplayItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                    setTextAlignment(TextAlignment.LEFT);
                }
            }
        });

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
    }

    /** Returns the associated ViewModel. */
    public OutlineViewModel getViewModel() {
        return viewModel;
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
}
