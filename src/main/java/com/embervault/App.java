package com.embervault;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main application entry point for EmberVault.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Welcome to EmberVault");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("EmberVault");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
