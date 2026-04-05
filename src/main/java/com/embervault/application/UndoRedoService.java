package com.embervault.application;

import java.util.Objects;

import com.embervault.application.port.in.UndoRedoUseCase;

/**
 * Application service implementing undo/redo operations.
 *
 * <p>Delegates to {@link CommandHistory} for stack management.</p>
 */
public final class UndoRedoService implements UndoRedoUseCase {

    private final CommandHistory history;

    /**
     * Creates the service.
     *
     * @param history the command history
     */
    public UndoRedoService(CommandHistory history) {
        this.history = Objects.requireNonNull(history);
    }

    @Override
    public boolean undo() {
        return history.undo();
    }

    @Override
    public boolean redo() {
        return history.redo();
    }

    @Override
    public boolean canUndo() {
        return history.canUndo();
    }

    @Override
    public boolean canRedo() {
        return history.canRedo();
    }
}
