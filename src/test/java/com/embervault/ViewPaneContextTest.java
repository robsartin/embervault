package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ViewPaneContextTest {

    private NoteService noteService;
    private LinkService linkService;
    private AttributeSchemaRegistry schemaRegistry;
    private SelectedNoteViewModel selectedNoteVm;
    private StringProperty rootNoteTitle;
    private Note rootNote;
    private MapViewModel mapViewModel;
    private ViewPaneContext paneContext;

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
        schemaRegistry = new AttributeSchemaRegistry();
        selectedNoteVm = new SelectedNoteViewModel(noteService);
        rootNoteTitle = new SimpleStringProperty("Root");

        rootNote = noteService.createNote("Root", "");
        noteService.createChildNote(rootNote.getId(), "Child1");

        mapViewModel = new MapViewModel(
                rootNoteTitle, noteService);
        mapViewModel.setBaseNoteId(rootNote.getId());
        mapViewModel.loadNotes();

        Label dummyView = new Label("map content");
        paneContext = new ViewPaneContext(
                ViewType.MAP,
                mapViewModel.tabTitleProperty(),
                dummyView,
                rootNote.getId(),
                mapViewModel::loadNotes);

        Runnable refreshAll = () -> { };
        ViewPaneDeps deps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                refreshAll, selectedNoteVm, rootNoteTitle);
        paneContext.setDeps(deps);
    }

    @Test
    @DisplayName("initial view type is MAP")
    void initialViewType_shouldBeMap() {
        assertEquals(ViewType.MAP,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("container has label and view as children")
    void container_shouldHaveTwoChildren() {
        VBox container = paneContext.getContainer();
        assertEquals(2, container.getChildren().size());
        assertTrue(container.getChildren().get(0)
                instanceof Label);
    }

    @Test
    @DisplayName("label text is bound to tab title property")
    void label_shouldBeBoundToTabTitle() {
        Label label = paneContext.getLabel();
        assertTrue(label.getText().startsWith("Map:"));
    }

    @Test
    @DisplayName("label has a context menu with 5 items")
    void label_shouldHaveContextMenu() {
        Label label = paneContext.getLabel();
        ContextMenu menu = label.getContextMenu();
        assertNotNull(menu);
        assertEquals(5, menu.getItems().size());
    }

    @Test
    @DisplayName("current view type menu item is disabled")
    void currentViewType_menuItemShouldBeDisabled() {
        ContextMenu menu = paneContext.getLabel()
                .getContextMenu();
        List<MenuItem> items = menu.getItems();

        // MAP is index 0 and should be disabled
        assertTrue(items.get(0).isDisable(),
                "MAP item should be disabled");
        assertFalse(items.get(1).isDisable(),
                "OUTLINE item should be enabled");
    }

    @Test
    @DisplayName("context menu items match view type names")
    void contextMenu_itemsShouldMatchViewTypeNames() {
        ContextMenu menu = paneContext.getLabel()
                .getContextMenu();
        ViewType[] types = ViewType.values();
        for (int i = 0; i < types.length; i++) {
            assertEquals(types[i].displayName(),
                    menu.getItems().get(i).getText());
        }
    }

    @Test
    @DisplayName("switchView to same type is a no-op")
    void switchView_sameType_shouldBeNoop() {
        paneContext.switchView(ViewType.MAP);
        assertEquals(ViewType.MAP,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("refreshCurrentView calls the view refresh")
    void refreshCurrentView_shouldCallRefresh() {
        AtomicInteger count = new AtomicInteger(0);
        ViewPaneContext ctx = new ViewPaneContext(
                ViewType.MAP,
                mapViewModel.tabTitleProperty(),
                new Label("test"),
                rootNote.getId(),
                count::incrementAndGet);
        ViewPaneDeps deps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                () -> { }, selectedNoteVm, rootNoteTitle);
        ctx.setDeps(deps);

        ctx.refreshCurrentView();
        assertEquals(1, count.get());

        ctx.refreshCurrentView();
        assertEquals(2, count.get());
    }

    @Test
    @DisplayName("switchView to OUTLINE changes view type")
    void switchView_toOutline_shouldChangeType() {
        paneContext.switchView(ViewType.OUTLINE);
        assertEquals(ViewType.OUTLINE,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("switchView to OUTLINE updates label binding")
    void switchView_toOutline_shouldUpdateLabel() {
        paneContext.switchView(ViewType.OUTLINE);
        assertTrue(paneContext.getLabel().getText()
                .startsWith("Outline:"));
    }

    @Test
    @DisplayName("switchView to TREEMAP changes view type")
    void switchView_toTreemap_shouldChangeType() {
        paneContext.switchView(ViewType.TREEMAP);
        assertEquals(ViewType.TREEMAP,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("switchView to HYPERBOLIC changes view type")
    void switchView_toHyperbolic_shouldChangeType() {
        paneContext.switchView(ViewType.HYPERBOLIC);
        assertEquals(ViewType.HYPERBOLIC,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("switchView to BROWSER changes view type")
    void switchView_toBrowser_shouldChangeType() {
        paneContext.switchView(ViewType.BROWSER);
        assertEquals(ViewType.BROWSER,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("after switch the context menu disables the new type")
    void switchView_shouldUpdateContextMenuDisabledItem() {
        paneContext.switchView(ViewType.OUTLINE);

        ContextMenu menu = paneContext.getLabel()
                .getContextMenu();
        // MAP (index 0) should now be enabled
        assertFalse(menu.getItems().get(0).isDisable());
        // OUTLINE (index 1) should now be disabled
        assertTrue(menu.getItems().get(1).isDisable());
    }

    @Test
    @DisplayName("container still has 2 children after switch")
    void switchView_containerShouldStillHaveTwoChildren() {
        paneContext.switchView(ViewType.TREEMAP);
        assertEquals(2,
                paneContext.getContainer().getChildren().size());
    }

    @Test
    @DisplayName("double switch preserves container structure")
    void doubleSwitch_shouldPreserveContainerStructure() {
        paneContext.switchView(ViewType.OUTLINE);
        paneContext.switchView(ViewType.TREEMAP);
        assertEquals(ViewType.TREEMAP,
                paneContext.getCurrentViewType());
        assertEquals(2,
                paneContext.getContainer().getChildren().size());
        assertTrue(paneContext.getLabel().getText()
                .startsWith("Treemap:"));
    }

    @Test
    @DisplayName("container has context menu request handler")
    void container_shouldHaveContextMenuHandler() {
        VBox container = paneContext.getContainer();
        assertNotNull(container.getOnContextMenuRequested(),
                "Container should handle context menu requests");
    }

    @Test
    @DisplayName("context menu item action triggers view switch")
    void contextMenuItem_shouldTriggerSwitch() {
        ContextMenu menu = paneContext.getLabel()
                .getContextMenu();
        // Fire the OUTLINE menu item action (index 1)
        menu.getItems().get(1).fire();
        assertEquals(ViewType.OUTLINE,
                paneContext.getCurrentViewType());
    }
}
