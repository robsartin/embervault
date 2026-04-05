package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.embervault.application.CommandHistory;
import com.embervault.application.UndoRedoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndoRedoShortcutTest {

    private ShortcutRegistry registry;
    private UndoRedoService undoRedoService;

    @BeforeEach
    void setUp() {
        registry = new ShortcutRegistry();
        CommandHistory history = new CommandHistory();
        undoRedoService = new UndoRedoService(history);
        UndoRedoShortcuts.register(registry, undoRedoService);
    }

    @Test
    @DisplayName("registers Shortcut+Z for undo")
    void registersUndoShortcut() {
        assertTrue(registry.lookup("Shortcut+Z").isPresent());
        assertEquals("Undo",
                registry.lookup("Shortcut+Z").orElseThrow().name());
    }

    @Test
    @DisplayName("registers Shortcut+Shift+Z for redo")
    void registersRedoShortcut() {
        assertTrue(registry.lookup("Shortcut+Shift+Z").isPresent());
        assertEquals("Redo",
                registry.lookup("Shortcut+Shift+Z").orElseThrow()
                        .name());
    }

    @Test
    @DisplayName("undo shortcut triggers undo")
    void undoShortcut_triggersUndo() {
        List<String> calls = new ArrayList<>();
        undoRedoService.record("test",
                () -> calls.add("undo"),
                () -> calls.add("redo"));

        registry.lookup("Shortcut+Z").orElseThrow().action().run();

        assertEquals(List.of("undo"), calls);
    }

    @Test
    @DisplayName("redo shortcut triggers redo")
    void redoShortcut_triggersRedo() {
        List<String> calls = new ArrayList<>();
        undoRedoService.record("test",
                () -> calls.add("undo"),
                () -> calls.add("redo"));

        registry.lookup("Shortcut+Z").orElseThrow().action().run();
        calls.clear();
        registry.lookup("Shortcut+Shift+Z").orElseThrow()
                .action().run();

        assertEquals(List.of("redo"), calls);
    }

    @Test
    @DisplayName("undo and redo shortcuts are global")
    void shortcuts_areGlobal() {
        assertTrue(registry.lookup("Shortcut+Z").orElseThrow()
                .global());
        assertTrue(registry.lookup("Shortcut+Shift+Z").orElseThrow()
                .global());
    }
}
