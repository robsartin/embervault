package com.embervault;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.application.port.in.CommandRecorder;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import javafx.beans.property.StringProperty;

/**
 * Shared dependencies needed by {@link ViewPaneContext} to create
 * view models and wire views during a view-type switch.
 *
 * @param noteService      the note service
 * @param linkService      the link service
 * @param schemaRegistry   the attribute schema registry
 * @param appState         the shared application state for
 *                         data-change notification
 * @param eventBus         the shared event bus for fire-and-forget events
 * @param selectedNoteVm   the shared selected-note view model
 * @param rootNoteTitle    the root note title property
 * @param commandRecorder  the command recorder for undo/redo
 */
public record ViewPaneDeps(
        NoteService noteService,
        LinkService linkService,
        AttributeSchemaRegistry schemaRegistry,
        AppState appState,
        EventBus eventBus,
        SelectedNoteViewModel selectedNoteVm,
        StringProperty rootNoteTitle,
        CommandRecorder commandRecorder) {
}
