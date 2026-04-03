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
class ShortcutRegistry {

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
  void register(String keyCombination, String name,
      String description, Runnable action) {
    shortcuts.put(keyCombination,
        new ShortcutAction(keyCombination, name, description, action));
  }

  /**
   * Looks up a shortcut action by its key combination string.
   *
   * @param keyCombination the key combination to look up
   * @return the action if registered, or empty
   */
  Optional<ShortcutAction> lookup(String keyCombination) {
    return Optional.ofNullable(shortcuts.get(keyCombination));
  }

  /**
   * Returns all registered shortcut actions in insertion order.
   *
   * @return unmodifiable list of all actions
   */
  List<ShortcutAction> getAll() {
    return List.copyOf(shortcuts.values());
  }

  /**
   * Searches registered shortcuts by name or description substring
   * (case-insensitive). An empty query returns all shortcuts.
   *
   * @param query the search query
   * @return matching actions in insertion order
   */
  List<ShortcutAction> search(String query) {
    if (query == null || query.isEmpty()) {
      return getAll();
    }
    String lowerQuery = query.toLowerCase();
    List<ShortcutAction> results = new ArrayList<>();
    for (ShortcutAction action : shortcuts.values()) {
      if (action.name().toLowerCase().contains(lowerQuery)
          || action.description().toLowerCase().contains(lowerQuery)) {
        results.add(action);
      }
    }
    return results;
  }
}
