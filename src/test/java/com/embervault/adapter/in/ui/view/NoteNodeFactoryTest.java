package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link NoteNodeFactory}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class NoteNodeFactoryTest {

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @Nested
    @DisplayName("createBadgeLabel")
    class CreateBadgeLabelTests {

        @Test
        @DisplayName("creates label with badge text")
        void createBadgeLabel_shouldSetText() {
            Label label = NoteNodeFactory.createBadgeLabel(
                    "\u2605", "#AABBCC");
            assertEquals("\u2605", label.getText());
        }

        @Test
        @DisplayName("label has drop shadow effect")
        void createBadgeLabel_shouldHaveDropShadow() {
            Label label = NoteNodeFactory.createBadgeLabel(
                    "\u2605", "#AABBCC");
            assertInstanceOf(DropShadow.class, label.getEffect());
        }

        @Test
        @DisplayName("label is mouse transparent")
        void createBadgeLabel_shouldBeMouseTransparent() {
            Label label = NoteNodeFactory.createBadgeLabel(
                    "\u2605", "#AABBCC");
            assertTrue(label.isMouseTransparent());
        }

        @Test
        @DisplayName("label is aligned TOP_RIGHT with margin")
        void createBadgeLabel_shouldAlignTopRight() {
            Label label = NoteNodeFactory.createBadgeLabel(
                    "\u2605", "#AABBCC");
            assertEquals(Pos.TOP_RIGHT,
                    StackPane.getAlignment(label));
            Insets margin = StackPane.getMargin(label);
            assertEquals(2, margin.getTop());
            assertEquals(4, margin.getRight());
        }
    }
}
