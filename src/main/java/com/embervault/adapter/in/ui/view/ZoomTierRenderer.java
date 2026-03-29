package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.ViewColorConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Strategy interface for rendering a note at a specific zoom tier.
 *
 * <p>Each implementation produces a {@link StackPane} containing the
 * visual elements appropriate for that tier. The caller is responsible
 * for wiring event handlers on the returned pane.</p>
 */
public sealed interface ZoomTierRenderer
        permits ZoomTierRenderer.OverviewRenderer,
                ZoomTierRenderer.TitlesOnlyRenderer,
                ZoomTierRenderer.NormalRenderer,
                ZoomTierRenderer.DetailedRenderer {

    /** Normal border width for note rectangles. */
    double NORMAL_BORDER_WIDTH = 1.0;

    /** Badge font size. */
    double BADGE_FONT_SIZE = 16.0;

    /**
     * Renders a note display item as a StackPane.
     *
     * @param item        the note to render
     * @param borderColor the border color hex string
     * @return a StackPane containing the rendered note
     */
    StackPane render(NoteDisplayItem item, String borderColor);

    /** Zoom &lt; 0.4: colored rectangles only, no text. */
    record OverviewRenderer() implements ZoomTierRenderer {

        @Override
        public StackPane render(NoteDisplayItem item,
                String borderColor) {
            Rectangle rect = createRect(item, borderColor);
            StackPane pane = new StackPane(rect);
            pane.setAlignment(Pos.TOP_LEFT);
            return pane;
        }
    }

    /** 0.4 &le; zoom &lt; 0.8: title text only, compact font, badge. */
    record TitlesOnlyRenderer() implements ZoomTierRenderer {

        private static final int TITLE_FONT_SIZE = 10;

        @Override
        public StackPane render(NoteDisplayItem item,
                String borderColor) {
            Rectangle rect = createRect(item, borderColor);
            Label titleLabel = createTitleLabel(
                    item, TITLE_FONT_SIZE);
            VBox textBox = createTextBox(item, titleLabel);
            StackPane pane = new StackPane(rect, textBox);
            pane.setAlignment(Pos.TOP_LEFT);
            addBadgeIfPresent(pane, item);
            return pane;
        }
    }

    /** 0.8 &le; zoom &lt; 1.5: title (bold) + truncated content + badge. */
    record NormalRenderer() implements ZoomTierRenderer {

        private static final int TITLE_FONT_SIZE = 14;
        private static final double CONTENT_FONT_SIZE = 11.0;

        @Override
        public StackPane render(NoteDisplayItem item,
                String borderColor) {
            Rectangle rect = createRect(item, borderColor);
            Label titleLabel = createTitleLabel(
                    item, TITLE_FONT_SIZE);
            VBox textBox = createTextBox(item, titleLabel);
            addContentIfPresent(textBox, item, CONTENT_FONT_SIZE);
            StackPane pane = new StackPane(rect, textBox);
            pane.setAlignment(Pos.TOP_LEFT);
            addBadgeIfPresent(pane, item);
            return pane;
        }
    }

    /** Zoom &ge; 1.5: larger fonts, more content visible, badge. */
    record DetailedRenderer() implements ZoomTierRenderer {

        private static final int TITLE_FONT_SIZE = 18;
        private static final double CONTENT_FONT_SIZE = 14.0;

        @Override
        public StackPane render(NoteDisplayItem item,
                String borderColor) {
            Rectangle rect = createRect(item, borderColor);
            Label titleLabel = createTitleLabel(
                    item, TITLE_FONT_SIZE);
            VBox textBox = createTextBox(item, titleLabel);
            addContentIfPresent(textBox, item, CONTENT_FONT_SIZE);
            StackPane pane = new StackPane(rect, textBox);
            pane.setAlignment(Pos.TOP_LEFT);
            addBadgeIfPresent(pane, item);
            return pane;
        }
    }

    // ---- shared helper methods ----

    /** Creates the background rectangle for a note. */
    private static Rectangle createRect(NoteDisplayItem item,
            String borderColor) {
        Rectangle rect = new Rectangle(
                item.getWidth(), item.getHeight());
        rect.setFill(Color.web(item.getColorHex()));
        rect.setStroke(Color.web(borderColor));
        rect.setStrokeWidth(NORMAL_BORDER_WIDTH);
        rect.setArcWidth(4);
        rect.setArcHeight(4);
        return rect;
    }

    /** Creates a bold title label with contrast text color. */
    private static Label createTitleLabel(NoteDisplayItem item,
            double fontSize) {
        Label label = new Label(item.getTitle());
        label.setFont(Font.font(
                "System", FontWeight.BOLD, fontSize));
        label.setTextFill(Color.web(
                ViewColorConfig.contrastTextColor(
                        item.getColorHex())));
        label.setTextAlignment(TextAlignment.LEFT);
        label.setAlignment(Pos.TOP_LEFT);
        label.setMaxWidth(item.getWidth() - 8);
        label.setWrapText(true);
        label.setMouseTransparent(false);
        label.setPadding(new Insets(4, 4, 2, 4));
        return label;
    }

    /** Creates a clipped VBox containing the title label. */
    private static VBox createTextBox(NoteDisplayItem item,
            Label titleLabel) {
        VBox textBox = new VBox(titleLabel);
        textBox.setMaxWidth(item.getWidth());
        textBox.setMaxHeight(item.getHeight());
        textBox.setAlignment(Pos.TOP_LEFT);
        Rectangle clip = new Rectangle(
                item.getWidth(), item.getHeight());
        textBox.setClip(clip);
        return textBox;
    }

    /** Adds a content label to the text box if content is present. */
    private static void addContentIfPresent(VBox textBox,
            NoteDisplayItem item, double contentFontSize) {
        String content = item.getContent();
        if (content != null && !content.isEmpty()) {
            Label contentLabel = new Label(content);
            contentLabel.setFont(
                    Font.font("System", contentFontSize));
            contentLabel.setTextFill(Color.web(
                    ViewColorConfig.contrastTextColor(
                            item.getColorHex())));
            contentLabel.setTextAlignment(TextAlignment.LEFT);
            contentLabel.setAlignment(Pos.TOP_LEFT);
            contentLabel.setMaxWidth(item.getWidth() - 8);
            contentLabel.setMaxHeight(Double.MAX_VALUE);
            contentLabel.setWrapText(true);
            contentLabel.setMouseTransparent(true);
            contentLabel.setPadding(new Insets(0, 4, 4, 4));
            VBox.setVgrow(contentLabel, Priority.ALWAYS);
            textBox.getChildren().add(contentLabel);
        }
    }

    /** Adds a badge label to the pane if the item has a badge. */
    private static void addBadgeIfPresent(StackPane pane,
            NoteDisplayItem item) {
        String badge = item.getBadge();
        if (badge != null && !badge.isEmpty()) {
            Label label = new Label(badge);
            label.setFont(Font.font(BADGE_FONT_SIZE));
            label.setTextFill(Color.web(
                    ViewColorConfig.contrastTextColor(
                            item.getColorHex())));
            label.setEffect(
                    new DropShadow(2, Color.gray(0.3, 0.6)));
            label.setMouseTransparent(true);
            StackPane.setAlignment(label, Pos.TOP_RIGHT);
            StackPane.setMargin(label, new Insets(2, 4, 0, 0));
            pane.getChildren().add(label);
        }
    }
}
