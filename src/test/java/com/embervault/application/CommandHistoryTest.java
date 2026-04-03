package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.application.port.in.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandHistoryTest {

    private CommandHistory history;

    @BeforeEach
    void setUp() {
        history = new CommandHistory();
    }

    private Command counterCommand(int[] counter) {
        return new Command() {
            @Override
            public void execute() {
                counter[0]++;
            }

            @Override
            public void undo() {
                counter[0]--;
            }

            @Override
            public String description() {
                return "increment";
            }
        };
    }

    @Test
    @DisplayName("execute() calls command's execute method")
    void execute_shouldCallCommandExecute() {
        int[] counter = {0};
        history.execute(counterCommand(counter));
        assertEquals(1, counter[0]);
    }

    @Test
    @DisplayName("undo() reverses the last executed command")
    void undo_shouldReverseLastCommand() {
        int[] counter = {0};
        history.execute(counterCommand(counter));

        history.undo();

        assertEquals(0, counter[0]);
    }

    @Test
    @DisplayName("undo() on empty history does nothing")
    void undo_onEmptyHistory_shouldDoNothing() {
        history.undo(); // should not throw
    }

    @Test
    @DisplayName("canUndo() returns false when history is empty")
    void canUndo_shouldReturnFalseWhenEmpty() {
        assertFalse(history.canUndo());
    }

    @Test
    @DisplayName("canUndo() returns true after execute")
    void canUndo_shouldReturnTrueAfterExecute() {
        int[] counter = {0};
        history.execute(counterCommand(counter));
        assertTrue(history.canUndo());
    }

    @Test
    @DisplayName("redo() re-applies the last undone command")
    void redo_shouldReapplyLastUndoneCommand() {
        int[] counter = {0};
        history.execute(counterCommand(counter));
        history.undo();

        history.redo();

        assertEquals(1, counter[0]);
    }

    @Test
    @DisplayName("redo() on empty redo stack does nothing")
    void redo_onEmptyRedoStack_shouldDoNothing() {
        history.redo(); // should not throw
    }

    @Test
    @DisplayName("canRedo() returns false when redo stack is empty")
    void canRedo_shouldReturnFalseWhenEmpty() {
        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("canRedo() returns true after undo")
    void canRedo_shouldReturnTrueAfterUndo() {
        int[] counter = {0};
        history.execute(counterCommand(counter));
        history.undo();
        assertTrue(history.canRedo());
    }

    @Test
    @DisplayName("execute() clears redo stack")
    void execute_shouldClearRedoStack() {
        int[] counter = {0};
        history.execute(counterCommand(counter));
        history.undo();
        assertTrue(history.canRedo());

        history.execute(counterCommand(counter));

        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("multiple undo/redo operations maintain correct state")
    void multipleUndoRedo_shouldMaintainCorrectState() {
        int[] counter = {0};
        history.execute(counterCommand(counter));
        history.execute(counterCommand(counter));
        history.execute(counterCommand(counter));
        assertEquals(3, counter[0]);

        history.undo();
        assertEquals(2, counter[0]);

        history.undo();
        assertEquals(1, counter[0]);

        history.redo();
        assertEquals(2, counter[0]);
    }
}
