package com.embervault;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.Project;
import javafx.beans.property.ObjectProperty;
import javafx.stage.Stage;

/**
 * Shared application context holding dependencies used across
 * menu-building and other setup methods.
 *
 * @param outlineViewModel the outline view model for the primary window
 * @param searchViewModel the search view model
 * @param project the current project
 * @param stampService the stamp service
 * @param selectedNoteId the currently selected note id property
 * @param refreshAll callback to refresh all views
 * @param ownerStage the primary application stage
 */
public record AppContext(
        OutlineViewModel outlineViewModel,
        SearchViewModel searchViewModel,
        Project project,
        StampService stampService,
        ObjectProperty<UUID> selectedNoteId,
        Runnable refreshAll,
        Stage ownerStage) {
}
