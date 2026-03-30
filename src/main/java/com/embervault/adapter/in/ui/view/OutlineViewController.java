package com.embervault.adapter.in.ui.view;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML controller for the Outline view.
 *
 * <p>Renders notes as a hierarchical tree. Single-clicking any note
 * immediately starts inline editing. Double-clicking drills down into a note.
 * Enter creates a new sibling, Tab indents, Shift+Tab outdents.
 * Focus lost on the edit field commits the change; Escape cancels.</p>
 */
public class OutlineViewController {

    private static final Logger LOG = LoggerFactory.getLogger(OutlineViewController.class);

    @FXML private TreeView<NoteDisplayItem> outlineTreeView;
    @FXML private VBox outlineRoot;

    private OutlineViewModel viewModel;
    private Button backButton;
    private Consumer<String> onViewSwitch;

    /**
     * Sets the callback invoked when the user selects a view-switch
     * menu item. The callback receives the {@link ViewType} name.
     *
     * @param callback the view-switch callback
     */
    public void setOnViewSwitch(Consumer<String> callback) {
        this.onViewSwitch = callback;
    }

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
        outlineTreeView.setCellFactory(tv -> new OutlineNoteTreeCell());

        // Load initial data
        viewModel.loadNotes();
        buildTree();

        // Re-build tree when root items change
        viewModel.getRootItems().addListener(
                (ListChangeListener<NoteDisplayItem>) change -> buildTree());

        // Selection listener
        outlineTreeView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) ->
                        handleTreeSelection(newVal));

        // Event filter to intercept Tab/Shift+Tab before TreeView handles them
        outlineTreeView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTreeKeyFilter);

        // Key handling: Escape navigates back
        outlineTreeView.setOnKeyPressed(this::handleTreeKeyPress);

        // Context menu
        outlineTreeView.setContextMenu(createContextMenu());
    }

    /** Returns the associated ViewModel. */
    public OutlineViewModel getViewModel() {
        return viewModel;
    }

    /** Handles key presses on the tree view: Escape navigates back. */
    void handleTreeKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE
                && viewModel.canNavigateBackProperty().get()) {
            viewModel.navigateBack();
            event.consume();
        }
    }

    /** Handles tree selection changes, updating the ViewModel selection. */
    void handleTreeSelection(TreeItem<NoteDisplayItem> newVal) {
        if (newVal != null && newVal.getValue() != null) {
            viewModel.selectNote(newVal.getValue().getId());
        } else {
            viewModel.selectNote(null);
        }
    }

    private void handleTreeKeyFilter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER
                && !isAnyoneEditing()) {
            TreeItem<NoteDisplayItem> selected =
                    outlineTreeView.getSelectionModel()
                            .getSelectedItem();
            if (selected != null && selected.getValue() != null) {
                NoteDisplayItem newItem =
                        viewModel.createSiblingNote(
                                selected.getValue().getId(), "");
                refreshAndEdit(newItem.getId());
                event.consume();
            }
        } else if (event.getCode() == KeyCode.TAB) {
            TreeItem<NoteDisplayItem> selected =
                    outlineTreeView.getSelectionModel()
                            .getSelectedItem();
            if (selected != null && selected.getValue() != null) {
                UUID noteId = selected.getValue().getId();
                if (event.isShiftDown()) {
                    viewModel.outdentNote(noteId);
                } else {
                    viewModel.indentNote(noteId);
                }
                event.consume();
            }
        }
    }

    private ContextMenu createContextMenu() {
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setOnAction(e -> createChildUnderSelected());

        ContextMenu menu = new ContextMenu(createNote);
        menu.getItems().addAll(
                ViewSwitchMenuHelper.createViewSwitchItems(
                        ViewType.OUTLINE, onViewSwitch));
        return menu;
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
     * Rebuilds the tree, selects the given note, and starts editing it.
     */
    private void refreshAndEdit(UUID noteIdToSelect) {
        TreeItem<NoteDisplayItem> target = findTreeItem(
                outlineTreeView.getRoot(), noteIdToSelect);
        if (target != null) {
            outlineTreeView.getSelectionModel().select(target);
            // Use Platform.runLater so the tree has settled before we trigger edit
            Platform.runLater(() -> {
                int row = outlineTreeView.getRow(target);
                if (row >= 0) {
                    OutlineNoteTreeCell cell = findCellForItem(target);
                    if (cell != null) {
                        cell.startInlineEdit();
                    }
                }
            });
        }
    }

    private TreeItem<NoteDisplayItem> findTreeItem(TreeItem<NoteDisplayItem> root,
            UUID noteId) {
        if (root == null || noteId == null) {
            return null;
        }
        if (root.getValue() != null && noteId.equals(root.getValue().getId())) {
            return root;
        }
        for (TreeItem<NoteDisplayItem> child : root.getChildren()) {
            TreeItem<NoteDisplayItem> found = findTreeItem(child, noteId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private boolean isAnyoneEditing() {
        for (var node : outlineTreeView.lookupAll(".tree-cell")) {
            if (node instanceof OutlineNoteTreeCell cell
                    && cell.editing) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private OutlineNoteTreeCell findCellForItem(TreeItem<NoteDisplayItem> target) {
        int row = outlineTreeView.getRow(target);
        if (row < 0) {
            return null;
        }
        // Look up the cell via the TreeView's lookup mechanism
        for (var node : outlineTreeView.lookupAll(".tree-cell")) {
            if (node instanceof OutlineNoteTreeCell cell
                    && cell.getTreeItem() == target) {
                return cell;
            }
        }
        return null;
    }

    /**
     * Custom TreeCell that starts editing on single click.
     * Enter creates a sibling, Tab/Shift+Tab indent/outdent,
     * Escape cancels, focus lost commits.
     */
    private final class OutlineNoteTreeCell extends TreeCell<NoteDisplayItem> {

        private TextField textField;
        private boolean editing;

        OutlineNoteTreeCell() {
            setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY
                        || isEmpty() || getItem() == null) {
                    return;
                }

                if (event.getClickCount() == 2) {
                    viewModel.drillDown(getItem().getId());
                    event.consume();
                } else if (event.getClickCount() == 1
                        && !editing) {
                    startInlineEdit();
                    event.consume();
                }
            });

            setOnDragDetected(event -> {
                if (getItem() == null || editing) {
                    return;
                }
                Dragboard db = startDragAndDrop(
                        TransferMode.MOVE);
                ClipboardContent content =
                        new ClipboardContent();
                content.putString(
                        getItem().getId().toString());
                db.setContent(content);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this
                        && event.getDragboard().hasString()) {
                    event.acceptTransferModes(
                            TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != this
                        && event.getDragboard().hasString()) {
                    setStyle("-fx-border-color: dodgerblue; "
                            + "-fx-border-width: 0 0 2 0;");
                }
            });

            setOnDragExited(event -> setStyle(""));

            setOnDragDropped(event -> {
                handleDragDropped(event);
                event.consume();
            });
        }

        void startInlineEdit() {
            if (getItem() == null) {
                return;
            }
            editing = true;
            textField = new TextField(getItem().getTitle());
            textField.selectAll();

            // Key handling on the text field
            textField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEditKeyPress);

            // Focus lost: always commit
            textField.focusedProperty().addListener(
                    (obs, wasFocused, isFocused) -> {
                        if (!isFocused && editing) {
                            commitInlineEdit();
                        }
                    });

            setText(null);
            setGraphic(textField);
            textField.requestFocus();
        }

        private void handleEditKeyPress(KeyEvent event) {
            if (event.getCode() == KeyCode.ENTER) {
                NoteDisplayItem currentItem = getItem();
                commitInlineEdit();
                if (currentItem != null) {
                    NoteDisplayItem newItem = viewModel.createSiblingNote(
                            currentItem.getId(), "");
                    refreshAndEdit(newItem.getId());
                }
                event.consume();
            } else if (event.getCode() == KeyCode.TAB) {
                NoteDisplayItem currentItem = getItem();
                commitInlineEdit();
                if (currentItem != null) {
                    if (event.isShiftDown()) {
                        viewModel.outdentNote(currentItem.getId());
                    } else {
                        viewModel.indentNote(currentItem.getId());
                    }
                    refreshAndEdit(currentItem.getId());
                }
                event.consume();
            } else if (event.getCode() == KeyCode.BACK_SPACE
                    && textField.getText().isEmpty()) {
                UUID noteId = getItem().getId();
                if (!viewModel.hasChildren(noteId)) {
                    UUID previousId = viewModel.getPreviousNoteId(noteId);
                    editing = false;
                    setText(null);
                    setGraphic(null);
                    textField = null;
                    viewModel.deleteNote(noteId);
                    if (previousId != null) {
                        refreshAndEdit(previousId);
                    }
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                commitInlineEdit();
                outlineTreeView.requestFocus();
                event.consume();
            }
        }

        private void handleDragDropped(DragEvent event) {
            Dragboard db = event.getDragboard();
            if (!db.hasString() || getItem() == null) {
                event.setDropCompleted(false);
                return;
            }
            UUID draggedId = UUID.fromString(db.getString());
            TreeItem<NoteDisplayItem> target = getTreeItem();
            if (target == null || target.getParent() == null) {
                event.setDropCompleted(false);
                return;
            }
            TreeItem<NoteDisplayItem> parent =
                    target.getParent();
            UUID parentId;
            if (parent.getValue() != null) {
                parentId = parent.getValue().getId();
            } else {
                parentId = viewModel.getBaseNoteId();
            }
            int position = parent.getChildren().indexOf(target);
            viewModel.moveNoteToPosition(
                    draggedId, parentId, position);
            event.setDropCompleted(true);
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
                setText(badgedTitle(item));
            }
            setGraphic(null);
            textField = null;
        }

        private String badgedTitle(NoteDisplayItem item) {
            String badge = item.getBadge();
            if (badge != null && !badge.isEmpty()) {
                return badge + " " + item.getTitle();
            }
            return item.getTitle();
        }

        @Override
        protected void updateItem(NoteDisplayItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                if (editing && textField != null) {
                    commitInlineEdit();
                }
                setText(null);
                setGraphic(null);
                editing = false;
            } else if (editing && textField != null) {
                setText(null);
                setGraphic(textField);
            } else {
                setText(badgedTitle(item));
                setGraphic(null);
            }
        }
    }

    /**
     * Applies a color scheme to the outline view.
     *
     * @param colors the view color config to apply
     */
    public void applyColorScheme(ViewColorConfig colors) {
        outlineRoot.setStyle("-fx-background-color: "
                + colors.panelBackground() + ";");
        outlineTreeView.setStyle("-fx-background-color: "
                + colors.panelBackground() + "; -fx-control-inner-background: "
                + colors.panelBackground() + ";");
    }
}
