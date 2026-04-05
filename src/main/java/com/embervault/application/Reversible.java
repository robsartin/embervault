package com.embervault.application;

/**
 * A command that can be undone and redone.
 *
 * <p>Implementations capture enough state to reverse and re-apply
 * a single user action.</p>
 */
public interface Reversible {

    /**
     * Undoes this command, restoring the previous state.
     */
    void undo();

    /**
     * Re-applies this command after it has been undone.
     */
    void redo();

    /**
     * Returns a human-readable description of this command.
     *
     * @return the description
     */
    String description();
}
