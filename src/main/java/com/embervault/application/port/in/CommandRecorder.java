package com.embervault.application.port.in;

/**
 * Inbound port for recording undoable commands.
 *
 * <p>ViewModels call {@link #record} after performing an action to
 * enable undo/redo. Each recorded action captures an undo and redo
 * function pair.</p>
 */
public interface CommandRecorder {

    /**
     * Records an undoable action.
     *
     * @param description human-readable description of the action
     * @param undoAction  the action to perform on undo
     * @param redoAction  the action to perform on redo
     */
    void record(String description, Runnable undoAction,
            Runnable redoAction);
}
