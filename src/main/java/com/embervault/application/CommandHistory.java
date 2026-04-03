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
}
