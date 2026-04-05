package com.embervault;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.view.SearchViewController;
import com.embervault.adapter.in.ui.view.TextPaneViewController;
import com.embervault.adapter.in.ui.viewmodel.AppStateEventBridge;
import com.embervault.adapter.in.ui.viewmodel.CommandPaletteViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.in.ui.viewmodel.ShortcutRegistry;
import com.embervault.adapter.in.ui.viewmodel.UndoRedoShortcuts;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.Attributes;
import com.embervault.domain.Project;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
        StampService stampService = sharedServices.stampService();
        populateBuiltInStamps(stampService);
        sharedServices.noteService().createChildNote(
                project.getRootNote().getId(),
                "Welcome to EmberVault");

        WindowSetupContext setupCtx = new WindowSetupContext(
                sharedServices, windowManager);
        WindowSetupResult setup = WindowBuilder.build(setupCtx);
        new AppStateEventBridge(setup.eventBus(), setup.appState());
        SelectedNoteViewModel selectedNoteVm = setup.selectedNoteVm();

        // Single Outline view
        OutlineViewModel outlineViewModel = new OutlineViewModel(
                setup.rootNoteTitle(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.appState(), setup.eventBus());
        outlineViewModel.setBaseNoteId(project.getRootNote().getId());
        var paneHolder = new ViewPaneContext[1];
        Parent outlineView = loadView("OutlineView.fxml", c -> {
            OutlineViewController ctrl = (OutlineViewController) c;
            ctrl.setOnViewSwitch(name ->
                    paneHolder[0].switchView(
                            ViewType.valueOf(name)));
            ctrl.initViewModel(outlineViewModel);
        });

        // Search
        SearchViewModel searchViewModel = new SearchViewModel(
                setup.paneDeps().noteService(), setup.appState());
        Parent searchView = loadView("SearchView.fxml",
                c -> ((SearchViewController) c)
                        .initViewModel(searchViewModel));

        // Text pane for selected note
        FXMLLoader textPaneLoader = new FXMLLoader(getClass().getResource(
                "/com/embervault/adapter/in/ui/view/TextPaneView.fxml"));
        Parent textPaneView = textPaneLoader.load();
        ((TextPaneViewController) textPaneLoader.getController())
                .initViewModel(selectedNoteVm);
        WindowBuilder.wireSelection(
                outlineViewModel.selectedNoteIdProperty(),
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
        paneHolder[0] = outlinePane;
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
        stage.setOnCloseRequest(event -> {
            windowManager.removeRefreshListener(localRefresh);
            windowManager.unregister(stage);
            if (windowManager.getWindows().isEmpty()) {
                javafx.application.Platform.exit();
            }
        });
        outlinePane.setDeps(setup.paneDeps());

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
                setup.appState(), stage,
                searchViewModel::toggleVisible,
                newRootId -> {
                    outlineViewModel.setBaseNoteId(newRootId);
                    outlineViewModel.loadNotes();
                });
        MenuBar menuBar = MenuBarFactory.create(winCtx);
        VBox topArea = new VBox(menuBar, searchView);
        BorderPane root = new BorderPane();
        root.setTop(topArea);
        root.setCenter(mainSplitPane);

        // Shortcut registry and command palette
        ShortcutRegistry shortcutRegistry = new ShortcutRegistry();
        CommandPaletteViewModel commandPaletteVm =
                new CommandPaletteViewModel(shortcutRegistry);
        CommandPaletteOverlay overlay =
                new CommandPaletteOverlay(commandPaletteVm);

        StackPane layeredRoot =
                new StackPane(root, overlay.getNode());

        registerShortcuts(shortcutRegistry, commandPaletteVm,
                winCtx, searchViewModel, outlineViewModel,
                setup, project);

        Scene scene = new Scene(layeredRoot, 1024, 768);
        ShortcutInstaller.install(scene, shortcutRegistry);
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
        stampService.createStamp("Color:green",
                Attributes.COLOR + "=green");
        stampService.createStamp("Color:blue",
                Attributes.COLOR + "=blue");
        stampService.createStamp("Mark Done",
                Attributes.CHECKED + "=true");
        stampService.createStamp("Mark Undone",
                Attributes.CHECKED + "=false");

        for (String b : List.of("star", "flag", "check", "warning",
                "book", "person", "idea", "heart", "pin", "fire")) {
            stampService.createStamp("Badge:" + b,
                    Attributes.BADGE + "=" + b);
        }
    }

    private void registerShortcuts(
            ShortcutRegistry registry,
            CommandPaletteViewModel commandPaletteVm,
            WindowContext winCtx,
            SearchViewModel searchViewModel,
            OutlineViewModel outlineViewModel,
            WindowSetupResult setup,
            Project project) {
        registry.register("Shortcut+K", "Command Palette",
                "Open the command palette",
                commandPaletteVm::show, true);
        registry.register("Shortcut+F", "Find",
                "Toggle the search panel",
                searchViewModel::toggleVisible, true);
        registry.register("Shortcut+N", "New Note",
                "Create a new child note", () -> {
                    UUID parentId =
                            winCtx.selectedNoteId().get();
                    if (parentId != null) {
                        sharedServices.noteService()
                                .createChildNote(parentId,
                                        "Untitled");
                        setup.appState().notifyDataChanged();
                    }
                });
        registry.register("Shortcut+Shift+N", "New Window",
                "Open a new window", () -> {
                    try {
                        WindowFactory.openNewWindow(
                                sharedServices, windowManager);
                    } catch (java.io.IOException ex) {
                        LOG.error("Failed to open new window", ex);
                    }
                });
        UndoRedoShortcuts.register(registry,
                sharedServices.undoRedoUseCase());
        registry.register("Shortcut+D", "Delete Note",
                "Delete the selected note", () -> {
                    UUID noteId =
                            winCtx.selectedNoteId().get();
                    if (noteId != null && !noteId.equals(
                            project.getRootNote().getId())) {
                        sharedServices.noteService()
                                .deleteNote(noteId);
                        setup.appState().notifyDataChanged();
                    }
                });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
