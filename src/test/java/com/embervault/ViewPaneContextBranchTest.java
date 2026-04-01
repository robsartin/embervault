package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import com.embervault.adapter.in.ui.viewmodel.AppState;
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
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Branch coverage tests for {@link ViewPaneContext}.
 *
 * <p>Covers switching to all 5 view types, context menu updates,
 * replaceView with 1 vs 2 children, and refresh after switch.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ViewPaneContextBranchTest {

    private NoteService noteService;
    private LinkService linkService;
    private AttributeSchemaRegistry schemaRegistry;
    private SelectedNoteViewModel selectedNoteVm;
    private StringProperty rootNoteTitle;
    private Note rootNote;
    private MapViewModel mapViewModel;
    private ViewPaneContext paneContext;
    private AtomicInteger refreshCount;

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
        AppState appState = new AppState();
        selectedNoteVm =
                new SelectedNoteViewModel(noteService, appState);
        rootNoteTitle = new SimpleStringProperty("Root");
        refreshCount = new AtomicInteger(0);

        rootNote = noteService.createNote("Root", "");
        noteService.createChildNote(
                rootNote.getId(), "Child1");

        mapViewModel = new MapViewModel(
                rootNoteTitle, noteService, appState);
        mapViewModel.setBaseNoteId(rootNote.getId());
        mapViewModel.loadNotes();

        Label dummyView = new Label("map content");
        paneContext = new ViewPaneContext(
                ViewType.MAP,
                mapViewModel.tabTitleProperty(),
                dummyView,
                rootNote.getId(),
                mapViewModel::loadNotes);

        ViewPaneDeps deps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                appState, selectedNoteVm, rootNoteTitle);
        paneContext.setDeps(deps);
    }

    // --- Switch to each view type ---

    @Test
    @DisplayName("switch MAP -> OUTLINE -> MAP round trip")
    void switch_mapOutlineMap_roundTrip(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.OUTLINE));
        assertEquals(ViewType.OUTLINE,
                paneContext.getCurrentViewType());

        robot.interact(
                () -> paneContext.switchView(ViewType.MAP));
        assertEquals(ViewType.MAP,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("switch to TREEMAP updates type and label")
    void switch_toTreemap_updatesTypeAndLabel(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.TREEMAP));
        assertEquals(ViewType.TREEMAP,
                paneContext.getCurrentViewType());
        assertTrue(paneContext.getLabel().getText()
                .startsWith("Treemap:"),
                "Label should start with Treemap:");
    }

    @Test
    @DisplayName("switch to HYPERBOLIC updates type and label")
    void switch_toHyperbolic_updatesTypeAndLabel(
            FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(
                        ViewType.HYPERBOLIC));
        assertEquals(ViewType.HYPERBOLIC,
                paneContext.getCurrentViewType());
        assertTrue(paneContext.getLabel().getText()
                .startsWith("Hyperbolic"),
                "Label should start with Hyperbolic");
    }

    @Test
    @DisplayName("switch to BROWSER updates type and label")
    void switch_toBrowser_updatesTypeAndLabel(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.BROWSER));
        assertEquals(ViewType.BROWSER,
                paneContext.getCurrentViewType());
        assertTrue(paneContext.getLabel().getText()
                .startsWith("Browser"),
                "Label should start with Browser");
    }

    // --- Same view type no-op ---

    @Test
    @DisplayName("switch to same type is a no-op")
    void switch_sameType_noop(FxRobot robot) {
        VBox container = paneContext.getContainer();
        int childCount = container.getChildren().size();

        robot.interact(
                () -> paneContext.switchView(ViewType.MAP));

        assertEquals(ViewType.MAP,
                paneContext.getCurrentViewType());
        assertEquals(childCount,
                container.getChildren().size(),
                "Container should not change for same type");
    }

    // --- Context menu after switch ---

    @Test
    @DisplayName("context menu disables current type "
            + "after switch")
    void contextMenu_disablesCurrentTypeAfterSwitch(
            FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.OUTLINE));

        ContextMenu menu =
                paneContext.getLabel().getContextMenu();
        for (MenuItem item : menu.getItems()) {
            if (item.getText().equals("Outline")) {
                assertTrue(item.isDisable(),
                        "OUTLINE should be disabled");
            } else {
                assertFalse(item.isDisable(),
                        item.getText() + " should be enabled");
            }
        }
    }

    @Test
    @DisplayName("context menu has 5 items for all view types")
    void contextMenu_hasFiveItems(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(
                        ViewType.HYPERBOLIC));

        ContextMenu menu =
                paneContext.getLabel().getContextMenu();
        assertEquals(5, menu.getItems().size(),
                "Should have menu items for all 5 view types");
    }

    // --- Container structure ---

    @Test
    @DisplayName("container has label + view after switch")
    void container_hasLabelAndViewAfterSwitch(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.TREEMAP));

        VBox container = paneContext.getContainer();
        assertEquals(2, container.getChildren().size(),
                "Container should have label + view");
        assertTrue(
                container.getChildren().get(0) instanceof Label,
                "First child should be the label");
    }

    // --- refreshCurrentView after switch ---

    @Test
    @DisplayName("refreshCurrentView works after switching "
            + "to OUTLINE")
    void refreshAfterSwitch_outline(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.OUTLINE));

        // Should not throw
        robot.interact(
                () -> paneContext.refreshCurrentView());

        assertEquals(ViewType.OUTLINE,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("refreshCurrentView works after switching "
            + "to TREEMAP")
    void refreshAfterSwitch_treemap(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.TREEMAP));

        robot.interact(
                () -> paneContext.refreshCurrentView());

        assertEquals(ViewType.TREEMAP,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("refreshCurrentView works after switching "
            + "to BROWSER")
    void refreshAfterSwitch_browser(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.BROWSER));

        robot.interact(
                () -> paneContext.refreshCurrentView());

        assertEquals(ViewType.BROWSER,
                paneContext.getCurrentViewType());
    }

    // --- setDeps null check ---

    @Test
    @DisplayName("setDeps with null throws "
            + "NullPointerException")
    void setDeps_nullThrows() {
        ViewPaneContext ctx = new ViewPaneContext(
                ViewType.MAP,
                mapViewModel.tabTitleProperty(),
                new Label("test"),
                rootNote.getId(),
                () -> { });
        assertThrows(NullPointerException.class,
                () -> ctx.setDeps(null));
    }

    // --- Multiple switches ---

    @Test
    @DisplayName("cycling through all view types preserves "
            + "container structure")
    void cycleAllViews_preservesStructure(FxRobot robot) {
        for (ViewType type : ViewType.values()) {
            if (type == ViewType.MAP) {
                continue;
            }
            robot.interact(
                    () -> paneContext.switchView(type));
            assertEquals(type,
                    paneContext.getCurrentViewType());
            assertEquals(2,
                    paneContext.getContainer()
                            .getChildren().size(),
                    "Container should have 2 children for "
                            + type);
        }
    }

    // --- replaceView with only 1 child ---

    @Test
    @DisplayName("initial container has exactly 2 children")
    void initialContainer_hasTwoChildren() {
        assertEquals(2,
                paneContext.getContainer().getChildren().size(),
                "Initial container should have label + view");
    }

    // --- Label binding ---

    @Test
    @DisplayName("label is bound to tab title after switch")
    void label_boundToTabTitleAfterSwitch(FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(ViewType.OUTLINE));

        Label label = paneContext.getLabel();
        assertNotNull(label.getText(),
                "Label text should not be null");
        assertFalse(label.getText().isEmpty(),
                "Label text should not be empty");
    }
}
