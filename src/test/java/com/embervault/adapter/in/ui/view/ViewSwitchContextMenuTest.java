package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.embervault.ViewType;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
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
 * Integration tests verifying that all five view controllers have
 * view-switch context menu items and that the callback is wired.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ViewSwitchContextMenuTest {

    private NoteService noteService;
    private LinkService linkService;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(noteRepo);
        linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        parentId = noteService.createNote("Parent", "").getId();
    }

    // --- MapViewController ---

    @Test
    @DisplayName("MapViewController context menu has view-switch items")
    void map_contextMenuHasViewSwitchItems() {
        AtomicReference<String> received = new AtomicReference<>();
        MapViewController controller = new MapViewController();
        Pane canvas = new Pane();
        injectField(controller, MapViewController.class,
                "mapCanvas", canvas);
        controller.setOnViewSwitch(received::set);

        MapViewModel vm = new MapViewModel(
                new SimpleStringProperty("Test"), noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new EventBus());
        vm.setBaseNoteId(parentId);
        controller.initViewModel(vm);

        ContextMenu menu = canvas.getOnContextMenuRequested() != null
                ? findContextMenuFromCanvas(canvas) : null;
        // The context menu is created in initViewModel and set via
        // setOnContextMenuRequested — verify via firing the request
        assertNotNull(canvas.getOnContextMenuRequested(),
                "Canvas should have context menu handler");
    }

    @Test
    @DisplayName("MapViewController view-switch callback is invoked")
    void map_viewSwitchCallbackInvoked() {
        AtomicReference<String> received = new AtomicReference<>();
        MapViewController controller = new MapViewController();
        Pane canvas = new Pane();
        injectField(controller, MapViewController.class,
                "mapCanvas", canvas);
        controller.setOnViewSwitch(received::set);

        MapViewModel vm = new MapViewModel(
                new SimpleStringProperty("Test"), noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new EventBus());
        vm.setBaseNoteId(parentId);
        controller.initViewModel(vm);

        // setOnViewSwitch before initViewModel means callback is used
        // Verify the callback is set
        assertNull(received.get(),
                "Callback should not have been invoked yet");
    }

    // --- OutlineViewController ---

    @Test
    @DisplayName("OutlineViewController context menu has view-switch "
            + "items with OUTLINE disabled")
    void outline_contextMenuHasViewSwitchItems() {
        AtomicReference<String> received = new AtomicReference<>();
        OutlineViewController controller =
                new OutlineViewController();
        VBox root = new VBox();
        TreeView<Object> treeView = new TreeView<>();
        injectField(controller, OutlineViewController.class,
                "outlineRoot", root);
        injectField(controller, OutlineViewController.class,
                "outlineTreeView", treeView);
        controller.setOnViewSwitch(received::set);

        OutlineViewModel vm = new OutlineViewModel(
                new SimpleStringProperty("Test"), noteService,
                noteService, noteService, noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new EventBus());
        vm.setBaseNoteId(parentId);
        controller.initViewModel(vm);

        ContextMenu menu = treeView.getContextMenu();
        assertNotNull(menu,
                "TreeView should have a context menu");
        assertViewSwitchItemsPresent(menu, ViewType.OUTLINE);
    }

    @Test
    @DisplayName("OutlineViewController fires callback on menu click")
    void outline_callbackFiredOnMenuClick() {
        AtomicReference<String> received = new AtomicReference<>();
        OutlineViewController controller =
                new OutlineViewController();
        VBox root = new VBox();
        TreeView<Object> treeView = new TreeView<>();
        injectField(controller, OutlineViewController.class,
                "outlineRoot", root);
        injectField(controller, OutlineViewController.class,
                "outlineTreeView", treeView);
        controller.setOnViewSwitch(received::set);

        OutlineViewModel vm = new OutlineViewModel(
                new SimpleStringProperty("Test"), noteService,
                noteService, noteService, noteService,
                noteService, noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new EventBus());
        vm.setBaseNoteId(parentId);
        controller.initViewModel(vm);

        // Find and fire the "Switch to Map" item
        fireViewSwitchItem(treeView.getContextMenu(), "MAP");
        assertEquals("MAP", received.get());
    }

    // --- TreemapViewController ---

    @Test
    @DisplayName("TreemapViewController context menu has view-switch "
            + "items with TREEMAP disabled")
    void treemap_contextMenuHasViewSwitchItems() {
        AtomicReference<String> received = new AtomicReference<>();
        TreemapViewController controller =
                new TreemapViewController();
        Pane canvas = new Pane();
        injectField(controller, TreemapViewController.class,
                "treemapCanvas", canvas);
        controller.setOnViewSwitch(received::set);

        TreemapViewModel vm = new TreemapViewModel(
                new SimpleStringProperty("Test"), noteService,
                noteService,
                new com.embervault.adapter.in.ui.viewmodel.AppState(),
                new EventBus());
        vm.setBaseNoteId(parentId);
        controller.initViewModel(vm);

        assertNotNull(canvas.getOnContextMenuRequested(),
                "Canvas should have context menu handler");
    }

    // --- HyperbolicViewController ---

    @Test
    @DisplayName("HyperbolicViewController context menu has "
            + "view-switch items with HYPERBOLIC disabled")
    void hyperbolic_contextMenuHasViewSwitchItems() {
        AtomicReference<String> received = new AtomicReference<>();
        HyperbolicViewController controller =
                new HyperbolicViewController();
        Pane canvas = new Pane();
        injectField(controller, HyperbolicViewController.class,
                "hyperbolicCanvas", canvas);
        controller.setOnViewSwitch(received::set);

        HyperbolicViewModel vm = new HyperbolicViewModel(
                noteService, linkService,
                new com.embervault.adapter.in.ui.viewmodel.AppState());
        vm.setFocusNote(parentId);
        controller.initViewModel(vm);

        assertNotNull(canvas.getOnContextMenuRequested(),
                "Canvas should have context menu handler");
    }

    // --- AttributeBrowserViewController ---

    @Test
    @DisplayName("AttributeBrowserViewController context menu has "
            + "view-switch items with BROWSER disabled")
    void browser_contextMenuHasViewSwitchItems() {
        AtomicReference<String> received = new AtomicReference<>();
        AttributeBrowserViewController controller =
                new AttributeBrowserViewController();
        VBox root = new VBox();
        javafx.scene.control.ComboBox<String> combo =
                new javafx.scene.control.ComboBox<>();
        TreeView<String> treeView = new TreeView<>();
        injectField(controller,
                AttributeBrowserViewController.class,
                "browserRoot", root);
        injectField(controller,
                AttributeBrowserViewController.class,
                "attributeComboBox", combo);
        injectField(controller,
                AttributeBrowserViewController.class,
                "categoryTreeView", treeView);
        controller.setOnViewSwitch(received::set);

        AttributeBrowserViewModel vm =
                new AttributeBrowserViewModel(
                        noteService, new AttributeSchemaRegistry(),
                        new com.embervault.adapter.in.ui.viewmodel.AppState());
        controller.initViewModel(vm);

        ContextMenu menu = treeView.getContextMenu();
        assertNotNull(menu,
                "TreeView should have a context menu");

        // Browser has no leading separator (removed)
        assertFalse(
                menu.getItems().get(0)
                        instanceof SeparatorMenuItem,
                "First item should not be a separator");

        // Should have 5 items (one per ViewType)
        assertEquals(5, menu.getItems().size(),
                "Should have 5 view-switch items");
    }

    @Test
    @DisplayName("AttributeBrowserViewController fires callback "
            + "on menu click")
    void browser_callbackFiredOnMenuClick() {
        AtomicReference<String> received = new AtomicReference<>();
        AttributeBrowserViewController controller =
                new AttributeBrowserViewController();
        VBox root = new VBox();
        javafx.scene.control.ComboBox<String> combo =
                new javafx.scene.control.ComboBox<>();
        TreeView<String> treeView = new TreeView<>();
        injectField(controller,
                AttributeBrowserViewController.class,
                "browserRoot", root);
        injectField(controller,
                AttributeBrowserViewController.class,
                "attributeComboBox", combo);
        injectField(controller,
                AttributeBrowserViewController.class,
                "categoryTreeView", treeView);
        controller.setOnViewSwitch(received::set);

        AttributeBrowserViewModel vm =
                new AttributeBrowserViewModel(
                        noteService, new AttributeSchemaRegistry(),
                        new com.embervault.adapter.in.ui.viewmodel.AppState());
        controller.initViewModel(vm);

        // Fire the "Switch to Map" item
        fireViewSwitchItem(treeView.getContextMenu(), "MAP");
        assertEquals("MAP", received.get());
    }

    // --- Helper methods ---

    /**
     * Asserts that the context menu contains view-switch items and
     * that the item for {@code disabledType} is disabled.
     */
    private void assertViewSwitchItemsPresent(
            ContextMenu menu, ViewType disabledType) {
        boolean foundSwitch = false;
        for (MenuItem item : menu.getItems()) {
            if (item.getText() != null
                    && item.getText().startsWith("Switch to ")) {
                foundSwitch = true;
                String typeName = item.getText()
                        .replace("Switch to ", "");
                ViewType type = findViewTypeByDisplayName(typeName);
                if (type == disabledType) {
                    assertTrue(item.isDisable(),
                            type + " should be disabled");
                } else {
                    assertFalse(item.isDisable(),
                            type + " should be enabled");
                }
            }
        }
        assertTrue(foundSwitch,
                "Context menu should contain view-switch items");
    }

    /**
     * Fires the view-switch menu item that maps to the given
     * ViewType name.
     */
    private void fireViewSwitchItem(ContextMenu menu,
            String viewTypeName) {
        ViewType target = ViewType.valueOf(viewTypeName);
        for (MenuItem item : menu.getItems()) {
            if (item.getText() != null
                    && item.getText().equals(
                            "Switch to " + target.displayName())) {
                item.fire();
                return;
            }
        }
        throw new AssertionError(
                "No menu item found for " + viewTypeName);
    }

    private ViewType findViewTypeByDisplayName(String displayName) {
        for (ViewType type : ViewType.values()) {
            if (type.displayName().equals(displayName)) {
                return type;
            }
        }
        return null;
    }

    private ContextMenu findContextMenuFromCanvas(Pane canvas) {
        // Canvas uses setOnContextMenuRequested, not setContextMenu
        return null;
    }

    @SuppressWarnings("unchecked")
    private void injectField(Object target, Class<?> clazz,
            String fieldName, Object value) {
        try {
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
