package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.stage.Stage;
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
            assertTrue(result.endsWith("\u2026"));
            assertTrue(result.length() < "This is a very long title that should be truncated".length());
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
            // MAX_LABEL_LENGTH is 30, so should truncate
            assertTrue(result.length() <= 31); // 30 chars + ellipsis
            assertTrue(result.endsWith("\u2026"));
        }

        private void assertTrue(boolean condition) {
            org.junit.jupiter.api.Assertions.assertTrue(condition);
        }
    }
}
