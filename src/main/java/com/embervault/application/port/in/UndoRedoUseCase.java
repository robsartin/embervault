package com.embervault.application.port.in;

/**
 * Use case for undoing and redoing commands.
 *
 * <p>Provides undo/redo capability backed by a command history stack.
 * ViewModels use this to expose undo/redo actions to the UI.</p>
 */
public interface UndoRedoUseCase {

    /**
     * Executes a command and records it for potential undo.
     *
     * @param command the command to execute
     */
    void execute(Command command);

    /**
     * Undoes the most recently executed command.
     */
    void undo();

    /**
     * Re-applies the most recently undone command.
     */
    void redo();

    /**
     * Returns whether there are commands that can be undone.
     *
     * @return true if undo is available
     */
    boolean canUndo();

    /**
     * Returns whether there are commands that can be redone.
     *
     * @return true if redo is available
     */
    boolean canRedo();
}
