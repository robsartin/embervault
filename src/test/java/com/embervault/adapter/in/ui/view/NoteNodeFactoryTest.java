package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.NoteDisplayItem;
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

    @Nested
    @DisplayName("updateNoteNode")
    class UpdateNoteNodeTests {

        private NoteDisplayItem originalItem;
        private StackPane notePane;

        @BeforeEach
        void setUp() {
            originalItem = new NoteDisplayItem(
                    UUID.randomUUID(), "Original", "Content",
                    10, 20, 200, 150, "#AABBCC", false, "");
            ZoomTierRenderer renderer =
                    ZoomTier.NORMAL.createRenderer();
            notePane = renderer.render(originalItem, "#000000");
        }

        @Test
        @DisplayName("updates position to new coordinates")
        void updateNoteNode_shouldUpdatePosition() {
            NoteDisplayItem updated = new NoteDisplayItem(
                    originalItem.getId(), "Original", "Content",
                    50, 60, 200, 150, "#AABBCC", false, "");
            NoteNodeFactory.updateNoteNode(
                    notePane, updated, ZoomTier.NORMAL);
            assertEquals(50, notePane.getLayoutX());
            assertEquals(60, notePane.getLayoutY());
        }

        @Test
        @DisplayName("updates rectangle dimensions and fill")
        void updateNoteNode_shouldUpdateRectangle() {
            NoteDisplayItem updated = new NoteDisplayItem(
                    originalItem.getId(), "Original", "Content",
                    10, 20, 300, 250, "#FF0000", false, "");
            NoteNodeFactory.updateNoteNode(
                    notePane, updated, ZoomTier.NORMAL);
            Rectangle rect =
                    (Rectangle) notePane.getChildren().get(0);
            assertEquals(300, rect.getWidth());
            assertEquals(250, rect.getHeight());
            assertEquals(Color.web("#FF0000"), rect.getFill());
        }

        @Test
        @DisplayName("updates title label text")
        void updateNoteNode_shouldUpdateTitle() {
            NoteDisplayItem updated = new NoteDisplayItem(
                    originalItem.getId(), "New Title", "Content",
                    10, 20, 200, 150, "#AABBCC", false, "");
            NoteNodeFactory.updateNoteNode(
                    notePane, updated, ZoomTier.NORMAL);
            VBox textBox =
                    (VBox) notePane.getChildren().get(1);
            Label titleLabel =
                    (Label) textBox.getChildren().get(0);
            assertEquals("New Title", titleLabel.getText());
        }

        @Test
        @DisplayName("adds badge when tier shows badges")
        void updateNoteNode_shouldAddBadge() {
            NoteDisplayItem updated = new NoteDisplayItem(
                    originalItem.getId(), "Original", "Content",
                    10, 20, 200, 150, "#AABBCC", false, "\u2605");
            NoteNodeFactory.updateNoteNode(
                    notePane, updated, ZoomTier.NORMAL);
            assertTrue(notePane.getChildren().size() > 2,
                    "Expected badge label to be added");
            Label badge =
                    (Label) notePane.getChildren().get(2);
            assertEquals("\u2605", badge.getText());
        }

        @Test
        @DisplayName("updates existing badge text")
        void updateNoteNode_shouldUpdateExistingBadge() {
            // First add a badge
            NoteDisplayItem withBadge = new NoteDisplayItem(
                    originalItem.getId(), "Original", "Content",
                    10, 20, 200, 150, "#AABBCC", false, "\u2605");
            NoteNodeFactory.updateNoteNode(
                    notePane, withBadge, ZoomTier.NORMAL);
            // Now update the badge
            NoteDisplayItem newBadge = new NoteDisplayItem(
                    originalItem.getId(), "Original", "Content",
                    10, 20, 200, 150, "#AABBCC", false, "\u2713");
            NoteNodeFactory.updateNoteNode(
                    notePane, newBadge, ZoomTier.NORMAL);
            Label badge =
                    (Label) notePane.getChildren().get(2);
            assertEquals("\u2713", badge.getText());
        }
    }

    @Nested
    @DisplayName("createRenderedNotePane")
    class CreateRenderedNotePaneTests {

        @Test
        @DisplayName("delegates rendering to ZoomTierRenderer")
        void createRenderedNotePane_shouldDelegateToRenderer() {
            NoteDisplayItem item = new NoteDisplayItem(
                    UUID.randomUUID(), "Test", "Content",
                    10, 20, 200, 150, "#AABBCC", false, "");
            StackPane pane = NoteNodeFactory.createRenderedNotePane(
                    item, ZoomTier.NORMAL, "#000000");
            assertNotNull(pane);
            assertInstanceOf(Rectangle.class,
                    pane.getChildren().get(0));
        }

        @Test
        @DisplayName("sets layoutX and layoutY from item position")
        void createRenderedNotePane_shouldSetPosition() {
            NoteDisplayItem item = new NoteDisplayItem(
                    UUID.randomUUID(), "Test", "",
                    50, 75, 200, 150, "#AABBCC", false, "");
            StackPane pane = NoteNodeFactory.createRenderedNotePane(
                    item, ZoomTier.NORMAL, "#000000");
            assertEquals(50, pane.getLayoutX());
            assertEquals(75, pane.getLayoutY());
        }

        @Test
        @DisplayName("sets userData to item id")
        void createRenderedNotePane_shouldSetUserData() {
            UUID id = UUID.randomUUID();
            NoteDisplayItem item = new NoteDisplayItem(
                    id, "Test", "",
                    0, 0, 200, 150, "#AABBCC", false, "");
            StackPane pane = NoteNodeFactory.createRenderedNotePane(
                    item, ZoomTier.NORMAL, "#000000");
            assertEquals(id, pane.getUserData());
        }

        @Test
        @DisplayName("sets cursor to HAND")
        void createRenderedNotePane_shouldSetCursor() {
            NoteDisplayItem item = new NoteDisplayItem(
                    UUID.randomUUID(), "Test", "",
                    0, 0, 200, 150, "#AABBCC", false, "");
            StackPane pane = NoteNodeFactory.createRenderedNotePane(
                    item, ZoomTier.NORMAL, "#000000");
            assertEquals(Cursor.HAND, pane.getCursor());
        }

        @Test
        @DisplayName("OVERVIEW tier renders rectangle only")
        void createRenderedNotePane_overviewShouldBeMinimal() {
            NoteDisplayItem item = new NoteDisplayItem(
                    UUID.randomUUID(), "Test", "",
                    0, 0, 200, 150, "#AABBCC", false, "");
            StackPane pane = NoteNodeFactory.createRenderedNotePane(
                    item, ZoomTier.OVERVIEW, "#000000");
            assertEquals(1, pane.getChildren().size(),
                    "OVERVIEW should only have Rectangle");
        }
    }
}
