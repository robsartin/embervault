package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.application.port.in.CommandRecorder;
import com.embervault.application.port.in.UndoRedoUseCase;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndoRedoServiceTest {

    private UndoRedoService service;
    private UndoRedoUseCase undoRedo;
    private CommandHistory history;

    @BeforeEach
    void setUp() {
        history = new CommandHistory();
        service = new UndoRedoService(history);
        undoRedo = service;
    }

    @Test
    @DisplayName("implements UndoRedoUseCase")
    void implementsUseCase() {
        assertTrue(undoRedo instanceof UndoRedoUseCase);
    }

    @Test
    @DisplayName("implements CommandRecorder")
    void implementsCommandRecorder() {
        assertTrue(service instanceof CommandRecorder);
    }

    @Test
    @DisplayName("record creates an undoable command")
    void record_createsUndoableCommand() {
        List<String> calls = new ArrayList<>();
        service.record("test action",
                () -> calls.add("undo"),
                () -> calls.add("redo"));

        assertTrue(undoRedo.canUndo());
        undoRedo.undo();
        assertEquals(List.of("undo"), calls);
    }

    @Test
    @DisplayName("recorded command can be redone")
    void record_canBeRedone() {
        List<String> calls = new ArrayList<>();
        service.record("test action",
                () -> calls.add("undo"),
                () -> calls.add("redo"));

        undoRedo.undo();
        calls.clear();
        undoRedo.redo();
        assertEquals(List.of("redo"), calls);
    }

    @Test
    @DisplayName("delegates canUndo to CommandHistory")
    void canUndo_delegatesToHistory() {
        assertFalse(undoRedo.canUndo());
        history.push(stubCommand());
        assertTrue(undoRedo.canUndo());
    }

    @Test
    @DisplayName("delegates canRedo to CommandHistory")
    void canRedo_delegatesToHistory() {
        assertFalse(undoRedo.canRedo());
        history.push(stubCommand());
        history.undo();
        assertTrue(undoRedo.canRedo());
    }

    @Test
    @DisplayName("undo delegates to CommandHistory")
    void undo_delegatesToHistory() {
        List<String> calls = new ArrayList<>();
        history.push(trackingCommand(calls));

        assertTrue(undoRedo.undo());
        assertEquals(List.of("undo"), calls);
    }

    @Test
    @DisplayName("redo delegates to CommandHistory")
    void redo_delegatesToHistory() {
        List<String> calls = new ArrayList<>();
        history.push(trackingCommand(calls));
        undoRedo.undo();
        calls.clear();

        assertTrue(undoRedo.redo());
        assertEquals(List.of("redo"), calls);
    }

    @Test
    @DisplayName("undo on empty returns false")
    void undo_empty_returnsFalse() {
        assertFalse(undoRedo.undo());
    }

    @Test
    @DisplayName("redo on empty returns false")
    void redo_empty_returnsFalse() {
        assertFalse(undoRedo.redo());
    }

    private Reversible stubCommand() {
        return trackingCommand(new ArrayList<>());
    }

    private Reversible trackingCommand(List<String> calls) {
        return new Reversible() {
            @Override
            public void undo() {
                calls.add("undo");
            }

            @Override
            public void redo() {
                calls.add("redo");
            }

            @Override
            public String description() {
                return "test";
            }
        };
    }
}
