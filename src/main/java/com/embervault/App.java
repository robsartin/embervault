package com.embervault;

import java.io.IOException;

import com.embervault.adapter.in.ui.view.NoteViewController;
import com.embervault.adapter.in.ui.viewmodel.NoteViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.out.NoteRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for EmberVault.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Manual dependency injection
        NoteRepository repository = new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(repository);
        NoteViewModel viewModel = new NoteViewModel(noteService);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/NoteView.fxml"));
        Parent root = loader.load();

        NoteViewController controller = loader.getController();
        controller.initViewModel(viewModel);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("EmberVault");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
