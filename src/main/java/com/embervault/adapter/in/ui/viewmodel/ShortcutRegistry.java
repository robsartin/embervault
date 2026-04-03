package com.embervault.adapter.in.ui.viewmodel;

import java.util.LinkedHashMap;
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
}
