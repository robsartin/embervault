package com.embervault.adapter.in.ui.viewmodel;

import com.embervault.application.port.in.UndoRedoUseCase;

/**
 * Registers undo/redo keyboard shortcuts in a {@link ShortcutRegistry}.
 *
 * <p>Binds Shortcut+Z to undo and Shortcut+Shift+Z to redo. Both
 * shortcuts are registered as global (fire even during text editing).</p>
 */
public final class UndoRedoShortcuts {

    private UndoRedoShortcuts() { }

    /**
     * Registers undo and redo shortcuts.
     *
     * @param registry the shortcut registry to register with
     * @param undoRedo the undo/redo use case
     */
    public static void register(ShortcutRegistry registry,
            UndoRedoUseCase undoRedo) {
        registry.register("Shortcut+Z", "Undo",
                "Undo the last action",
                undoRedo::undo, true);
        registry.register("Shortcut+Shift+Z", "Redo",
                "Redo the last undone action",
                undoRedo::redo, true);
    }
}
