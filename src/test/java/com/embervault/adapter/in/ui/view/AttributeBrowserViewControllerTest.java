package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.TbxColor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link AttributeBrowserViewController}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class AttributeBrowserViewControllerTest {

    private AttributeBrowserViewController controller;
    private AttributeBrowserViewModel viewModel;
    private NoteService noteService;
    private TreeView<String> categoryTreeView;
    private ComboBox<String> attributeComboBox;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        AttributeSchemaRegistry registry = new AttributeSchemaRegistry();
        viewModel = new AttributeBrowserViewModel(noteService, registry,
                new com.embervault.adapter.in.ui.viewmodel.AppState());

        controller = new AttributeBrowserViewController();
        categoryTreeView = new TreeView<>();
        attributeComboBox = new ComboBox<>();
        VBox browserRoot = new VBox();

        injectField("categoryTreeView", categoryTreeView);
        injectField("attributeComboBox", attributeComboBox);
        injectField("browserRoot", browserRoot);

        controller.initViewModel(viewModel);
    }

    @Test
    @DisplayName("tree has hidden root after init")
    void initViewModel_treeHasHiddenRoot() {
        assertNotNull(categoryTreeView.getRoot());
        assertFalse(categoryTreeView.isShowRoot());
    }

    @Test
    @DisplayName("combo box populated with available attributes")
    void initViewModel_comboBoxPopulated() {
        assertFalse(attributeComboBox.getItems().isEmpty(),
                "Combo box should have attribute options");
    }

    @Test
    @DisplayName("selecting attribute groups notes into categories")
    void selectAttribute_groupsNotes() {
        UUID note1 = noteService.createNote("Note A", "").getId();
        UUID note2 = noteService.createNote("Note B", "").getId();
        noteService.getNote(note1).ifPresent(n ->
                n.setAttribute(Attributes.COLOR,
                        new AttributeValue.ColorValue(
                                TbxColor.hex("#FF0000"))));
        noteService.getNote(note2).ifPresent(n ->
                n.setAttribute(Attributes.COLOR,
                        new AttributeValue.ColorValue(
                                TbxColor.hex("#FF0000"))));

        viewModel.setSelectedAttribute(Attributes.COLOR);

        TreeItem<String> root = categoryTreeView.getRoot();
        assertFalse(root.getChildren().isEmpty(),
                "Should have at least one category");
    }

    @Test
    @DisplayName("category header shows value and count")
    void renderCategories_headerShowsCount() {
        UUID note1 = noteService.createNote("Note A", "").getId();
        noteService.getNote(note1).ifPresent(n ->
                n.setAttribute(Attributes.COLOR,
                        new AttributeValue.ColorValue(
                                TbxColor.hex("#FF0000"))));

        viewModel.setSelectedAttribute(Attributes.COLOR);

        TreeItem<String> root = categoryTreeView.getRoot();
        if (!root.getChildren().isEmpty()) {
            String header = root.getChildren().get(0).getValue();
            assertNotNull(header);
            // Header should contain a count in parentheses
            assertEquals(true, header.contains("("),
                    "Header should contain count: " + header);
        }
    }

    @Test
    @DisplayName("getViewModel returns injected viewModel")
    void getViewModel_returnsInjected() {
        assertEquals(viewModel, controller.getViewModel());
    }

    @Test
    @DisplayName("setOnViewSwitch sets callback")
    void setOnViewSwitch_setsCallback() {
        controller.setOnViewSwitch(viewType -> { });
        // No exception means success; callback is stored for context menu
    }

    @Test
    @DisplayName("tree context menu is set after init")
    void initViewModel_contextMenuSet() {
        assertNotNull(categoryTreeView.getContextMenu(),
                "Context menu should be set");
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = AttributeBrowserViewController.class
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
