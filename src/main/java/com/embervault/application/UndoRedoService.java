package com.embervault.application;

import java.util.Objects;

import com.embervault.application.port.in.CommandRecorder;
import com.embervault.application.port.in.UndoRedoUseCase;

/**
 * Application service implementing undo/redo operations and command
 * recording.
 *
 * <p>Delegates to {@link CommandHistory} for stack management.
 * ViewModels use the {@link CommandRecorder} interface to register
 * undoable actions.</p>
 */
public final class UndoRedoService
        implements UndoRedoUseCase, CommandRecorder {

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

    @Override
    public void record(String description, Runnable undoAction,
            Runnable redoAction) {
        history.push(new Reversible() {
            @Override
            public void undo() {
                undoAction.run();
            }

            @Override
            public void redo() {
                redoAction.run();
            }

            @Override
            public String description() {
                return description;
            }
        });
    }
}
