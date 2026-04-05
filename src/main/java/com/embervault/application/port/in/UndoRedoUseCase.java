package com.embervault.application.port.in;

/**
 * Use case for undo/redo operations.
 *
 * <p>Provides methods to undo the most recent command, redo the most
 * recently undone command, and query whether those operations are
 * available.</p>
 */
public interface UndoRedoUseCase {

    /**
     * Undoes the most recent command.
     *
     * @return true if a command was undone, false if nothing to undo
     */
    boolean undo();

    /**
     * Redoes the most recently undone command.
     *
     * @return true if a command was redone, false if nothing to redo
     */
    boolean redo();

    /**
     * Returns true if there is a command available to undo.
     *
     * @return true if undo is available
     */
    boolean canUndo();

    /**
     * Returns true if there is a command available to redo.
     *
     * @return true if redo is available
     */
    boolean canRedo();
}
