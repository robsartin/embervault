package com.embervault.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Smoke test verifying that TestFX can launch a JavaFX stage and interact with it.
 *
 * <p>Tagged with "ui" so headless CI environments can exclude these tests
 * via {@code mvn test -Pskip-ui-tests}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class JavaFxSmokeTest {

    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("EmberVault TestFX Smoke Test");
        stage.show();
    }

    @Test
    void stageShouldBeShowing(FxRobot robot) {
        assertNotNull(stage, "Stage should have been initialized by @Start");
        assertTrue(stage.isShowing(), "Stage should be visible after show()");
    }
}
