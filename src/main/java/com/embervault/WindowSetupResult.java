package com.embervault;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import javafx.beans.property.StringProperty;

/**
 * Result of {@link WindowBuilder#build}, containing the per-window
 * components that are common to every window regardless of initial
 * view type.
 *
 * @param appState       the per-window application state
 * @param eventBus       the per-window event bus
 * @param selectedNoteVm the per-window selected-note view model
 * @param paneDeps       the shared dependencies for view panes
 * @param rootNoteTitle  the root note title property
 */
public record WindowSetupResult(
    AppState appState,
    EventBus eventBus,
    SelectedNoteViewModel selectedNoteVm,
    ViewPaneDeps paneDeps,
    StringProperty rootNoteTitle) {
}
