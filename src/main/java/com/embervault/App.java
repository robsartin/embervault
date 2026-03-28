package com.embervault;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.AttributeBrowserViewController;
import com.embervault.adapter.in.ui.view.HyperbolicViewController;
import com.embervault.adapter.in.ui.view.MapViewController;
import com.embervault.adapter.in.ui.view.NoteEditorViewController;
import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.view.SearchViewController;
import com.embervault.adapter.in.ui.view.StampEditorViewController;
import com.embervault.adapter.in.ui.view.TextPaneViewController;
import com.embervault.adapter.in.ui.view.TreemapViewController;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.in.ui.viewmodel.StampEditorViewModel;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.ProjectService;
import com.embervault.application.port.in.StampService;
import com.embervault.application.port.out.LinkRepository;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.application.port.out.StampRepository;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;
import com.embervault.domain.Stamp;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point for EmberVault.
 */
public class App extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage stage) throws IOException {
        // Create an empty project on startup
        ProjectService projectService = new ProjectServiceImpl();
        Project project = projectService.createEmptyProject();

        // Create shared NoteRepository and NoteService
        NoteRepository noteRepository = new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(noteRepository);

        // Create shared LinkRepository and LinkService
        LinkRepository linkRepository = new InMemoryLinkRepository();
        LinkService linkService = new LinkServiceImpl(linkRepository);

        // Create StampRepository and StampService
        StampRepository stampRepository = new InMemoryStampRepository();
        StampService stampService = new StampServiceImpl(
                stampRepository, noteRepository);

        // Pre-populate built-in stamps
        populateBuiltInStamps(stampService);

        // Save root note to repository so children can reference it
        noteRepository.save(project.getRootNote());

        // Create a welcome note so the views have something to display
        noteService.createChildNote(project.getRootNote().getId(),
                "Welcome to EmberVault");

        // Observable note title for binding to ViewModels
        StringProperty rootNoteTitle = new SimpleStringProperty(
                project.getRootNote().getTitle());

        // Create ViewModels with shared NoteService
        MapViewModel mapViewModel = new MapViewModel(
                rootNoteTitle, noteService);
        mapViewModel.setBaseNoteId(project.getRootNote().getId());

        OutlineViewModel outlineViewModel = new OutlineViewModel(
                rootNoteTitle, noteService);
        outlineViewModel.setBaseNoteId(project.getRootNote().getId());

        TreemapViewModel treemapViewModel = new TreemapViewModel(
                rootNoteTitle, noteService);
        treemapViewModel.setBaseNoteId(project.getRootNote().getId());

        // Create shared AttributeSchemaRegistry
        AttributeSchemaRegistry schemaRegistry = new AttributeSchemaRegistry();

        // Create Attribute Browser ViewModel
        AttributeBrowserViewModel browserViewModel =
                new AttributeBrowserViewModel(noteService, schemaRegistry);

        // Create Note Editor ViewModel
        NoteEditorViewModel editorViewModel =
                new NoteEditorViewModel(noteService, schemaRegistry);

        // Load views
        Parent mapView = loadView("MapView.fxml",
                c -> ((MapViewController) c).initViewModel(mapViewModel));
        Parent outlineView = loadView("OutlineView.fxml",
                c -> ((OutlineViewController) c).initViewModel(outlineViewModel));
        Parent treemapView = loadView("TreemapView.fxml",
                c -> ((TreemapViewController) c).initViewModel(treemapViewModel));
        HyperbolicViewModel hyperbolicViewModel =
                new HyperbolicViewModel(noteService, linkService);
        Parent hyperbolicView = loadView("HyperbolicView.fxml",
                c -> ((HyperbolicViewController) c).initViewModel(hyperbolicViewModel));
        Parent browserView = loadView("AttributeBrowserView.fxml",
                c -> ((AttributeBrowserViewController) c).initViewModel(browserViewModel));
        Parent editorView = loadView("NoteEditorView.fxml",
                c -> ((NoteEditorViewController) c).initViewModel(editorViewModel));
        browserViewModel.selectedNoteIdProperty().addListener(
                (obs, oldVal, newVal) -> editorViewModel.setNote(newVal));
        SearchViewModel searchViewModel = new SearchViewModel(noteService);
        Parent searchView = loadView("SearchView.fxml",
                c -> ((SearchViewController) c).initViewModel(searchViewModel));
        searchViewModel.selectedNoteIdProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        mapViewModel.selectNote(newVal);
                        outlineViewModel.selectNote(newVal);
                    }
                });

        // Create SelectedNoteViewModel and load TextPaneView
        SelectedNoteViewModel selectedNoteVm =
                new SelectedNoteViewModel(noteService);
        FXMLLoader textPaneLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/TextPaneView.fxml"));
        Parent textPaneView = textPaneLoader.load();
        ((TextPaneViewController) textPaneLoader.getController())
                .initViewModel(selectedNoteVm);
        // Wire all views' selection to the text pane
        wireSelection(mapViewModel.selectedNoteIdProperty(), selectedNoteVm);
        wireSelection(outlineViewModel.selectedNoteIdProperty(), selectedNoteVm);
        wireSelection(treemapViewModel.selectedNoteIdProperty(), selectedNoteVm);

        // Create ViewPaneContext instances for switchable panes.
        // refreshAll is defined as a lambda that delegates to each
        // pane context, so pane switches are automatically reflected.
        ViewPaneContext mapPane = new ViewPaneContext(
                ViewType.MAP,
                mapViewModel.tabTitleProperty(), mapView,
                project.getRootNote().getId(),
                mapViewModel::loadNotes);
        ViewPaneContext outlinePane = new ViewPaneContext(
                ViewType.OUTLINE,
                outlineViewModel.tabTitleProperty(), outlineView,
                project.getRootNote().getId(),
                outlineViewModel::loadNotes);
        ViewPaneContext treemapPane = new ViewPaneContext(
                ViewType.TREEMAP,
                treemapViewModel.tabTitleProperty(), treemapView,
                project.getRootNote().getId(),
                treemapViewModel::loadNotes);

        // Synchronize: any mutation triggers all views to reload.
        Runnable refreshAll = () -> {
            mapPane.refreshCurrentView();
            outlinePane.refreshCurrentView();
            treemapPane.refreshCurrentView();
            browserViewModel.groupNotes();
            if (hyperbolicViewModel.getFocusNoteId() != null) {
                hyperbolicViewModel.setFocusNote(
                        hyperbolicViewModel.getFocusNoteId());
            }
            UUID selId = selectedNoteVm.selectedNoteIdProperty().get();
            if (selId != null) {
                selectedNoteVm.setSelectedNoteId(selId);
            }
        };
        mapViewModel.setOnDataChanged(refreshAll);
        outlineViewModel.setOnDataChanged(refreshAll);
        treemapViewModel.setOnDataChanged(refreshAll);
        editorViewModel.setOnDataChanged(refreshAll);
        hyperbolicViewModel.setOnDataChanged(refreshAll);
        selectedNoteVm.setOnDataChanged(refreshAll);

        // Wire shared deps into pane contexts for view switching
        ViewPaneDeps paneDeps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                refreshAll, selectedNoteVm, rootNoteTitle);
        mapPane.setDeps(paneDeps);
        outlinePane.setDeps(paneDeps);
        treemapPane.setDeps(paneDeps);

        VBox mapContainer = mapPane.getContainer();
        VBox outlineContainer = outlinePane.getContainer();
        VBox treemapContainer = treemapPane.getContainer();
        VBox hyperbolicContainer = wrapWithLabel(
                hyperbolicViewModel.tabTitleProperty(), hyperbolicView);

        // Browser + Editor combined pane
        VBox browserContainer = wrapWithLabel(
                browserViewModel.tabTitleProperty(), browserView);

        VBox editorContainer = new VBox(editorView);
        VBox.setVgrow(editorView, Priority.ALWAYS);

        SplitPane browserEditorPane = new SplitPane(
                browserContainer, editorContainer);
        browserEditorPane.setDividerPositions(0.4);

        // SplitPane with Map, Outline, and Treemap
        SplitPane viewsSplitPane = new SplitPane(
                mapContainer, outlineContainer, treemapContainer);
        viewsSplitPane.setDividerPositions(0.33, 0.66);

        // Vertical SplitPane: views on top, text pane on bottom
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(
                javafx.geometry.Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(viewsSplitPane, textPaneView);
        mainSplitPane.setDividerPositions(0.6);

        // Build shared context for menu construction
        AppContext ctx = new AppContext(
                mapViewModel, hyperbolicViewModel, searchViewModel,
                mainSplitPane, browserEditorPane, hyperbolicContainer,
                project, stampService,
                mapViewModel.selectedNoteIdProperty(),
                refreshAll, stage);

        // Menu bar
        MenuBar menuBar = createMenuBar(ctx);

        // Top area: menu bar + search bar
        VBox topArea = new VBox(menuBar, searchView);

        // BorderPane: top area on top, split pane in center
        BorderPane root = new BorderPane();
        root.setTop(topArea);
        root.setCenter(mainSplitPane);

        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("EmberVault - " + project.getName());
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar(AppContext ctx) {
        // Note menu
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setAccelerator(
                new KeyCodeCombination(KeyCode.N,
                        KeyCombination.SHORTCUT_DOWN));
        createNote.setOnAction(e -> {
            // Create a child under the selected note in the map,
            // or under root
            ctx.mapViewModel().createChildNote("Untitled");
        });

        Menu noteMenu = new Menu("Note");
        noteMenu.getItems().add(createNote);

        // Stamps menu
        Menu stampsMenu = new Menu("Stamps");
        buildStampsMenu(stampsMenu, ctx);

        // View menu
        MenuItem mapViewItem = new MenuItem("Map");
        mapViewItem.setOnAction(e ->
                LOG.debug("Map view placeholder selected"));

        MenuItem outlineViewItem = new MenuItem("Outline");
        outlineViewItem.setOnAction(e ->
                LOG.debug("Outline view placeholder selected"));

        MenuItem treemapViewItem = new MenuItem("Treemap");
        treemapViewItem.setOnAction(e ->
                LOG.debug("Treemap view placeholder selected"));

        MenuItem hyperbolicViewItem = new MenuItem("Hyperbolic");
        hyperbolicViewItem.setAccelerator(
                new KeyCodeCombination(KeyCode.H,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.SHIFT_DOWN));
        hyperbolicViewItem.setOnAction(e -> {
            BorderPane root = (BorderPane) ctx.mainSplitPane()
                    .getScene().getRoot();
            if (root.getCenter() == ctx.hyperbolicContainer()) {
                root.setCenter(ctx.mainSplitPane());
            } else {
                // Set focus to root note if no focus set yet
                if (ctx.hyperbolicViewModel().getFocusNoteId()
                        == null) {
                    ctx.hyperbolicViewModel().setFocusNote(
                            ctx.project().getRootNote().getId());
                }
                root.setCenter(ctx.hyperbolicContainer());
            }
        });

        MenuItem browserViewItem = new MenuItem("Browser");
        browserViewItem.setAccelerator(
                new KeyCodeCombination(KeyCode.B,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.SHIFT_DOWN));
        browserViewItem.setOnAction(e -> {
            BorderPane root = (BorderPane) ctx.mainSplitPane()
                    .getScene().getRoot();
            if (root.getCenter() == ctx.browserEditorPane()) {
                root.setCenter(ctx.mainSplitPane());
            } else {
                root.setCenter(ctx.browserEditorPane());
            }
        });

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(mapViewItem, outlineViewItem,
                treemapViewItem, hyperbolicViewItem, browserViewItem);

        // Edit menu
        MenuItem findItem = new MenuItem("Find");
        findItem.setAccelerator(
                new KeyCodeCombination(KeyCode.F,
                        KeyCombination.SHORTCUT_DOWN));
        findItem.setOnAction(e ->
                ctx.searchViewModel().toggleVisible());

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().add(findItem);

        MenuBar menuBar = new MenuBar(
                noteMenu, editMenu, stampsMenu, viewMenu);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    private void buildStampsMenu(Menu stampsMenu, AppContext ctx) {
        stampsMenu.getItems().clear();

        // "Inspect Stamps..." item
        MenuItem inspectItem = new MenuItem("Inspect Stamps...");
        inspectItem.setOnAction(e -> {
            try {
                openStampEditor(stampsMenu, ctx);
            } catch (IOException ex) {
                LOG.error("Failed to open stamp editor", ex);
            }
        });
        stampsMenu.getItems().add(inspectItem);
        stampsMenu.getItems().add(new SeparatorMenuItem());

        // Build dynamic stamp items
        Map<String, Menu> subMenus = new HashMap<>();

        for (Stamp stamp : ctx.stampService().getAllStamps()) {
            String name = stamp.name();
            UUID stampId = stamp.id();

            // Check for exactly one colon for submenu
            int colonIndex = name.indexOf(':');
            boolean hasSubmenu = colonIndex > 0
                    && colonIndex == name.lastIndexOf(':');

            if (hasSubmenu) {
                String menuName = name.substring(0, colonIndex);
                String itemName = name.substring(colonIndex + 1);

                Menu subMenu = subMenus.computeIfAbsent(menuName,
                        k -> {
                            Menu m = new Menu(k);
                            stampsMenu.getItems().add(m);
                            return m;
                        });

                MenuItem item = new MenuItem(itemName);
                item.setOnAction(ev -> applyStampToSelected(
                        ctx, stampId));
                subMenu.getItems().add(item);
            } else {
                MenuItem item = new MenuItem(name);
                item.setOnAction(ev -> applyStampToSelected(
                        ctx, stampId));
                stampsMenu.getItems().add(item);
            }
        }
    }

    private void applyStampToSelected(AppContext ctx,
            UUID stampId) {
        UUID noteId = ctx.selectedNoteId().get();
        if (noteId == null) {
            LOG.warn("No note selected to apply stamp to");
            return;
        }
        try {
            ctx.stampService().applyStamp(stampId, noteId);
            ctx.refreshAll().run();
        } catch (Exception ex) {
            LOG.error("Failed to apply stamp", ex);
        }
    }

    private void openStampEditor(Menu stampsMenu,
            AppContext ctx) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/"
                        + "StampEditorView.fxml"));
        Parent editorRoot = loader.load();
        StampEditorViewController controller = loader.getController();
        StampEditorViewModel editorVm =
                new StampEditorViewModel(ctx.stampService());
        controller.initViewModel(editorVm);

        Stage editorStage = new Stage();
        editorStage.setTitle("Inspect Stamps");
        editorStage.setScene(new Scene(editorRoot));
        editorStage.initOwner(ctx.ownerStage());
        editorStage.showAndWait();

        // Rebuild stamps menu after editor closes
        buildStampsMenu(stampsMenu, ctx);
    }

    private Parent loadView(String fxml,
            java.util.function.Consumer<Object> init) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/" + fxml));
        Parent view = loader.load();
        init.accept(loader.getController());
        return view;
    }

    private static VBox wrapWithLabel(
            ReadOnlyStringProperty titleProp,
            Parent view) {
        Label label = new Label();
        label.textProperty().bind(titleProp);
        label.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        VBox container = new VBox(label, view);
        VBox.setVgrow(view, Priority.ALWAYS);
        return container;
    }

    private void populateBuiltInStamps(StampService stampService) {
        stampService.createStamp("Color:red", "$Color=red");
        stampService.createStamp("Color:green", "$Color=green");
        stampService.createStamp("Color:blue", "$Color=blue");
        stampService.createStamp("Mark Done", "$Checked=true");
        stampService.createStamp("Mark Undone", "$Checked=false");

        for (String b : List.of("star", "flag", "check", "warning",
                "book", "person", "idea", "heart", "pin", "fire")) {
            stampService.createStamp("Badge:" + b, "$Badge=" + b);
        }
    }

    private static void wireSelection(
            ObjectProperty<UUID> source,
            SelectedNoteViewModel target) {
        source.addListener(
                (obs, oldVal, newVal) -> target.setSelectedNoteId(newVal));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
