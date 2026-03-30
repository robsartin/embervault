package com.embervault;

import java.util.UUID;

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
 * @param refreshAll     callback to refresh all windows
 * @param ownerStage     this window's stage (for modal dialogs)
 * @param onFind         callback for Edit &gt; Find, or null
 */
public record WindowContext(
        SharedServices sharedServices,
        WindowManager windowManager,
        ObjectProperty<UUID> selectedNoteId,
        Runnable refreshAll,
        Stage ownerStage,
        Runnable onFind) {
}
