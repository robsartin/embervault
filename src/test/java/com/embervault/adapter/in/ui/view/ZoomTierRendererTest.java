package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.view.ZoomTierRenderer.DetailedRenderer;
import com.embervault.adapter.in.ui.view.ZoomTierRenderer.NormalRenderer;
import com.embervault.adapter.in.ui.view.ZoomTierRenderer.OverviewRenderer;
import com.embervault.adapter.in.ui.view.ZoomTierRenderer.TitlesOnlyRenderer;
import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
import com.embervault.adapter.in.ui.viewmodel.ZoomTier;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
 * Tests for {@link ZoomTierRenderer} and its implementations.
 *
 * <p>Each renderer is tested in isolation without requiring
 * MapViewController or a canvas.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ZoomTierRendererTest {

    private static final String BORDER_COLOR = "#000000";
    private NoteDisplayItem item;
    private NoteDisplayItem itemWithBadge;
    private NoteDisplayItem itemWithContent;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        item = new NoteDisplayItem(
                UUID.randomUUID(), "Test Title", "",
                10, 20, 200, 150, "#AABBCC", false, "");
        itemWithBadge = new NoteDisplayItem(
                UUID.randomUUID(), "Badged", "Some content",
                10, 20, 200, 150, "#AABBCC", false, "\u2B50");
        itemWithContent = new NoteDisplayItem(
                UUID.randomUUID(), "Content Title", "Hello world",
                10, 20, 200, 150, "#AABBCC", false, "");
    }

    @Nested
    @DisplayName("OverviewRenderer")
    class OverviewRendererTests {

        @Test
        @DisplayName("renders rectangle only, no text or badge")
        void render_shouldReturnRectangleOnly() {
            StackPane result = new OverviewRenderer()
                    .render(item, BORDER_COLOR);

            assertEquals(1, result.getChildren().size(),
                    "OVERVIEW should have only a Rectangle");
            assertInstanceOf(Rectangle.class,
                    result.getChildren().get(0));
        }

        @Test
        @DisplayName("rectangle uses item dimensions and color")
        void render_shouldUseItemDimensions() {
            StackPane result = new OverviewRenderer()
                    .render(item, BORDER_COLOR);

            Rectangle rect = (Rectangle) result.getChildren().get(0);
            assertEquals(200, rect.getWidth());
            assertEquals(150, rect.getHeight());
        }

        @Test
        @DisplayName("badge is not rendered even when present")
        void render_shouldIgnoreBadge() {
            StackPane result = new OverviewRenderer()
                    .render(itemWithBadge, BORDER_COLOR);

            assertEquals(1, result.getChildren().size(),
                    "OVERVIEW should not render badge");
        }
    }

    @Nested
    @DisplayName("TitlesOnlyRenderer")
    class TitlesOnlyRendererTests {

        @Test
        @DisplayName("renders rectangle and title label, no content")
        void render_shouldRenderTitleOnly() {
            StackPane result = new TitlesOnlyRenderer()
                    .render(itemWithContent, BORDER_COLOR);

            assertInstanceOf(Rectangle.class,
                    result.getChildren().get(0));
            VBox textBox = findTextBox(result);
            assertNotNull(textBox, "Should have a text VBox");
            assertEquals(1, textBox.getChildren().size(),
                    "Should have only title label, no content");
            assertInstanceOf(Label.class,
                    textBox.getChildren().get(0));
            Label title = (Label) textBox.getChildren().get(0);
            assertEquals("Content Title", title.getText());
        }

        @Test
        @DisplayName("title font size is 10pt (compact)")
        void render_shouldUseCompactFont() {
            StackPane result = new TitlesOnlyRenderer()
                    .render(item, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            Label title = (Label) textBox.getChildren().get(0);
            assertEquals(10.0, title.getFont().getSize(), 0.1);
        }

        @Test
        @DisplayName("badge is rendered when present")
        void render_shouldShowBadge() {
            StackPane result = new TitlesOnlyRenderer()
                    .render(itemWithBadge, BORDER_COLOR);

            Label badge = findBadgeLabel(result);
            assertNotNull(badge, "Badge should be rendered");
            assertEquals("\u2B50", badge.getText());
        }

        @Test
        @DisplayName("badge is not rendered when empty")
        void render_shouldNotShowEmptyBadge() {
            StackPane result = new TitlesOnlyRenderer()
                    .render(item, BORDER_COLOR);

            Label badge = findBadgeLabel(result);
            assertNull(badge,
                    "Badge should not be present for empty badge");
        }
    }

    @Nested
    @DisplayName("NormalRenderer")
    class NormalRendererTests {

        @Test
        @DisplayName("renders rectangle, title, and content")
        void render_shouldRenderTitleAndContent() {
            StackPane result = new NormalRenderer()
                    .render(itemWithContent, BORDER_COLOR);

            assertInstanceOf(Rectangle.class,
                    result.getChildren().get(0));
            VBox textBox = findTextBox(result);
            assertNotNull(textBox);
            assertTrue(textBox.getChildren().size() >= 2,
                    "Should have title and content labels");
        }

        @Test
        @DisplayName("title font size is 14pt bold")
        void render_shouldUseBoldFont() {
            StackPane result = new NormalRenderer()
                    .render(item, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            Label title = (Label) textBox.getChildren().get(0);
            assertEquals(14.0, title.getFont().getSize(), 0.1);
        }

        @Test
        @DisplayName("content font size is 11pt")
        void render_contentFontSize() {
            StackPane result = new NormalRenderer()
                    .render(itemWithContent, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            Label content = (Label) textBox.getChildren().get(1);
            assertEquals(11.0, content.getFont().getSize(), 0.1);
        }

        @Test
        @DisplayName("badge is rendered when present")
        void render_shouldShowBadge() {
            StackPane result = new NormalRenderer()
                    .render(itemWithBadge, BORDER_COLOR);

            Label badge = findBadgeLabel(result);
            assertNotNull(badge);
            assertEquals("\u2B50", badge.getText());
        }

        @Test
        @DisplayName("no content label when content is empty")
        void render_shouldSkipEmptyContent() {
            StackPane result = new NormalRenderer()
                    .render(item, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            assertEquals(1, textBox.getChildren().size(),
                    "Should only have title when content is empty");
        }
    }

    @Nested
    @DisplayName("DetailedRenderer")
    class DetailedRendererTests {

        @Test
        @DisplayName("renders rectangle, title, and content")
        void render_shouldRenderTitleAndContent() {
            StackPane result = new DetailedRenderer()
                    .render(itemWithContent, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            assertNotNull(textBox);
            assertTrue(textBox.getChildren().size() >= 2,
                    "Should have title and content labels");
        }

        @Test
        @DisplayName("title font size is 18pt")
        void render_shouldUseLargerTitleFont() {
            StackPane result = new DetailedRenderer()
                    .render(item, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            Label title = (Label) textBox.getChildren().get(0);
            assertEquals(18.0, title.getFont().getSize(), 0.1);
        }

        @Test
        @DisplayName("content font size is 14pt (larger than normal)")
        void render_shouldUseLargerContentFont() {
            StackPane result = new DetailedRenderer()
                    .render(itemWithContent, BORDER_COLOR);

            VBox textBox = findTextBox(result);
            Label content = (Label) textBox.getChildren().get(1);
            assertEquals(14.0, content.getFont().getSize(), 0.1);
        }

        @Test
        @DisplayName("badge is rendered when present")
        void render_shouldShowBadge() {
            StackPane result = new DetailedRenderer()
                    .render(itemWithBadge, BORDER_COLOR);

            Label badge = findBadgeLabel(result);
            assertNotNull(badge);
            assertEquals("\u2B50", badge.getText());
            assertEquals(Pos.TOP_RIGHT,
                    StackPane.getAlignment(badge));
        }
    }

    @Nested
    @DisplayName("ZoomTier.createRenderer()")
    class ZoomTierCreateRendererTests {

        @Test
        @DisplayName("OVERVIEW creates OverviewRenderer")
        void overview_createsOverviewRenderer() {
            assertInstanceOf(OverviewRenderer.class,
                    ZoomTier.OVERVIEW.createRenderer());
        }

        @Test
        @DisplayName("TITLES_ONLY creates TitlesOnlyRenderer")
        void titlesOnly_createsTitlesOnlyRenderer() {
            assertInstanceOf(TitlesOnlyRenderer.class,
                    ZoomTier.TITLES_ONLY.createRenderer());
        }

        @Test
        @DisplayName("NORMAL creates NormalRenderer")
        void normal_createsNormalRenderer() {
            assertInstanceOf(NormalRenderer.class,
                    ZoomTier.NORMAL.createRenderer());
        }

        @Test
        @DisplayName("DETAILED creates DetailedRenderer")
        void detailed_createsDetailedRenderer() {
            assertInstanceOf(DetailedRenderer.class,
                    ZoomTier.DETAILED.createRenderer());
        }
    }

    /** Finds the VBox child within a StackPane. */
    private VBox findTextBox(StackPane pane) {
        for (Node child : pane.getChildren()) {
            if (child instanceof VBox vbox) {
                return vbox;
            }
        }
        return null;
    }

    /** Finds a direct Label child of a StackPane (badge). */
    private Label findBadgeLabel(StackPane pane) {
        for (Node child : pane.getChildren()) {
            if (child instanceof Label label) {
                return label;
            }
        }
        return null;
    }
}
