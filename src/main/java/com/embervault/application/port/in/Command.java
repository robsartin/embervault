package com.embervault.application.port.in;

/**
 * A reversible command that can be executed and undone.
 *
 * <p>Commands capture the before-state needed to reverse their effect.
 * They are used by {@link UndoRedoUseCase} to provide undo/redo support.</p>
 */
public interface Command {

    /**
     * Executes this command.
     */
    void execute();

    /**
     * Reverses the effect of this command.
     */
    void undo();

    /**
     * Returns a human-readable description of this command.
     *
     * @return the description
     */
    String description();
}
