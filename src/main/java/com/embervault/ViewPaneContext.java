package com.embervault;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.AttributeBrowserViewController;
import com.embervault.adapter.in.ui.view.HyperbolicViewController;
import com.embervault.adapter.in.ui.view.MapViewController;
import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.view.TreemapViewController;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a single view pane within the split-pane layout.
 *
 * <p>Encapsulates the current view type, the base note id, the VBox
 * container, and the ability to switch to a different view type
 * via a right-click context menu on the tab title label.</p>
 */
public final class ViewPaneContext {

    private static final Logger LOG =
            LoggerFactory.getLogger(ViewPaneContext.class);

    private final VBox container;
    private final Label label;

    private NoteService noteService;
    private LinkService linkService;
    private AttributeSchemaRegistry schemaRegistry;
    private Runnable refreshAll;
    private SelectedNoteViewModel selectedNoteVm;
    private StringProperty rootNoteTitle;

    private ViewType currentViewType;
    private UUID baseNoteId;
    private Runnable currentViewRefresh = () -> { };

    /**
     * Creates a new ViewPaneContext with the given initial state.
     *
     * <p>The shared {@link ViewPaneDeps} must be set via
     * {@link #setDeps(ViewPaneDeps)} before any view switch is
     * attempted. This two-phase initialization avoids the circular
     * dependency between the pane contexts and the refreshAll
     * callback.</p>
     *
     * @param initialViewType the initial view type
     * @param titleProperty   the initial tab title property to bind
     * @param initialView     the initial view Parent
     * @param baseNoteId      the base note id for this pane
     * @param viewRefresh     the refresh action for the initial view
     */
    public ViewPaneContext(
            ViewType initialViewType,
            ReadOnlyStringProperty titleProperty,
            Parent initialView,
            UUID baseNoteId,
            Runnable viewRefresh) {
        this.currentViewType = Objects.requireNonNull(
                initialViewType);
        this.baseNoteId = baseNoteId;
        this.currentViewRefresh = viewRefresh;

        this.label = new Label();
        label.textProperty().bind(titleProperty);
        label.setStyle(
                "-fx-font-weight: bold; -fx-padding: 4 8;");
        label.setContextMenu(buildContextMenu());

        this.container = new VBox(label, initialView);
        VBox.setVgrow(initialView, Priority.ALWAYS);
    }

    /** Returns the VBox container for this pane. */
    public VBox getContainer() {
        return container;
    }

    /** Returns the current view type. */
    public ViewType getCurrentViewType() {
        return currentViewType;
    }

    /** Returns the label used for the tab title. */
    Label getLabel() {
        return label;
    }

    /**
     * Sets the shared dependencies needed for view switching.
     * Must be called before any view switch is attempted.
     *
     * @param deps the shared dependencies
     */
    public void setDeps(ViewPaneDeps deps) {
        Objects.requireNonNull(deps, "deps must not be null");
        this.noteService = deps.noteService();
        this.linkService = deps.linkService();
        this.schemaRegistry = deps.schemaRegistry();
        this.refreshAll = deps.refreshAll();
        this.selectedNoteVm = deps.selectedNoteVm();
        this.rootNoteTitle = deps.rootNoteTitle();
    }

    /**
     * Refreshes the current view by invoking its reload action.
     * Called as part of the global refreshAll cycle.
     */
    public void refreshCurrentView() {
        currentViewRefresh.run();
    }

    /**
     * Switches this pane to the given view type, preserving
     * the base note id.
     *
     * @param newType the view type to switch to
     */
    public void switchView(ViewType newType) {
        if (newType == currentViewType) {
            return;
        }
        try {
            doSwitchView(newType);
        } catch (IOException e) {
            LOG.error("Failed to switch view to {}",
                    newType, e);
        }
    }

    @SuppressWarnings("CyclomaticComplexity")
    private void doSwitchView(ViewType newType)
            throws IOException {
        label.textProperty().unbind();

        ReadOnlyStringProperty newTitleProp;

        switch (newType) {
            case MAP -> {
                MapViewModel vm = new MapViewModel(
                        rootNoteTitle, noteService);
                vm.setBaseNoteId(baseNoteId);
                vm.setOnDataChanged(refreshAll);
                wireSelection(vm.selectedNoteIdProperty());
                Parent view = loadFxml(newType, c ->
                        ((MapViewController) c)
                                .initViewModel(vm));
                replaceView(view);
                newTitleProp = vm.tabTitleProperty();
                currentViewRefresh = vm::loadNotes;
                vm.loadNotes();
            }
            case OUTLINE -> {
                OutlineViewModel vm = new OutlineViewModel(
                        rootNoteTitle, noteService);
                vm.setBaseNoteId(baseNoteId);
                vm.setOnDataChanged(refreshAll);
                wireSelection(vm.selectedNoteIdProperty());
                Parent view = loadFxml(newType, c ->
                        ((OutlineViewController) c)
                                .initViewModel(vm));
                replaceView(view);
                newTitleProp = vm.tabTitleProperty();
                currentViewRefresh = vm::loadNotes;
                vm.loadNotes();
            }
            case TREEMAP -> {
                TreemapViewModel vm = new TreemapViewModel(
                        rootNoteTitle, noteService);
                vm.setBaseNoteId(baseNoteId);
                vm.setOnDataChanged(refreshAll);
                wireSelection(vm.selectedNoteIdProperty());
                Parent view = loadFxml(newType, c ->
                        ((TreemapViewController) c)
                                .initViewModel(vm));
                replaceView(view);
                newTitleProp = vm.tabTitleProperty();
                currentViewRefresh = vm::loadNotes;
                vm.loadNotes();
            }
            case HYPERBOLIC -> {
                HyperbolicViewModel vm =
                        new HyperbolicViewModel(
                                noteService, linkService);
                vm.setOnDataChanged(refreshAll);
                wireSelection(vm.selectedNoteIdProperty());
                if (baseNoteId != null) {
                    vm.setFocusNote(baseNoteId);
                }
                Parent view = loadFxml(newType, c ->
                        ((HyperbolicViewController) c)
                                .initViewModel(vm));
                replaceView(view);
                newTitleProp = vm.tabTitleProperty();
                currentViewRefresh = () -> {
                    if (vm.getFocusNoteId() != null) {
                        vm.setFocusNote(
                                vm.getFocusNoteId());
                    }
                };
            }
            case BROWSER -> {
                AttributeBrowserViewModel vm =
                        new AttributeBrowserViewModel(
                                noteService, schemaRegistry);
                vm.setOnDataChanged(refreshAll);
                Parent view = loadFxml(newType, c ->
                        ((AttributeBrowserViewController) c)
                                .initViewModel(vm));
                replaceView(view);
                newTitleProp = vm.tabTitleProperty();
                currentViewRefresh = vm::groupNotes;
                vm.groupNotes();
            }
            default -> throw new IllegalArgumentException(
                    "Unknown view type: " + newType);
        }

        label.textProperty().bind(newTitleProp);
        currentViewType = newType;
        label.setContextMenu(buildContextMenu());
    }

    private ContextMenu buildContextMenu() {
        ContextMenu menu = new ContextMenu();
        for (ViewType type : ViewType.values()) {
            MenuItem item = new MenuItem(type.displayName());
            item.setDisable(type == currentViewType);
            item.setOnAction(e -> switchView(type));
            menu.getItems().add(item);
        }
        return menu;
    }

    private void wireSelection(
            ObjectProperty<UUID> source) {
        source.addListener((obs, oldVal, newVal) ->
                selectedNoteVm.setSelectedNoteId(newVal));
    }

    private Parent loadFxml(ViewType viewType,
            java.util.function.Consumer<Object> init)
            throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource(
                        "/com/embervault/adapter/in/ui/view/"
                                + viewType.fxmlFile()));
        Parent view = loader.load();
        init.accept(loader.getController());
        return view;
    }

    private void replaceView(Parent newView) {
        if (container.getChildren().size() > 1) {
            container.getChildren().set(1, newView);
        } else {
            container.getChildren().add(newView);
        }
        VBox.setVgrow(newView, Priority.ALWAYS);
    }
}
