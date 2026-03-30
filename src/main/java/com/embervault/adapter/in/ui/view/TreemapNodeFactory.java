package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.TreemapRect;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Factory for creating Treemap view JavaFX nodes.
 *
 * <p>Extracts node construction logic from {@link TreemapViewController}
 * so it can be tested without a full scene graph.</p>
 */
final class TreemapNodeFactory {

    static final double TITLE_FONT_SIZE = 13.0;
    static final double RECT_PADDING = 2.0;
    static final double NORMAL_BORDER_WIDTH = 1.0;
    private static final int MAX_LABEL_LENGTH = 30;
    private static final double MIN_BADGE_WIDTH = 30;
    private static final double MIN_BADGE_HEIGHT = 20;

    private TreemapNodeFactory() { }

    static StackPane createTreemapCell(
            NoteDisplayItem item, TreemapRect tr, String borderColor) {
        double rectWidth = Math.max(0, tr.width() - RECT_PADDING * 2);
        double rectHeight = Math.max(0, tr.height() - RECT_PADDING * 2);

        Rectangle rect = new Rectangle(rectWidth, rectHeight);
        rect.setFill(Color.web(item.getColorHex()));
        rect.setStroke(Color.web(borderColor));
        rect.setStrokeWidth(NORMAL_BORDER_WIDTH);
        rect.setArcWidth(4);
        rect.setArcHeight(4);

        String labelText = truncateLabel(item.getTitle(), rectWidth);
        Label titleLabel = new Label(labelText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD,
                TITLE_FONT_SIZE));
        titleLabel.setTextFill(Color.web(
                ViewColorConfig.contrastTextColor(item.getColorHex())));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(rectWidth - 4);
        titleLabel.setMaxHeight(rectHeight - 4);
        titleLabel.setWrapText(true);
        titleLabel.setPadding(new Insets(2));
        titleLabel.setMouseTransparent(true);

        Rectangle clip = new Rectangle(rectWidth, rectHeight);
        StackPane notePane = new StackPane(rect, titleLabel);

        String badge = item.getBadge();
        if (badge != null && !badge.isEmpty()
                && rectWidth > MIN_BADGE_WIDTH
                && rectHeight > MIN_BADGE_HEIGHT) {
            Label badgeLabel = new Label(badge);
            badgeLabel.setFont(Font.font("System", TITLE_FONT_SIZE));
            badgeLabel.setMouseTransparent(true);
            badgeLabel.setPadding(new Insets(2, 4, 0, 0));
            StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
            notePane.getChildren().add(badgeLabel);
        }

        notePane.setClip(clip);
        notePane.setUserData(item.getId());
        notePane.setAlignment(Pos.CENTER);
        notePane.setLayoutX(tr.x() + RECT_PADDING);
        notePane.setLayoutY(tr.y() + RECT_PADDING);

        return notePane;
    }

    static String truncateLabel(String title, double availableWidth) {
        double charsPerWidth = availableWidth / (TITLE_FONT_SIZE * 0.6);
        int maxChars = Math.min(MAX_LABEL_LENGTH, (int) charsPerWidth);
        if (maxChars <= 0) {
            return "";
        }
        if (title.length() <= maxChars) {
            return title;
        }
        return title.substring(0, maxChars) + "\u2026";
    }
}
