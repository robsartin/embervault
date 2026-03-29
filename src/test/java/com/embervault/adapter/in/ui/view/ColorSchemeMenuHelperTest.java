package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.embervault.domain.ColorScheme;
import com.embervault.domain.ColorSchemeRegistry;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests for {@link ColorSchemeMenuHelper}.
 *
 * <p>Verifies that the menu items match the schemes in the
 * {@link ColorSchemeRegistry} and that selection wiring works.</p>
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ColorSchemeMenuHelperTest {

    private static final List<String> SCHEME_NAMES =
            ColorSchemeRegistry.getAllSchemes().stream()
                    .map(ColorScheme::name).toList();

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @Test
    @DisplayName("menu contains one RadioMenuItem per registry scheme")
    void createColorSchemeMenu_matchesRegistryCount() {
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", name -> { });

        assertEquals(SCHEME_NAMES.size(), menu.getItems().size(),
                "Menu item count should match registry scheme count");
    }

    @Test
    @DisplayName("menu item names match registry scheme names in order")
    void createColorSchemeMenu_namesMatchRegistry() {
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", name -> { });

        for (int i = 0; i < SCHEME_NAMES.size(); i++) {
            assertEquals(SCHEME_NAMES.get(i),
                    menu.getItems().get(i).getText(),
                    "Item " + i + " name mismatch");
        }
    }

    @Test
    @DisplayName("all items are RadioMenuItems")
    void createColorSchemeMenu_allRadioItems() {
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", name -> { });

        for (var item : menu.getItems()) {
            assertInstanceOf(RadioMenuItem.class, item,
                    "All items should be RadioMenuItems");
        }
    }

    @Test
    @DisplayName("all items share the same ToggleGroup")
    void createColorSchemeMenu_sharedToggleGroup() {
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", name -> { });

        ToggleGroup group = ((RadioMenuItem) menu.getItems()
                .get(0)).getToggleGroup();
        assertNotNull(group, "First item should have a toggle group");
        for (var item : menu.getItems()) {
            assertEquals(group,
                    ((RadioMenuItem) item).getToggleGroup(),
                    "All items should share the same toggle group");
        }
    }

    @Test
    @DisplayName("Standard is selected by default")
    void createColorSchemeMenu_standardSelectedByDefault() {
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", name -> { });

        for (var item : menu.getItems()) {
            RadioMenuItem radio = (RadioMenuItem) item;
            if ("Standard".equals(radio.getText())) {
                assertTrue(radio.isSelected(),
                        "Standard should be selected by default");
            }
        }
    }

    @Test
    @DisplayName("selecting item invokes callback with correct name")
    void createColorSchemeMenu_callbackInvokedWithName() {
        AtomicReference<String> received = new AtomicReference<>();
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", received::set);

        // Fire the "Dark" item (index 1)
        menu.getItems().get(1).fire();

        assertNotNull(received.get(), "Callback should have been called");
        assertEquals("Dark", received.get());
    }

    @Test
    @DisplayName("menu title is 'Color Scheme'")
    void createColorSchemeMenu_title() {
        Menu menu = ColorSchemeMenuHelper.createColorSchemeMenu(
                SCHEME_NAMES, "Standard", name -> { });

        assertEquals("Color Scheme", menu.getText());
    }
}
