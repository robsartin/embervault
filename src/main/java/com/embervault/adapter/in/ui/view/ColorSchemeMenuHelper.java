package com.embervault.adapter.in.ui.view;

import java.util.List;
import java.util.function.Consumer;

import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

/**
 * Shared helper that builds a "Color Scheme" submenu with a
 * {@link RadioMenuItem} for each scheme name provided.
 * The item matching {@code defaultName} is selected by default.
 */
public final class ColorSchemeMenuHelper {

    private ColorSchemeMenuHelper() {
        // utility class
    }

    /**
     * Creates a "Color Scheme" {@link Menu} populated with one
     * {@link RadioMenuItem} per scheme name.
     *
     * <p>All items share a single {@link ToggleGroup} so that
     * exactly one scheme is active at a time. The item whose
     * text equals {@code defaultName} is selected by default.</p>
     *
     * @param schemeNames      ordered list of scheme names
     * @param defaultName      the name of the initially-selected scheme
     * @param onSchemeSelected callback invoked with the selected
     *                         scheme name when the user picks one
     * @return the fully-wired Color Scheme submenu
     */
    public static Menu createColorSchemeMenu(
            List<String> schemeNames,
            String defaultName,
            Consumer<String> onSchemeSelected) {
        Menu menu = new Menu("Color Scheme");
        ToggleGroup toggleGroup = new ToggleGroup();

        for (String name : schemeNames) {
            RadioMenuItem item = new RadioMenuItem(name);
            item.setToggleGroup(toggleGroup);
            if (name.equals(defaultName)) {
                item.setSelected(true);
            }
            item.setOnAction(e ->
                    onSchemeSelected.accept(name));
            menu.getItems().add(item);
        }

        return menu;
    }
}
