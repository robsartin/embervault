package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry of keyboard shortcut actions.
 *
 * <p>Maps string-based key combination descriptions to named actions.
 * Uses strings rather than {@code javafx.scene.input.KeyCombination}
 * to comply with ADR-0013 (ViewModels must not reference scene-graph classes).</p>
 */
public class ShortcutRegistry {

    private final Map<String, ShortcutAction> shortcuts =
            new LinkedHashMap<>();

    /**
     * Registers a shortcut action.
     *
     * @param keyCombination string key combination (e.g. "Shortcut+N")
     * @param name           human-readable name
     * @param description    longer description
     * @param action         runnable to execute
     */
    public void register(String keyCombination, String name,
            String description, Runnable action) {
        register(keyCombination, name, description, action, false);
    }

    /**
     * Registers a shortcut action with explicit global flag.
     *
     * @param keyCombination string key combination
     * @param name           human-readable name
     * @param description    longer description
     * @param action         runnable to execute
     * @param global         true if this shortcut should fire even
     *                       when a text input has focus
     */
    public void register(String keyCombination, String name,
            String description, Runnable action, boolean global) {
        shortcuts.put(keyCombination,
                new ShortcutAction(keyCombination, name,
                        description, action, global));
    }

    /**
     * Looks up a shortcut action by its key combination string.
     *
     * @param keyCombination the key combination to look up
     * @return the action if registered, or empty
     */
    public Optional<ShortcutAction> lookup(String keyCombination) {
        return Optional.ofNullable(shortcuts.get(keyCombination));
    }

    /**
     * Removes a shortcut registration.
     *
     * @param keyCombination the key combination to unregister
     */
    public void unregister(String keyCombination) {
        shortcuts.remove(keyCombination);
    }

    /**
     * Returns all registered shortcut actions in insertion order.
     *
     * @return unmodifiable list of all actions
     */
    public List<ShortcutAction> getAll() {
        return List.copyOf(shortcuts.values());
    }

    /**
     * Searches registered shortcuts by name, description, or key
     * combination substring (case-insensitive). An empty query
     * returns all shortcuts.
     *
     * @param query the search query
     * @return matching actions in insertion order
     */
    public List<ShortcutAction> search(String query) {
        if (query == null || query.isEmpty()) {
            return getAll();
        }
        String lowerQuery = query.toLowerCase();
        List<ShortcutAction> results = new ArrayList<>();
        for (ShortcutAction action : shortcuts.values()) {
            if (action.name().toLowerCase().contains(lowerQuery)
                    || action.description().toLowerCase()
                            .contains(lowerQuery)
                    || action.keyCombination().toLowerCase()
                            .contains(lowerQuery)) {
                results.add(action);
            }
        }
        return results;
    }
}
