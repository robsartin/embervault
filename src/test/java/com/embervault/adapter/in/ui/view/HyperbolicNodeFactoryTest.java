package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

    @Nested
    @DisplayName("createNodeCircle")
    class CreateNodeCircleTests {

        @Test
        @DisplayName("creates circle at specified position with radius")
        void createNodeCircle_shouldSetPositionAndRadius() {
            Circle circle = HyperbolicNodeFactory.createNodeCircle(
                    100, 200, 15, "#4A90D9");
            assertEquals(100, circle.getCenterX());
            assertEquals(200, circle.getCenterY());
            assertEquals(15, circle.getRadius());
        }

        @Test
        @DisplayName("circle fill uses provided color hex")
        void createNodeCircle_shouldUseFillColor() {
            Circle circle = HyperbolicNodeFactory.createNodeCircle(
                    0, 0, 10, "#FF0000");
            assertEquals(Color.web("#FF0000"), circle.getFill());
        }

        @Test
        @DisplayName("circle has white stroke")
        void createNodeCircle_shouldHaveWhiteStroke() {
            Circle circle = HyperbolicNodeFactory.createNodeCircle(
                    0, 0, 10, "#4A90D9");
            assertEquals(Color.WHITE, circle.getStroke());
            assertEquals(HyperbolicNodeFactory.NORMAL_STROKE_WIDTH,
                    circle.getStrokeWidth());
        }

        @Test
        @DisplayName("circle cursor is HAND")
        void createNodeCircle_shouldHaveHandCursor() {
            Circle circle = HyperbolicNodeFactory.createNodeCircle(
                    0, 0, 10, "#4A90D9");
            assertEquals(Cursor.HAND, circle.getCursor());
        }
    }
}
