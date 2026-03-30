package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Factory for creating Map view JavaFX nodes.
 *
 * <p>Extracts node construction logic from {@link MapViewController}
 * so it can be tested without a full scene graph.</p>
 */
final class NoteNodeFactory {

    private static final double BADGE_FONT_SIZE = 16.0;

    private NoteNodeFactory() { }

    static Label createBadgeLabel(String badge, String colorHex) {
        Label l = new Label(badge);
        l.setFont(Font.font(BADGE_FONT_SIZE));
        l.setTextFill(Color.web(
                ViewColorConfig.contrastTextColor(colorHex)));
        l.setEffect(new DropShadow(2, Color.gray(0.3, 0.6)));
        l.setMouseTransparent(true);
        StackPane.setAlignment(l, Pos.TOP_RIGHT);
        StackPane.setMargin(l, new Insets(2, 4, 0, 0));
        return l;
    }
}
