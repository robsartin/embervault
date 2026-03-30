package com.embervault.adapter.in.ui.view;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Factory for creating Hyperbolic view JavaFX nodes.
 *
 * <p>Extracts node construction logic from {@link HyperbolicViewController}
 * so it can be tested without a full scene graph.</p>
 */
final class HyperbolicNodeFactory {

    static final double NORMAL_STROKE_WIDTH = 1.5;
    private static final double EDGE_OPACITY = 0.3;
    private static final double LABEL_FONT_SIZE = 12.0;

    private HyperbolicNodeFactory() { }

    static Line createEdgeLine(
            double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.gray(0.6, EDGE_OPACITY));
        line.setStrokeWidth(1.0);
        line.setMouseTransparent(true);
        return line;
    }

    static Circle createNodeCircle(
            double cx, double cy, double radius, String colorHex) {
        Circle circle = new Circle(cx, cy, radius);
        circle.setFill(Color.web(colorHex));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(NORMAL_STROKE_WIDTH);
        circle.setCursor(Cursor.HAND);
        return circle;
    }

    static Label createNodeLabel(
            String text, double cx, double cy, double radius) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD,
                LABEL_FONT_SIZE));
        label.setTextFill(Color.WHITE);
        label.setMouseTransparent(true);
        label.setLayoutX(cx - radius);
        label.setLayoutY(cy - LABEL_FONT_SIZE / 2);
        label.setMaxWidth(radius * 2);
        return label;
    }
}
