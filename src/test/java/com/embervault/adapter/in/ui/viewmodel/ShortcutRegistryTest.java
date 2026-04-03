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
        assertEquals("Create a new note",
                result.get().description());
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

    @Test
    @DisplayName("search filters shortcuts by name substring (case-insensitive)")
    void search_shouldFilterByNameCaseInsensitive() {
        registry.register("Shortcut+N", "New Note",
                "Create a new note", () -> { });
        registry.register("Shortcut+F", "Find",
                "Open search", () -> { });
        registry.register("Shortcut+Shift+N", "New Child Note",
                "Create child note", () -> { });

        List<ShortcutAction> results = registry.search("new");

        assertEquals(2, results.size());
        assertEquals("New Note", results.get(0).name());
        assertEquals("New Child Note", results.get(1).name());
    }

    @Test
    @DisplayName("search also matches description substring")
    void search_shouldMatchDescription() {
        registry.register("Shortcut+F", "Find",
                "Open search panel", () -> { });

        List<ShortcutAction> results = registry.search("search");

        assertEquals(1, results.size());
        assertEquals("Find", results.get(0).name());
    }

    @Test
    @DisplayName("search with empty query returns all shortcuts")
    void search_shouldReturnAllForEmptyQuery() {
        registry.register("Shortcut+N", "New Note",
                "Create note", () -> { });
        registry.register("Shortcut+F", "Find",
                "Open search", () -> { });

        List<ShortcutAction> results = registry.search("");

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("registering duplicate key replaces previous action")
    void register_shouldReplaceDuplicateKey() {
        registry.register("Shortcut+N", "Old Action",
                "Old description", () -> { });
        registry.register("Shortcut+N", "New Action",
                "New description", () -> { });

        assertEquals(1, registry.getAll().size());
        assertEquals("New Action",
                registry.lookup("Shortcut+N").get().name());
    }

    @Test
    @DisplayName("unregister removes a shortcut by key combination")
    void unregister_shouldRemoveShortcut() {
        registry.register("Shortcut+N", "New Note",
                "Create note", () -> { });
        registry.unregister("Shortcut+N");

        assertTrue(registry.lookup("Shortcut+N").isEmpty());
        assertEquals(0, registry.getAll().size());
    }

    @Test
    @DisplayName("search also matches key combination string")
    void search_shouldMatchKeyCombination() {
        registry.register("Shortcut+N", "New Note",
                "Create note", () -> { });

        List<ShortcutAction> results =
                registry.search("Shortcut+N");

        assertEquals(1, results.size());
    }
}
