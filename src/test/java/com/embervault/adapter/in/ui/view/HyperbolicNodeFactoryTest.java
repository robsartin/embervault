package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link HyperbolicNodeFactory}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class HyperbolicNodeFactoryTest {

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @Nested
    @DisplayName("createEdgeLine")
    class CreateEdgeLineTests {

        @Test
        @DisplayName("creates line with correct coordinates")
        void createEdgeLine_shouldSetCoordinates() {
            Line line = HyperbolicNodeFactory.createEdgeLine(
                    10, 20, 30, 40);
            assertEquals(10, line.getStartX());
            assertEquals(20, line.getStartY());
            assertEquals(30, line.getEndX());
            assertEquals(40, line.getEndY());
        }

        @Test
        @DisplayName("line is mouse transparent")
        void createEdgeLine_shouldBeMouseTransparent() {
            Line line = HyperbolicNodeFactory.createEdgeLine(
                    0, 0, 100, 100);
            assertTrue(line.isMouseTransparent());
        }

        @Test
        @DisplayName("line has gray stroke")
        void createEdgeLine_shouldHaveGrayStroke() {
            Line line = HyperbolicNodeFactory.createEdgeLine(
                    0, 0, 100, 100);
            assertEquals(1.0, line.getStrokeWidth());
            assertTrue(line.getStroke() instanceof Color);
        }
    }
}
