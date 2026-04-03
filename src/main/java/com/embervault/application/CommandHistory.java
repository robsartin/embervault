package com.embervault.application;

import java.util.ArrayDeque;
import java.util.Deque;

import com.embervault.application.port.in.Command;

/**
 * Maintains undo and redo stacks for reversible commands.
 *
 * <p>When a command is executed, it is pushed onto the undo stack and the
 * redo stack is cleared. Undoing pops from the undo stack and pushes onto
 * redo; redoing does the reverse.</p>
 */
public class CommandHistory {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Executes the given command and records it for undo.
     *
     * @param command the command to execute
     */
    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Undoes the most recently executed command.
     *
     * <p>If the undo stack is empty, this method does nothing.</p>
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    /**
     * Re-applies the most recently undone command.
     *
     * <p>If the redo stack is empty, this method does nothing.</p>
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    /**
     * Returns whether there are commands that can be undone.
     *
     * @return true if the undo stack is not empty
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Returns whether there are commands that can be redone.
     *
     * @return true if the redo stack is not empty
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
