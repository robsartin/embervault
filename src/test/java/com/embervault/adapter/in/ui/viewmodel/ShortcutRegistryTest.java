package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShortcutRegistryTest {

  private ShortcutRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new ShortcutRegistry();
  }

  @Test
  @DisplayName("register and retrieve a shortcut action by key combination")
  void register_shouldAllowRetrievalByKeyCombination() {
    Runnable action = () -> { };
    registry.register("Shortcut+N", "New Note",
        "Create a new note", action);

    Optional<ShortcutAction> result =
        registry.lookup("Shortcut+N");

    assertTrue(result.isPresent());
    assertEquals("New Note", result.get().name());
    assertEquals("Create a new note", result.get().description());
    assertEquals(action, result.get().action());
  }

  @Test
  @DisplayName("getAll returns all registered shortcuts in insertion order")
  void getAll_shouldReturnAllRegisteredShortcuts() {
    registry.register("Shortcut+N", "New Note",
        "Create a new note", () -> { });
    registry.register("Shortcut+F", "Find",
        "Open search", () -> { });

    List<ShortcutAction> all = registry.getAll();

    assertEquals(2, all.size());
    assertEquals("New Note", all.get(0).name());
    assertEquals("Find", all.get(1).name());
  }

  @Test
  @DisplayName("lookup returns empty for unregistered key combination")
  void lookup_shouldReturnEmptyForUnregisteredKey() {
    Optional<ShortcutAction> result =
        registry.lookup("Shortcut+Z");

    assertTrue(result.isEmpty());
  }
}
