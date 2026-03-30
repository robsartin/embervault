package com.embervault.adapter.in.ui.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Factory for creating Hyperbolic view JavaFX nodes.
 *
 * <p>Extracts node construction logic from {@link HyperbolicViewController}
 * so it can be tested without a full scene graph.</p>
 */
final class HyperbolicNodeFactory {

    static final double NORMAL_STROKE_WIDTH = 1.5;
    private static final double EDGE_OPACITY = 0.3;

    private HyperbolicNodeFactory() { }

    static Line createEdgeLine(
            double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.gray(0.6, EDGE_OPACITY));
        line.setStrokeWidth(1.0);
        line.setMouseTransparent(true);
        return line;
    }
}
