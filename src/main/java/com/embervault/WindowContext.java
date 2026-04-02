package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import javafx.beans.property.ObjectProperty;
import javafx.stage.Stage;

/**
 * Per-window context for menu bar construction.
 *
 * <p>Each window has its own selection state, stage reference, and
 * optional search toggle. The shared services and window manager
 * are common across all windows.</p>
 *
 * @param sharedServices shared singleton services
 * @param windowManager  the application window manager
 * @param selectedNoteId this window's selected note id property
 * @param appState       the shared application state for data-change notification
 * @param ownerStage     this window's stage (for modal dialogs)
 * @param onFind         callback for Edit &gt; Find, or null
 * @param onBaseNoteChanged callback when loaded project changes
 *                          root note, or null
 */
public record WindowContext(
        SharedServices sharedServices,
        WindowManager windowManager,
        ObjectProperty<UUID> selectedNoteId,
        AppState appState,
        Stage ownerStage,
        Runnable onFind,
        Consumer<UUID> onBaseNoteChanged) {
}
