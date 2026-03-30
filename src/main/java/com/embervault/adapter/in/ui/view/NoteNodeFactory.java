package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import com.embervault.adapter.in.ui.viewmodel.ZoomTier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    static void updateNoteNode(
            StackPane notePane, NoteDisplayItem item, ZoomTier tier) {
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        if (notePane.getChildren().get(0) instanceof Rectangle rect) {
            rect.setWidth(item.getWidth());
            rect.setHeight(item.getHeight());
            rect.setFill(Color.web(item.getColorHex()));
        }
        if (notePane.getChildren().size() > 1
                && notePane.getChildren().get(1)
                        instanceof VBox textBox) {
            textBox.setMaxWidth(item.getWidth());
            textBox.setMaxHeight(item.getHeight());
            if (textBox.getClip() instanceof Rectangle clip) {
                clip.setWidth(item.getWidth());
                clip.setHeight(item.getHeight());
            }
            if (!textBox.getChildren().isEmpty()
                    && textBox.getChildren().get(0)
                            instanceof Label titleLabel) {
                titleLabel.setText(item.getTitle());
                titleLabel.setMaxWidth(item.getWidth() - 8);
            }
            if (textBox.getChildren().size() > 1
                    && textBox.getChildren().get(1)
                            instanceof Label contentLabel) {
                contentLabel.setText(
                        item.getContent() != null
                                ? item.getContent() : "");
                contentLabel.setMaxWidth(item.getWidth() - 8);
            }
        }
        String badge = item.getBadge();
        if (notePane.getChildren().size() > 2
                && notePane.getChildren().get(2)
                        instanceof Label badgeLabel) {
            badgeLabel.setText(badge != null ? badge : "");
        } else if (tier.isShowBadge()
                && badge != null && !badge.isEmpty()) {
            notePane.getChildren().add(
                    createBadgeLabel(badge, item.getColorHex()));
        }
    }

    static StackPane createRenderedNotePane(
            NoteDisplayItem item, ZoomTier tier,
            String borderColor) {
        ZoomTierRenderer renderer = tier.createRenderer();
        StackPane notePane = renderer.render(item, borderColor);
        notePane.setUserData(item.getId());
        notePane.setLayoutX(item.getXpos());
        notePane.setLayoutY(item.getYpos());
        notePane.setCursor(Cursor.HAND);
        return notePane;
    }
}
