package com.embervault.adapter.in.ui.view;

import java.util.UUID;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Shared helpers for TestFX-based UI tests.
 *
 * <p>Consolidates utility methods that were previously duplicated across
 * {@code MapViewControllerTest}, {@code BadgeRenderingTest}, and
 * {@code TreemapViewControllerTest}.</p>
 */
public final class TestFxHelper {

    private TestFxHelper() {
        // utility class
    }

    /**
     * Waits for pending JavaFX events to complete.
     *
     * <p>Wraps {@link WaitForAsyncUtils#waitForFxEvents()} so that callers
     * do not need to import the TestFX utility class directly.</p>
     */
    public static void waitForFx() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Finds a {@link StackPane} child of the given canvas whose
     * {@code userData} matches the given UUID.
     *
     * @param canvas the parent pane to search
     * @param id     the UUID stored as user data
     * @return the matching StackPane, or {@code null} if not found
     */
    public static StackPane findNodeByUserData(Pane canvas, UUID id) {
        for (Node child : canvas.getChildren()) {
            if (child instanceof StackPane sp
                    && id.equals(sp.getUserData())) {
                return sp;
            }
        }
        return null;
    }

    /**
     * Finds the badge {@link Label} inside a note's {@link StackPane}.
     *
     * <p>The badge label is identified as a Label that is aligned to
     * {@link Pos#TOP_RIGHT} (the convention used by both the Map and
     * Treemap renderers).</p>
     *
     * @param noteNode the StackPane representing a note
     * @return the badge label, or {@code null} if none found
     */
    public static Label findBadgeLabel(StackPane noteNode) {
        for (Node child : noteNode.getChildren()) {
            if (child instanceof Label label
                    && StackPane.getAlignment(label) == Pos.TOP_RIGHT) {
                return label;
            }
        }
        return null;
    }
}
