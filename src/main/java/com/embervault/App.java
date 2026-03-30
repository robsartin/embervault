package com.embervault;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.view.SearchViewController;
import com.embervault.adapter.in.ui.view.TextPaneViewController;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Attributes;
import com.embervault.domain.Project;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point for EmberVault.
 */
public class App extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private final WindowManager windowManager = new WindowManager();
    private SharedServices sharedServices;

    @Override
    public void start(Stage stage) throws IOException {
        sharedServices = SharedServices.create();
        Project project = sharedServices.project();
        NoteService noteService = sharedServices.noteService();
        LinkService linkService = sharedServices.linkService();
        StampService stampService = sharedServices.stampService();
        AttributeSchemaRegistry schemaRegistry =
                sharedServices.schemaRegistry();
        populateBuiltInStamps(stampService);
        noteService.createChildNote(project.getRootNote().getId(),
                "Welcome to EmberVault");
        StringProperty rootNoteTitle = new SimpleStringProperty(
                project.getRootNote().getTitle());

        // Single Outline view
        OutlineViewModel outlineViewModel = new OutlineViewModel(
                rootNoteTitle, noteService);
        outlineViewModel.setBaseNoteId(project.getRootNote().getId());
        var outlineCtrl = new OutlineViewController[1];
        Parent outlineView = loadView("OutlineView.fxml", c -> {
            outlineCtrl[0] = (OutlineViewController) c;
            outlineCtrl[0].initViewModel(outlineViewModel);
        });

        // Search
        SearchViewModel searchViewModel = new SearchViewModel(noteService);
        Parent searchView = loadView("SearchView.fxml",
                c -> ((SearchViewController) c)
                        .initViewModel(searchViewModel));

        // Text pane for selected note
        SelectedNoteViewModel selectedNoteVm =
                new SelectedNoteViewModel(noteService);
        FXMLLoader textPaneLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/TextPaneView.fxml"));
        Parent textPaneView = textPaneLoader.load();
        ((TextPaneViewController) textPaneLoader.getController())
                .initViewModel(selectedNoteVm);
        wireSelection(outlineViewModel.selectedNoteIdProperty(),
                selectedNoteVm);
        searchViewModel.selectedNoteIdProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        outlineViewModel.selectNote(newVal);
                    }
                });

        // Single ViewPaneContext
        ViewPaneContext outlinePane = new ViewPaneContext(
                ViewType.OUTLINE,
                outlineViewModel.tabTitleProperty(), outlineView,
                project.getRootNote().getId(),
                outlineViewModel::loadNotes);
        outlineCtrl[0].setOnViewSwitch(name ->
                outlinePane.switchView(ViewType.valueOf(name)));
        Runnable localRefresh = () -> {
            outlinePane.refreshCurrentView();
            UUID selId = selectedNoteVm.selectedNoteIdProperty().get();
            if (selId != null) {
                selectedNoteVm.setSelectedNoteId(selId);
            }
            searchViewModel.refreshResults();
        };
        windowManager.register(stage);
        windowManager.addRefreshListener(localRefresh);
        Runnable refreshAll = windowManager::notifyAllWindows;
        outlineViewModel.setOnDataChanged(refreshAll);
        selectedNoteVm.setOnDataChanged(refreshAll);
        searchViewModel.setOnDataChanged(refreshAll);
        stage.setOnCloseRequest(event -> {
            windowManager.removeRefreshListener(localRefresh);
            windowManager.unregister(stage);
            if (windowManager.getWindows().isEmpty()) {
                javafx.application.Platform.exit();
            }
        });
        ViewPaneDeps paneDeps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                refreshAll, selectedNoteVm, rootNoteTitle);
        outlinePane.setDeps(paneDeps);

        // Layout: outline + text pane
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(
                javafx.geometry.Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(
                outlinePane.getContainer(), textPaneView);
        mainSplitPane.setDividerPositions(0.6);

        WindowContext winCtx = new WindowContext(
                sharedServices, windowManager,
                outlineViewModel.selectedNoteIdProperty(),
                refreshAll, stage,
                searchViewModel::toggleVisible);
        MenuBar menuBar = MenuBarFactory.create(winCtx);
        VBox topArea = new VBox(menuBar, searchView);
        BorderPane root = new BorderPane();
        root.setTop(topArea);
        root.setCenter(mainSplitPane);

        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("EmberVault - " + project.getName());
        stage.setScene(scene);
        stage.show();
    }

    private Parent loadView(String fxml,
            java.util.function.Consumer<Object> init) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/" + fxml));
        Parent view = loader.load();
        init.accept(loader.getController());
        return view;
    }

    private void populateBuiltInStamps(StampService stampService) {
        stampService.createStamp("Color:red", Attributes.COLOR + "=red");
        stampService.createStamp("Color:green", Attributes.COLOR + "=green");
        stampService.createStamp("Color:blue", Attributes.COLOR + "=blue");
        stampService.createStamp("Mark Done", Attributes.CHECKED + "=true");
        stampService.createStamp("Mark Undone", Attributes.CHECKED + "=false");

        for (String b : List.of("star", "flag", "check", "warning",
                "book", "person", "idea", "heart", "pin", "fire")) {
            stampService.createStamp("Badge:" + b, Attributes.BADGE + "=" + b);
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
