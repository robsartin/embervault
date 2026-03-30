package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tests for {@link WindowManager}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class WindowManagerTest {

    private WindowManager windowManager;
    private Stage testStage1;
    private Stage testStage2;

    @Start
    private void start(Stage stage) {
        stage.show();
        testStage1 = new Stage();
        testStage2 = new Stage();
    }

    @BeforeEach
    void setUp() {
        windowManager = new WindowManager();
    }

    @Test
    @DisplayName("register adds stage to tracked windows")
    void register_shouldAddStage() {
        windowManager.register(testStage1);
        assertEquals(1, windowManager.getWindows().size());
        assertTrue(windowManager.getWindows().contains(testStage1));
    }

    @Test
    @DisplayName("unregister removes stage from tracked windows")
    void unregister_shouldRemoveStage() {
        windowManager.register(testStage1);
        windowManager.unregister(testStage1);
        assertEquals(0, windowManager.getWindows().size());
    }

    @Test
    @DisplayName("getWindows returns unmodifiable view")
    void getWindows_shouldBeUnmodifiable() {
        windowManager.register(testStage1);
        try {
            windowManager.getWindows().add(testStage2);
            assertFalse(true, "Should have thrown");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    @DisplayName("isLastWindow returns true when only one window")
    void isLastWindow_shouldReturnTrueForSingle() {
        windowManager.register(testStage1);
        assertTrue(windowManager.isLastWindow(testStage1));
    }

    @Test
    @DisplayName("isLastWindow returns false when multiple windows")
    void isLastWindow_shouldReturnFalseForMultiple() {
        windowManager.register(testStage1);
        windowManager.register(testStage2);
        assertFalse(windowManager.isLastWindow(testStage1));
    }

    @Test
    @DisplayName("addRefreshListener and notifyAll propagates")
    void notifyAll_shouldCallAllListeners() {
        AtomicInteger count = new AtomicInteger(0);
        windowManager.addRefreshListener(count::incrementAndGet);
        windowManager.addRefreshListener(count::incrementAndGet);
        windowManager.notifyAllWindows();
        assertEquals(2, count.get());
    }

    @Test
    @DisplayName("removeRefreshListener stops notifications")
    void removeRefreshListener_shouldStopNotification() {
        AtomicInteger count = new AtomicInteger(0);
        Runnable listener = count::incrementAndGet;
        windowManager.addRefreshListener(listener);
        windowManager.removeRefreshListener(listener);
        windowManager.notifyAllWindows();
        assertEquals(0, count.get());
    }

    @Test
    @DisplayName("closeAll closes all tracked stages")
    void closeAll_shouldCloseAllStages() {
        windowManager.register(testStage1);
        windowManager.register(testStage2);
        Platform.runLater(() -> windowManager.closeAll());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, windowManager.getWindows().size());
    }
}
