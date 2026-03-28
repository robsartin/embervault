package com.embervault;

import java.io.IOException;

import com.embervault.adapter.in.ui.view.AttributeBrowserViewController;
import com.embervault.adapter.in.ui.view.HyperbolicViewController;
import com.embervault.adapter.in.ui.view.MapViewController;
import com.embervault.adapter.in.ui.view.NoteEditorViewController;
import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.view.TreemapViewController;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.ProjectService;
import com.embervault.application.port.out.LinkRepository;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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

        // Save root note to repository so children can reference it
        noteRepository.save(project.getRootNote());

        // Create a welcome note so the views have something to display
        noteService.createChildNote(project.getRootNote().getId(), "Welcome to EmberVault");

        // Observable note title for binding to ViewModels
        StringProperty rootNoteTitle = new SimpleStringProperty(
                project.getRootNote().getTitle());

        // Create ViewModels with shared NoteService
        MapViewModel mapViewModel = new MapViewModel(rootNoteTitle, noteService);
        mapViewModel.setBaseNoteId(project.getRootNote().getId());

        OutlineViewModel outlineViewModel = new OutlineViewModel(rootNoteTitle, noteService);
        outlineViewModel.setBaseNoteId(project.getRootNote().getId());

        TreemapViewModel treemapViewModel = new TreemapViewModel(rootNoteTitle, noteService);
        treemapViewModel.setBaseNoteId(project.getRootNote().getId());

        // Create shared AttributeSchemaRegistry
        AttributeSchemaRegistry schemaRegistry = new AttributeSchemaRegistry();

        // Create Attribute Browser ViewModel
        AttributeBrowserViewModel browserViewModel =
                new AttributeBrowserViewModel(noteService, schemaRegistry);

        // Create Note Editor ViewModel
        NoteEditorViewModel editorViewModel =
                new NoteEditorViewModel(noteService, schemaRegistry);

        // Load MapView
        FXMLLoader mapLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/MapView.fxml"));
        Parent mapView = mapLoader.load();
        MapViewController mapController = mapLoader.getController();
        mapController.initViewModel(mapViewModel);

        // Load OutlineView
        FXMLLoader outlineLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/OutlineView.fxml"));
        Parent outlineView = outlineLoader.load();
        OutlineViewController outlineController = outlineLoader.getController();
        outlineController.initViewModel(outlineViewModel);

        // Load TreemapView
        FXMLLoader treemapLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/TreemapView.fxml"));
        Parent treemapView = treemapLoader.load();
        TreemapViewController treemapController = treemapLoader.getController();
        treemapController.initViewModel(treemapViewModel);

        // Create HyperbolicViewModel with shared services
        HyperbolicViewModel hyperbolicViewModel =
                new HyperbolicViewModel(noteService, linkService);

        // Load HyperbolicView
        FXMLLoader hyperbolicLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/HyperbolicView.fxml"));
        Parent hyperbolicView = hyperbolicLoader.load();
        HyperbolicViewController hyperbolicController =
                hyperbolicLoader.getController();
        hyperbolicController.initViewModel(hyperbolicViewModel);

        // Load AttributeBrowserView
        FXMLLoader browserLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/AttributeBrowserView.fxml"));
        Parent browserView = browserLoader.load();
        AttributeBrowserViewController browserController =
                browserLoader.getController();
        browserController.initViewModel(browserViewModel);

        // Load NoteEditorView
        FXMLLoader editorLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/NoteEditorView.fxml"));
        Parent editorView = editorLoader.load();
        NoteEditorViewController editorController =
                editorLoader.getController();
        editorController.initViewModel(editorViewModel);

        // Wire browser note selection to editor
        browserViewModel.selectedNoteIdProperty().addListener(
                (obs, oldVal, newVal) -> editorViewModel.setNote(newVal));

        // Synchronize: any mutation in any view triggers all to reload
        // from the shared NoteService/Repository.
        Runnable refreshAll = () -> {
            mapViewModel.loadNotes();
            outlineViewModel.loadNotes();
            treemapViewModel.loadNotes();
            browserViewModel.groupNotes();
            if (hyperbolicViewModel.getFocusNoteId() != null) {
                hyperbolicViewModel.setFocusNote(
                        hyperbolicViewModel.getFocusNoteId());
            }
        };
        mapViewModel.setOnDataChanged(refreshAll);
        outlineViewModel.setOnDataChanged(refreshAll);
        treemapViewModel.setOnDataChanged(refreshAll);
        editorViewModel.setOnDataChanged(refreshAll);
        hyperbolicViewModel.setOnDataChanged(refreshAll);

        // Wrap each view with a title label
        Label mapLabel = new Label();
        mapLabel.textProperty().bind(mapViewModel.tabTitleProperty());
        mapLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        VBox mapContainer = new VBox(mapLabel, mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);

        Label outlineLabel = new Label();
        outlineLabel.textProperty().bind(outlineViewModel.tabTitleProperty());
        outlineLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        VBox outlineContainer = new VBox(outlineLabel, outlineView);
        VBox.setVgrow(outlineView, Priority.ALWAYS);

        Label treemapLabel = new Label();
        treemapLabel.textProperty().bind(treemapViewModel.tabTitleProperty());
        treemapLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        VBox treemapContainer = new VBox(treemapLabel, treemapView);
        VBox.setVgrow(treemapView, Priority.ALWAYS);

        Label hyperbolicLabel = new Label();
        hyperbolicLabel.textProperty().bind(
                hyperbolicViewModel.tabTitleProperty());
        hyperbolicLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        VBox hyperbolicContainer = new VBox(hyperbolicLabel, hyperbolicView);
        VBox.setVgrow(hyperbolicView, Priority.ALWAYS);

        // Browser + Editor combined pane
        Label browserLabel = new Label();
        browserLabel.textProperty().bind(browserViewModel.tabTitleProperty());
        browserLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        VBox browserContainer = new VBox(browserLabel, browserView);
        VBox.setVgrow(browserView, Priority.ALWAYS);

        VBox editorContainer = new VBox(editorView);
        VBox.setVgrow(editorView, Priority.ALWAYS);

        SplitPane browserEditorPane = new SplitPane(
                browserContainer, editorContainer);
        browserEditorPane.setDividerPositions(0.4);

        // SplitPane with Map, Outline, and Treemap
        SplitPane splitPane = new SplitPane(
                mapContainer, outlineContainer, treemapContainer);
        splitPane.setDividerPositions(0.33, 0.66);

        // Menu bar
        MenuBar menuBar = createMenuBar(
                mapViewModel, outlineViewModel, splitPane,
                browserEditorPane, hyperbolicContainer,
                hyperbolicViewModel, project);

        // BorderPane: menu bar on top, split pane in center
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("EmberVault - " + project.getName());
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar(MapViewModel mapViewModel,
            OutlineViewModel outlineViewModel,
            SplitPane mainSplitPane,
            SplitPane browserEditorPane,
            VBox hyperbolicContainer,
            HyperbolicViewModel hyperbolicViewModel,
            Project project) {
        // Note menu
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setAccelerator(
                new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        createNote.setOnAction(e -> {
            // Create a child under the selected note in the map, or under root
            mapViewModel.createChildNote("Untitled");
        });

        Menu noteMenu = new Menu("Note");
        noteMenu.getItems().add(createNote);

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
            BorderPane root = (BorderPane) mainSplitPane.getScene().getRoot();
            if (root.getCenter() == hyperbolicContainer) {
                root.setCenter(mainSplitPane);
            } else {
                // Set focus to root note if no focus set yet
                if (hyperbolicViewModel.getFocusNoteId() == null) {
                    hyperbolicViewModel.setFocusNote(
                            project.getRootNote().getId());
                }
                root.setCenter(hyperbolicContainer);
            }
        });

        MenuItem browserViewItem = new MenuItem("Browser");
        browserViewItem.setAccelerator(
                new KeyCodeCombination(KeyCode.B,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.SHIFT_DOWN));
        browserViewItem.setOnAction(e -> {
            BorderPane root = (BorderPane) mainSplitPane.getScene().getRoot();
            if (root.getCenter() == browserEditorPane) {
                root.setCenter(mainSplitPane);
            } else {
                root.setCenter(browserEditorPane);
            }
        });

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(mapViewItem, outlineViewItem,
                treemapViewItem, hyperbolicViewItem, browserViewItem);

        MenuBar menuBar = new MenuBar(noteMenu, viewMenu);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
