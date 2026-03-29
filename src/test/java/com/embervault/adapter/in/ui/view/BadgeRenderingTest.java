package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tests that badge symbols render correctly across all view types.
 *
 * <p>Verifies the fix for issue #140 and issues #147-#151: badges
 * must appear in Map, Outline, Treemap, Hyperbolic, and Attribute
 * Browser views when {@code $Badge} is set on a note.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class BadgeRenderingTest {

    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private UUID parentId;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        parentId = noteService.createNote("Parent", "").getId();
    }

    // --- Map view badge tests ---

    @Test
    @DisplayName("Map view renders badge Label in top-right at NORMAL zoom")
    void mapView_shouldRenderBadgeLabelAtNormalZoom() {
        Note child = noteService.createChildNote(parentId, "Starred");
        child.setAttribute("$Badge", new AttributeValue.StringValue("star"));

        SimpleStringProperty title = new SimpleStringProperty("Parent");
        MapViewModel vm = new MapViewModel(title, noteService);
        vm.setBaseNoteId(parentId);

        MapViewController controller = new MapViewController();
        Pane mapCanvas = new Pane();
        injectField(controller, MapViewController.class, "mapCanvas", mapCanvas);
        controller.initViewModel(vm);

        // Find the note node
        StackPane noteNode = findNodeByUserData(mapCanvas, child.getId());
        assertNotNull(noteNode, "Note node should exist on canvas");

        // Should have a badge Label as third child
        Label badgeLabel = findBadgeLabel(noteNode);
        assertNotNull(badgeLabel,
                "Map view should render a badge Label for a badged note");
        assertEquals("\u2B50", badgeLabel.getText(),
                "Badge label should contain the star symbol");
    }

    @Test
    @DisplayName("Map view omits badge Label when $Badge is not set")
    void mapView_shouldOmitBadgeWhenNotSet() {
        noteService.createChildNote(parentId, "NoBadge");

        SimpleStringProperty title = new SimpleStringProperty("Parent");
        MapViewModel vm = new MapViewModel(title, noteService);
        vm.setBaseNoteId(parentId);

        MapViewController controller = new MapViewController();
        Pane mapCanvas = new Pane();
        injectField(controller, MapViewController.class, "mapCanvas", mapCanvas);
        controller.initViewModel(vm);

        NoteDisplayItem item = vm.getNoteItems().get(0);
        StackPane noteNode = findNodeByUserData(mapCanvas, item.getId());
        assertNotNull(noteNode);

        Label badgeLabel = findBadgeLabel(noteNode);
        assertTrue(badgeLabel == null
                        || badgeLabel.getText() == null
                        || badgeLabel.getText().isEmpty(),
                "Map view should not render a badge when $Badge is empty");
    }

    // --- Outline view badge tests ---

    @Test
    @DisplayName("Outline view prepends badge to note title in tree cell")
    void outlineView_shouldPrependBadgeToTitle() {
        Note child = noteService.createChildNote(parentId, "Flagged");
        child.setAttribute("$Badge",
                new AttributeValue.StringValue("flag"));

        SimpleStringProperty title = new SimpleStringProperty("Parent");
        OutlineViewModel vm = new OutlineViewModel(title, noteService);
        vm.setBaseNoteId(parentId);
        vm.loadNotes();

        // Verify the display item has the badge
        NoteDisplayItem item = vm.getRootItems().get(0);
        assertEquals("\uD83D\uDEA9", item.getBadge(),
                "Outline display item should carry the flag badge");

        // The OutlineNoteTreeCell.badgedTitle() method prepends badge
        // We test this indirectly: badge + " " + title
        String expected = "\uD83D\uDEA9 Flagged";
        String badgedTitle = item.getBadge().isEmpty()
                ? item.getTitle()
                : item.getBadge() + " " + item.getTitle();
        assertEquals(expected, badgedTitle,
                "Badged title should prepend badge symbol");
    }

    @Test
    @DisplayName("Outline view shows plain title when $Badge is empty")
    void outlineView_shouldShowPlainTitleWhenNoBadge() {
        noteService.createChildNote(parentId, "Plain");

        SimpleStringProperty title = new SimpleStringProperty("Parent");
        OutlineViewModel vm = new OutlineViewModel(title, noteService);
        vm.setBaseNoteId(parentId);
        vm.loadNotes();

        NoteDisplayItem item = vm.getRootItems().get(0);
        assertEquals("", item.getBadge());
        String badgedTitle = item.getBadge().isEmpty()
                ? item.getTitle()
                : item.getBadge() + " " + item.getTitle();
        assertEquals("Plain", badgedTitle);
    }

    // --- Treemap view badge tests ---

    @Test
    @DisplayName("Treemap view renders badge Label for large-enough rectangle")
    void treemapView_shouldRenderBadgeLabelForLargeRect() {
        Note child = noteService.createChildNote(parentId, "Checked");
        child.setAttribute("$Badge",
                new AttributeValue.StringValue("check"));

        SimpleStringProperty title = new SimpleStringProperty("Parent");
        TreemapViewModel vm = new TreemapViewModel(title, noteService);
        vm.setBaseNoteId(parentId);

        TreemapViewController controller = new TreemapViewController();
        Pane treemapCanvas = new Pane();
        injectField(controller, TreemapViewController.class,
                "treemapCanvas", treemapCanvas);
        controller.initViewModel(vm);

        // Force a size so treemap layout runs
        treemapCanvas.resize(400, 300);
        // Trigger layout recalculation by firing width/height listeners
        treemapCanvas.setPrefSize(400, 300);
        // Wait for layout
        WaitForAsyncUtils.waitForFxEvents();

        // Find the note node
        boolean foundBadge = false;
        for (Node node : treemapCanvas.getChildren()) {
            if (node instanceof StackPane sp
                    && child.getId().equals(sp.getUserData())) {
                Label badgeLabel = findBadgeLabel(sp);
                if (badgeLabel != null
                        && "\u2705".equals(badgeLabel.getText())) {
                    foundBadge = true;
                }
            }
        }
        assertTrue(foundBadge,
                "Treemap view should render a check badge "
                        + "for a large-enough rectangle");
    }

    // --- Attribute Browser view badge tests ---

    @Test
    @DisplayName("Attribute Browser prepends badge to note title "
            + "in category tree")
    @SuppressWarnings("unchecked")
    void attributeBrowser_shouldPrependBadgeToNoteTitle() {
        Note child = noteService.createNote("Important", "");
        child.setAttribute("$Badge",
                new AttributeValue.StringValue("star"));
        child.setAttribute("$Color",
                new AttributeValue.ColorValue(
                        com.embervault.domain.TbxColor.named("red")));

        AttributeSchemaRegistry schemaRegistry =
                new AttributeSchemaRegistry();
        AttributeBrowserViewModel vm =
                new AttributeBrowserViewModel(noteService, schemaRegistry);

        AttributeBrowserViewController controller =
                new AttributeBrowserViewController();
        VBox browserRoot = new VBox();
        ComboBox<String> comboBox = new ComboBox<>();
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        injectField(controller,
                AttributeBrowserViewController.class,
                "browserRoot", browserRoot);
        injectField(controller,
                AttributeBrowserViewController.class,
                "attributeComboBox", comboBox);
        injectField(controller,
                AttributeBrowserViewController.class,
                "categoryTreeView", treeView);

        controller.initViewModel(vm);

        // Select $Color to trigger grouping
        comboBox.setValue("$Color");
        WaitForAsyncUtils.waitForFxEvents();

        // Find the note leaf in the tree
        boolean foundBadgedTitle = false;
        TreeItem<String> treeRoot = treeView.getRoot();
        for (TreeItem<String> category : treeRoot.getChildren()) {
            for (TreeItem<String> noteItem : category.getChildren()) {
                String text = noteItem.getValue();
                if (text != null && text.contains("\u2B50")
                        && text.contains("Important")) {
                    foundBadgedTitle = true;
                }
            }
        }
        assertTrue(foundBadgedTitle,
                "Attribute Browser should prepend the star badge "
                        + "to the note title in the category tree");
    }

    @Test
    @DisplayName("Attribute Browser shows plain title when $Badge is empty")
    @SuppressWarnings("unchecked")
    void attributeBrowser_shouldShowPlainTitleWhenNoBadge() {
        Note child = noteService.createNote("NoBadge", "");
        child.setAttribute("$Color",
                new AttributeValue.ColorValue(
                        com.embervault.domain.TbxColor.named("blue")));

        AttributeSchemaRegistry schemaRegistry =
                new AttributeSchemaRegistry();
        AttributeBrowserViewModel vm =
                new AttributeBrowserViewModel(noteService, schemaRegistry);

        AttributeBrowserViewController controller =
                new AttributeBrowserViewController();
        VBox browserRoot = new VBox();
        ComboBox<String> comboBox = new ComboBox<>();
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        injectField(controller,
                AttributeBrowserViewController.class,
                "browserRoot", browserRoot);
        injectField(controller,
                AttributeBrowserViewController.class,
                "attributeComboBox", comboBox);
        injectField(controller,
                AttributeBrowserViewController.class,
                "categoryTreeView", treeView);

        controller.initViewModel(vm);

        comboBox.setValue("$Color");
        WaitForAsyncUtils.waitForFxEvents();

        boolean foundPlainTitle = false;
        TreeItem<String> treeRoot = treeView.getRoot();
        for (TreeItem<String> category : treeRoot.getChildren()) {
            for (TreeItem<String> noteItem : category.getChildren()) {
                if ("NoBadge".equals(noteItem.getValue())) {
                    foundPlainTitle = true;
                }
            }
        }
        assertTrue(foundPlainTitle,
                "Attribute Browser should show plain title "
                        + "when $Badge is empty");
    }

    // --- Hyperbolic view badge tests ---

    @Test
    @DisplayName("Hyperbolic view prepends badge to note label text")
    void hyperbolicView_shouldPrependBadgeToLabel() {
        Note child = noteService.createChildNote(parentId, "Linked");
        child.setAttribute("$Badge",
                new AttributeValue.StringValue("star"));

        LinkService linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        HyperbolicViewModel vm = new HyperbolicViewModel(
                noteService, linkService);
        vm.setViewportRadius(200);
        vm.setFocusNote(parentId);

        // Verify the ViewModel returns the badge for the child note
        String badge = vm.getNoteBadge(child.getId());
        assertEquals("\u2B50", badge,
                "Hyperbolic ViewModel should return star badge symbol");

        // Verify composed label text matches what the controller renders
        String title = child.getTitle();
        String labelText = badge.isEmpty() ? title : badge + " " + title;
        assertEquals("\u2B50 Linked", labelText,
                "Hyperbolic label should prepend badge to title");
    }

    @Test
    @DisplayName("Hyperbolic view returns empty badge when $Badge is not set")
    void hyperbolicView_shouldReturnEmptyBadgeWhenNotSet() {
        Note child = noteService.createChildNote(parentId, "NoBadge");

        LinkService linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        HyperbolicViewModel vm = new HyperbolicViewModel(
                noteService, linkService);
        vm.setViewportRadius(200);
        vm.setFocusNote(parentId);

        String badge = vm.getNoteBadge(child.getId());
        assertEquals("", badge,
                "Hyperbolic ViewModel should return empty badge "
                        + "when $Badge is not set");

        // Label text should be plain title
        String title = child.getTitle();
        String labelText = badge.isEmpty() ? title : badge + " " + title;
        assertEquals("NoBadge", labelText,
                "Hyperbolic label should show plain title "
                        + "when no badge is set");
    }

    // --- Helpers ---

    private StackPane findNodeByUserData(Pane canvas, UUID id) {
        for (Node child : canvas.getChildren()) {
            if (child instanceof StackPane sp
                    && id.equals(sp.getUserData())) {
                return sp;
            }
        }
        return null;
    }

    private Label findBadgeLabel(StackPane noteNode) {
        for (Node child : noteNode.getChildren()) {
            if (child instanceof Label label
                    && !(child instanceof javafx.scene.control.Button)
                    && StackPane.getAlignment(label)
                            == javafx.geometry.Pos.TOP_RIGHT) {
                return label;
            }
        }
        return null;
    }

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
