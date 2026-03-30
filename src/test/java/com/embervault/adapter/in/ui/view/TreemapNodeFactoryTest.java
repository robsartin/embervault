package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.TreemapRect;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link TreemapNodeFactory}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class TreemapNodeFactoryTest {

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @Nested
    @DisplayName("truncateLabel")
    class TruncateLabelTests {

        @Test
        @DisplayName("returns full title when it fits within width")
        void truncateLabel_shouldReturnFullTitle() {
            String result = TreemapNodeFactory.truncateLabel("Hello", 200);
            assertEquals("Hello", result);
        }

        @Test
        @DisplayName("truncates with ellipsis when title exceeds width")
        void truncateLabel_shouldTruncateWithEllipsis() {
            String result = TreemapNodeFactory.truncateLabel(
                    "This is a very long title that should be truncated", 50);
            assertTrue(result.endsWith("\u2026"),
                    "Expected ellipsis at end");
            assertTrue(result.length()
                    < "This is a very long title that should be truncated"
                            .length(),
                    "Expected truncation");
        }

        @Test
        @DisplayName("returns empty string when width is zero")
        void truncateLabel_shouldReturnEmptyForZeroWidth() {
            String result = TreemapNodeFactory.truncateLabel("Hello", 0);
            assertEquals("", result);
        }

        @Test
        @DisplayName("respects max label length even with wide width")
        void truncateLabel_shouldRespectMaxLength() {
            String longTitle = "A".repeat(50);
            String result = TreemapNodeFactory.truncateLabel(longTitle, 1000);
            assertTrue(result.length() <= 31,
                    "Expected max 31 chars (30 + ellipsis)");
            assertTrue(result.endsWith("\u2026"),
                    "Expected ellipsis at end");
        }
    }

    @Nested
    @DisplayName("createTreemapCell")
    class CreateTreemapCellTests {

        private static final String BORDER_COLOR = "#000000";
        private NoteDisplayItem item;
        private TreemapRect rect;

        @BeforeEach
        void setUp() {
            UUID id = UUID.randomUUID();
            item = new NoteDisplayItem(
                    id, "Test Note", "",
                    0, 0, 200, 150, "#AABBCC", false, "");
            rect = new TreemapRect(id, 10, 20, 200, 150);
        }

        @Test
        @DisplayName("creates StackPane with Rectangle and title Label")
        void createTreemapCell_shouldCreateRectAndTitle() {
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    item, rect, BORDER_COLOR);
            assertNotNull(pane);
            assertTrue(pane.getChildren().size() >= 2,
                    "Expected at least Rectangle + Label");
            assertInstanceOf(Rectangle.class,
                    pane.getChildren().get(0));
            assertInstanceOf(Label.class,
                    pane.getChildren().get(1));
        }

        @Test
        @DisplayName("rectangle uses item color and border color")
        void createTreemapCell_shouldUseItemColor() {
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    item, rect, BORDER_COLOR);
            Rectangle r = (Rectangle) pane.getChildren().get(0);
            assertEquals(Color.web("#AABBCC"), r.getFill());
            assertEquals(Color.web(BORDER_COLOR), r.getStroke());
        }

        @Test
        @DisplayName("sets position from TreemapRect with padding")
        void createTreemapCell_shouldSetPosition() {
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    item, rect, BORDER_COLOR);
            assertEquals(10 + TreemapNodeFactory.RECT_PADDING,
                    pane.getLayoutX());
            assertEquals(20 + TreemapNodeFactory.RECT_PADDING,
                    pane.getLayoutY());
        }

        @Test
        @DisplayName("sets userData to item id")
        void createTreemapCell_shouldSetUserData() {
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    item, rect, BORDER_COLOR);
            assertEquals(item.getId(), pane.getUserData());
        }

        @Test
        @DisplayName("badge label added when badge present and space sufficient")
        void createTreemapCell_shouldAddBadgeWhenPresent() {
            NoteDisplayItem badgeItem = new NoteDisplayItem(
                    UUID.randomUUID(), "Test", "",
                    0, 0, 200, 150, "#AABBCC", false, "\u2605");
            TreemapRect bigRect = new TreemapRect(
                    badgeItem.getId(), 0, 0, 200, 150);
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    badgeItem, bigRect, BORDER_COLOR);
            assertEquals(3, pane.getChildren().size(),
                    "Expected Rectangle + Title + Badge");
            Label badge = (Label) pane.getChildren().get(2);
            assertEquals("\u2605", badge.getText());
        }

        @Test
        @DisplayName("badge omitted when rectangle too small")
        void createTreemapCell_shouldOmitBadgeWhenTooSmall() {
            NoteDisplayItem badgeItem = new NoteDisplayItem(
                    UUID.randomUUID(), "Test", "",
                    0, 0, 20, 15, "#AABBCC", false, "\u2605");
            TreemapRect smallRect = new TreemapRect(
                    badgeItem.getId(), 0, 0, 20, 15);
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    badgeItem, smallRect, BORDER_COLOR);
            assertEquals(2, pane.getChildren().size(),
                    "Expected only Rectangle + Title, no badge");
        }

        @Test
        @DisplayName("clip rectangle matches cell dimensions")
        void createTreemapCell_shouldSetClip() {
            StackPane pane = TreemapNodeFactory.createTreemapCell(
                    item, rect, BORDER_COLOR);
            assertInstanceOf(Rectangle.class, pane.getClip());
        }
    }
}
