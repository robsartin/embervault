package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.embervault.ViewType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link ViewSwitchMenuHelper}.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ViewSwitchMenuHelperTest {

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @Test
    @DisplayName("creates separator followed by one item per ViewType")
    void createViewSwitchItems_correctCount() {
        List<MenuItem> items = ViewSwitchMenuHelper
                .createViewSwitchItems(ViewType.MAP, name -> { });

        // 1 separator + 5 view types = 6
        assertEquals(6, items.size());
        assertInstanceOf(SeparatorMenuItem.class, items.get(0));
    }

    @Test
    @DisplayName("current view type item is disabled")
    void createViewSwitchItems_currentTypeDisabled() {
        for (ViewType current : ViewType.values()) {
            List<MenuItem> items = ViewSwitchMenuHelper
                    .createViewSwitchItems(current, name -> { });

            for (int i = 1; i < items.size(); i++) {
                MenuItem item = items.get(i);
                ViewType type = ViewType.values()[i - 1];
                if (type == current) {
                    assertTrue(item.isDisable(),
                            type + " should be disabled when current");
                } else {
                    assertFalse(item.isDisable(),
                            type + " should be enabled when not current");
                }
            }
        }
    }

    @Test
    @DisplayName("menu item text includes view type display name")
    void createViewSwitchItems_itemTextMatchesDisplayName() {
        List<MenuItem> items = ViewSwitchMenuHelper
                .createViewSwitchItems(ViewType.OUTLINE, name -> { });

        for (int i = 1; i < items.size(); i++) {
            ViewType type = ViewType.values()[i - 1];
            assertEquals("Switch to " + type.displayName(),
                    items.get(i).getText());
        }
    }

    @Test
    @DisplayName("clicking a menu item invokes callback with "
            + "correct ViewType name")
    void createViewSwitchItems_callbackInvokedWithName() {
        AtomicReference<String> received = new AtomicReference<>();
        List<MenuItem> items = ViewSwitchMenuHelper
                .createViewSwitchItems(ViewType.MAP, received::set);

        // Fire the OUTLINE item (index 2: separator=0, MAP=1, OUTLINE=2)
        items.get(2).fire();

        assertEquals("OUTLINE", received.get());
    }

    @Test
    @DisplayName("null callback does not throw when item is fired")
    void createViewSwitchItems_nullCallback_noException() {
        List<MenuItem> items = ViewSwitchMenuHelper
                .createViewSwitchItems(ViewType.MAP, null);

        // Firing should not throw
        items.get(1).fire();
    }

    @Test
    @DisplayName("returned list is unmodifiable")
    void createViewSwitchItems_unmodifiable() {
        List<MenuItem> items = ViewSwitchMenuHelper
                .createViewSwitchItems(ViewType.MAP, name -> { });

        try {
            items.add(new MenuItem("extra"));
            // If we reach here, it is mutable — fail
            assertTrue(false,
                    "List should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }
}
