package com.embervault.application;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Maintains undo/redo stacks of {@link Reversible} commands.
 *
 * <p>Pushing a new command clears the redo stack. Undo pops from the
 * undo stack and pushes onto the redo stack. Redo does the reverse.</p>
 */
public class CommandHistory {

    private final Deque<Reversible> undoStack = new ArrayDeque<>();
    private final Deque<Reversible> redoStack = new ArrayDeque<>();

    /**
     * Returns true if there is a command that can be undone.
     *
     * @return true if undo is available
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Returns true if there is a command that can be redone.
     *
     * @return true if redo is available
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Undoes the most recent command.
     *
     * @return true if a command was undone, false if the stack was empty
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Reversible command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }

    /**
     * Redoes the most recently undone command.
     *
     * @return true if a command was redone, false if the stack was empty
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Reversible command = redoStack.pop();
        command.redo();
        undoStack.push(command);
        return true;
    }

    /**
     * Pushes a new command onto the undo stack and clears the redo stack.
     *
     * @param command the command to push
     */
    public void push(Reversible command) {
        undoStack.push(command);
        redoStack.clear();
    }
}
