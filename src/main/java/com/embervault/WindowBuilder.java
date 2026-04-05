package com.embervault;

import java.util.Objects;
import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Creates the common per-window components shared by both
 * {@link App} and {@link WindowFactory}.
 *
 * <p>Eliminates duplication of AppState, EventBus,
 * SelectedNoteViewModel, ViewPaneDeps creation, and the
 * dataVersion-to-WindowManager wiring that was previously
 * copy-pasted between the two entry points.</p>
 */
public final class WindowBuilder {

    private WindowBuilder() { }

    /**
     * Builds the common per-window components from the given setup
     * context.
     *
     * @param ctx the window setup context with shared services
     * @return a result containing the created components
     */
    public static WindowSetupResult build(WindowSetupContext ctx) {
        Objects.requireNonNull(ctx, "ctx must not be null");

        AppState appState = new AppState();
        EventBus eventBus = new EventBus();
        SelectedNoteViewModel selectedNoteVm =
                new SelectedNoteViewModel(
                        ctx.noteService(), ctx.noteService(),
                        ctx.noteService(), appState, eventBus);
        StringProperty rootNoteTitle = new SimpleStringProperty(
                ctx.project().getRootNote().getTitle());
        ViewPaneDeps paneDeps = new ViewPaneDeps(
                ctx.noteService(), ctx.linkService(),
                ctx.schemaRegistry(), appState, eventBus,
                selectedNoteVm, rootNoteTitle,
                ctx.commandRecorder());

        appState.dataVersionProperty().addListener(
                (obs, oldVal, newVal) ->
                        ctx.windowManager().notifyAllWindows());

        return new WindowSetupResult(
                appState, eventBus, selectedNoteVm,
                paneDeps, rootNoteTitle);
    }

    /**
     * Wires a view model's selected-note-id property to the shared
     * {@link SelectedNoteViewModel} so that selection changes in
     * any view are reflected in the text pane.
     *
     * @param source the source property (from the view's ViewModel)
     * @param target the shared selected-note view model
     */
    public static void wireSelection(
            ObjectProperty<UUID> source,
            SelectedNoteViewModel target) {
        source.addListener(
                (obs, oldVal, newVal) ->
                        target.setSelectedNoteId(newVal));
    }
}
