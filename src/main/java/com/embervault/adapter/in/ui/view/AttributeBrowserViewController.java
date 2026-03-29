package com.embervault.adapter.in.ui.view;

import java.util.function.Consumer;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.CategoryItem;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * FXML controller for the Attribute Browser view.
 *
 * <p>Renders notes grouped by the selected attribute's values in a TreeView
 * with category headers as parent nodes and notes as children.</p>
 */
public class AttributeBrowserViewController {

    @FXML private VBox browserRoot;
    @FXML private ComboBox<String> attributeComboBox;
    @FXML private TreeView<String> categoryTreeView;

    private AttributeBrowserViewModel viewModel;
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
     *
     * @param viewModel the attribute browser view model
     */
    public void initViewModel(AttributeBrowserViewModel viewModel) {
        this.viewModel = viewModel;

        // Populate combo box
        attributeComboBox.setItems(viewModel.getAvailableAttributes());

        // On attribute selection change -> regroup
        attributeComboBox.valueProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        viewModel.setSelectedAttribute(newVal);
                    }
                });

        // Render categories when they change
        viewModel.getCategories().addListener(
                (ListChangeListener<CategoryItem>) change -> renderCategories());

        // On tree item selection -> set selected note
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null && newVal.getValue() != null
                            && newVal.isLeaf() && newVal.getParent() != null
                            && newVal.getParent().getParent() != null) {
                        // Find the NoteDisplayItem by title match
                        String noteTitle = newVal.getValue();
                        findNoteByTitle(noteTitle);
                    }
                });

        // Set up hidden root
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        categoryTreeView.setRoot(root);
        categoryTreeView.setShowRoot(false);

        // Context menu with view-switch items
        categoryTreeView.setContextMenu(createContextMenu());
    }

    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
                ViewSwitchMenuHelper.createViewSwitchItems(
                        ViewType.BROWSER, onViewSwitch));
        // Remove leading separator when there are no preceding items
        if (!menu.getItems().isEmpty()
                && menu.getItems().get(0)
                        instanceof javafx.scene.control.SeparatorMenuItem) {
            menu.getItems().remove(0);
        }
        return menu;
    }

    /** Returns the associated ViewModel. */
    public AttributeBrowserViewModel getViewModel() {
        return viewModel;
    }

    private void renderCategories() {
        TreeItem<String> root = categoryTreeView.getRoot();
        root.getChildren().clear();

        for (CategoryItem category : viewModel.getCategories()) {
            String header = category.value() + " (" + category.count() + ")";
            TreeItem<String> categoryNode = new TreeItem<>(header);
            categoryNode.setExpanded(true);

            for (NoteDisplayItem note : category.notes()) {
                String badge = note.getBadge();
                String displayTitle = (badge != null && !badge.isEmpty())
                        ? badge + " " + note.getTitle()
                        : note.getTitle();
                TreeItem<String> noteNode = new TreeItem<>(displayTitle);
                noteNode.setGraphic(null);
                categoryNode.getChildren().add(noteNode);
            }

            root.getChildren().add(categoryNode);
        }
    }

    private void findNoteByTitle(String displayTitle) {
        for (CategoryItem category : viewModel.getCategories()) {
            for (NoteDisplayItem note : category.notes()) {
                String badge = note.getBadge();
                String expected = (badge != null && !badge.isEmpty())
                        ? badge + " " + note.getTitle()
                        : note.getTitle();
                if (expected.equals(displayTitle)) {
                    viewModel.selectNote(note.getId());
                    return;
                }
            }
        }
    }
}
