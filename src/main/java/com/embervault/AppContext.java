package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.ColorScheme;
import com.embervault.domain.Project;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Shared application context holding dependencies used across
 * menu-building and other setup methods.
 *
 * @param mapViewModel the map view model
 * @param hyperbolicViewModel the hyperbolic view model
 * @param searchViewModel the search view model
 * @param mainSplitPane the main split pane layout
 * @param browserEditorPane the browser/editor split pane
 * @param hyperbolicContainer the hyperbolic view container
 * @param project the current project
 * @param stampService the stamp service
 * @param selectedNoteId the currently selected note id property
 * @param refreshAll callback to refresh all views
 * @param ownerStage the primary application stage
 * @param colorSchemeApplier callback to apply a ColorScheme to all views
 */
public record AppContext(
        MapViewModel mapViewModel,
        HyperbolicViewModel hyperbolicViewModel,
        SearchViewModel searchViewModel,
        SplitPane mainSplitPane,
        SplitPane browserEditorPane,
        VBox hyperbolicContainer,
        Project project,
        StampService stampService,
        ObjectProperty<UUID> selectedNoteId,
        Runnable refreshAll,
        Stage ownerStage,
        Consumer<ColorScheme> colorSchemeApplier) {
}
