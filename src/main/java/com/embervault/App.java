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
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

/**
 * Main application entry point for EmberVault.
 */
public class App extends Application {

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

        Scene scene = new Scene(splitPane, 1024, 768);
        stage.setTitle("EmberVault - " + project.getName());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
