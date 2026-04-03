package com.embervault;

import java.io.IOException;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.MapViewController;
import com.embervault.adapter.in.ui.view.TextPaneViewController;
import com.embervault.adapter.in.ui.viewmodel.AppStateEventBridge;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.domain.Project;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Creates new application windows that share the same services.
 *
 * <p>Each window gets its own ViewModels, SelectedNoteViewModel, and
 * ViewPaneContexts, but shares the same NoteService, LinkService,
 * and StampService so all windows see the same data.</p>
 */
public final class WindowFactory {

    private WindowFactory() { }

    /**
     * Creates and shows a new secondary window.
     *
     * <p>The new window starts with a Map view and text pane, with
     * its own selection state. Data mutations in this window
     * trigger refresh across all open windows via the WindowManager.</p>
     *
     * @param services      the shared services
     * @param windowManager the window manager for registration
     * @return the new Stage
     * @throws IOException if FXML loading fails
     */
    public static Stage openNewWindow(
            SharedServices services,
            WindowManager windowManager) throws IOException {
        Stage newStage = new Stage();
        Project project = services.project();

        WindowSetupContext setupCtx = new WindowSetupContext(
                services, windowManager);
        WindowSetupResult setup = WindowBuilder.build(setupCtx);
        new AppStateEventBridge(setup.eventBus(), setup.appState());
        SelectedNoteViewModel selectedNoteVm = setup.selectedNoteVm();

        MapViewModel mapVm = new MapViewModel(
                setup.rootNoteTitle(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.paneDeps().noteService(),
                setup.appState(), setup.eventBus());
        mapVm.setBaseNoteId(project.getRootNote().getId());
        var paneHolder = new ViewPaneContext[1];
        Parent mapView = loadView("MapView.fxml", c -> {
            MapViewController ctrl = (MapViewController) c;
            ctrl.setOnViewSwitch(name ->
                    paneHolder[0].switchView(
                            ViewType.valueOf(name)));
            ctrl.initViewModel(mapVm);
        });


        FXMLLoader textPaneLoader = new FXMLLoader(
                WindowFactory.class.getResource(
                        "/com/embervault/adapter/in/ui/view/"
                                + "TextPaneView.fxml"));
        Parent textPaneView = textPaneLoader.load();
        ((TextPaneViewController) textPaneLoader.getController())
                .initViewModel(selectedNoteVm);
        WindowBuilder.wireSelection(
                mapVm.selectedNoteIdProperty(), selectedNoteVm);

        ViewPaneContext mapPane = new ViewPaneContext(
                ViewType.MAP,
                mapVm.tabTitleProperty(), mapView,
                project.getRootNote().getId(),
                mapVm::loadNotes);
        paneHolder[0] = mapPane;
        Runnable localRefresh = () -> {
            mapPane.refreshCurrentView();
            UUID selId = selectedNoteVm.selectedNoteIdProperty().get();
            if (selId != null) {
                selectedNoteVm.setSelectedNoteId(selId);
            }
        };
        windowManager.register(newStage);
        windowManager.addRefreshListener(localRefresh);
        mapPane.setDeps(setup.paneDeps());

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(
                javafx.geometry.Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(
                mapPane.getContainer(), textPaneView);
        mainSplitPane.setDividerPositions(0.6);

        WindowContext winCtx = new WindowContext(
                services, windowManager,
                mapVm.selectedNoteIdProperty(),
                setup.appState(), newStage, null, null,
                newRootId -> {
                    mapVm.setBaseNoteId(newRootId);
                    mapVm.loadNotes();
                });
        javafx.scene.control.MenuBar menuBar =
                MenuBarFactory.create(winCtx);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainSplitPane);

        Scene scene = new Scene(root, 800, 600);
        newStage.setTitle("EmberVault - " + project.getName());
        newStage.setScene(scene);
        newStage.setOnCloseRequest(event -> {
            windowManager.removeRefreshListener(localRefresh);
            windowManager.unregister(newStage);
        });
        newStage.show();
        return newStage;
    }

    private static Parent loadView(String fxml,
            java.util.function.Consumer<Object> init) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                WindowFactory.class.getResource(
                        "/com/embervault/adapter/in/ui/view/" + fxml));
        Parent view = loader.load();
        init.accept(loader.getController());
        return view;
    }
}
