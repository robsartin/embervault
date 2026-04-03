package com.embervault.adapter.in.ui.viewmodel;

/**
 * A named action bound to a keyboard shortcut.
 *
 * @param keyCombination string representation of the key combination
 *                       (e.g. "Shortcut+N", "Shortcut+Shift+N")
 * @param name           human-readable name for display
 * @param description    longer description for tooltips or command palette
 * @param action         the runnable to execute when the shortcut
 *                       is triggered
 * @param global         if true, this shortcut fires even when a text
 *                       input control has focus; if false, it is
 *                       suppressed during text editing
 */
public record ShortcutAction(
        String keyCombination,
        String name,
        String description,
        Runnable action,
        boolean global) {
}
