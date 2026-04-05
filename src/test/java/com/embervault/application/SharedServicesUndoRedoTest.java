package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.SharedServices;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SharedServicesUndoRedoTest {

    @Test
    @DisplayName("SharedServices provides UndoRedoUseCase")
    void sharedServices_providesUndoRedoUseCase() {
        SharedServices services = SharedServices.create();
        assertNotNull(services.undoRedoUseCase());
    }

    @Test
    @DisplayName("SharedServices provides CommandRecorder")
    void sharedServices_providesCommandRecorder() {
        SharedServices services = SharedServices.create();
        assertNotNull(services.commandRecorder());
    }

    @Test
    @DisplayName("undo/redo and recorder share the same history")
    void undoRedoAndRecorder_shareSameHistory() {
        SharedServices services = SharedServices.create();
        services.commandRecorder().record("test",
                () -> { }, () -> { });
        assertTrue(services.undoRedoUseCase().canUndo());
    }
}
