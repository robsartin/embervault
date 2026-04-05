package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
