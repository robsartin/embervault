package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandHistoryTest {

    private CommandHistory history;

    @BeforeEach
    void setUp() {
        history = new CommandHistory();
    }

    @Test
    @DisplayName("new CommandHistory has nothing to undo")
    void newHistory_cannotUndo() {
        assertFalse(history.canUndo());
    }

    @Test
    @DisplayName("new CommandHistory has nothing to redo")
    void newHistory_cannotRedo() {
        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("undo on empty history returns false")
    void undo_emptyHistory_returnsFalse() {
        assertFalse(history.undo());
    }

    @Test
    @DisplayName("redo on empty history returns false")
    void redo_emptyHistory_returnsFalse() {
        assertFalse(history.redo());
    }

    @Test
    @DisplayName("push makes canUndo true")
    void push_makesCanUndoTrue() {
        history.push(stubCommand("test"));
        assertTrue(history.canUndo());
    }

    @Test
    @DisplayName("undo calls command.undo() and moves to redo stack")
    void undo_callsUndoAndMovesToRedoStack() {
        List<String> calls = new ArrayList<>();
        history.push(trackingCommand("cmd", calls));

        assertTrue(history.undo());

        assertEquals(List.of("undo:cmd"), calls);
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    @DisplayName("redo calls command.redo() and moves back to undo stack")
    void redo_callsRedoAndMovesToUndoStack() {
        List<String> calls = new ArrayList<>();
        history.push(trackingCommand("cmd", calls));
        history.undo();
        calls.clear();

        assertTrue(history.redo());

        assertEquals(List.of("redo:cmd"), calls);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("push clears redo stack")
    void push_clearsRedoStack() {
        history.push(stubCommand("first"));
        history.undo();
        assertTrue(history.canRedo());

        history.push(stubCommand("second"));
        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("undo/redo preserves LIFO order")
    void undoRedo_preservesLifoOrder() {
        List<String> calls = new ArrayList<>();
        history.push(trackingCommand("A", calls));
        history.push(trackingCommand("B", calls));

        history.undo();
        history.undo();

        assertEquals(List.of("undo:B", "undo:A"), calls);
    }

    private Reversible stubCommand(String desc) {
        return new Reversible() {
            @Override
            public void undo() { }

            @Override
            public void redo() { }

            @Override
            public String description() {
                return desc;
            }
        };
    }

    private Reversible trackingCommand(String desc, List<String> calls) {
        return new Reversible() {
            @Override
            public void undo() {
                calls.add("undo:" + desc);
            }

            @Override
            public void redo() {
                calls.add("redo:" + desc);
            }

            @Override
            public String description() {
                return desc;
            }
        };
    }
}
