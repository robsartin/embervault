package com.embervault;

import java.io.IOException;

import com.embervault.adapter.in.ui.view.MapViewController;
import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.ProjectService;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Project;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
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

        // Save root note to repository so children can reference it
        noteRepository.save(project.getRootNote());

        // Observable note title for binding to ViewModels
        StringProperty rootNoteTitle = new SimpleStringProperty(
                project.getRootNote().getTitle());

        // Create ViewModels with shared NoteService
        MapViewModel mapViewModel = new MapViewModel(rootNoteTitle, noteService);
        mapViewModel.setBaseNoteId(project.getRootNote().getId());

        OutlineViewModel outlineViewModel = new OutlineViewModel(rootNoteTitle, noteService);
        outlineViewModel.setBaseNoteId(project.getRootNote().getId());

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

        // Synchronize: when map creates a note, refresh outline and vice versa
        mapViewModel.getNoteItems().addListener(
                (javafx.collections.ListChangeListener<Object>) change ->
                        outlineViewModel.loadNotes());
        outlineViewModel.getRootItems().addListener(
                (javafx.collections.ListChangeListener<Object>) change ->
                        mapViewModel.loadNotes());

        // SplitPane with Map on left, Outline on right
        SplitPane splitPane = new SplitPane(mapView, outlineView);
        splitPane.setDividerPositions(0.5);

        // Menu bar
        MenuBar menuBar = createMenuBar(mapViewModel, outlineViewModel);

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
                                   OutlineViewModel outlineViewModel) {
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

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(mapViewItem, outlineViewItem);

        MenuBar menuBar = new MenuBar(noteMenu, viewMenu);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
