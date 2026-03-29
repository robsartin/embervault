package com.embervault.adapter.in.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.embervault.ViewType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Shared helper that builds context-menu items for switching between
 * view types. Each of the five view controllers delegates to this
 * helper so that the menu structure is consistent and not duplicated.
 */
final class ViewSwitchMenuHelper {

    private ViewSwitchMenuHelper() {
        // utility class
    }

    /**
     * Creates a list of menu items for switching views: a separator
     * followed by one item per {@link ViewType}. The item
     * corresponding to {@code currentType} is disabled.
     *
     * @param currentType   the currently active view type
     * @param onViewSwitch  callback invoked with the target
     *                      view-type name when a menu item is selected
     * @return an unmodifiable list starting with a separator
     */
    static List<MenuItem> createViewSwitchItems(
            ViewType currentType,
            Consumer<String> onViewSwitch) {
        List<MenuItem> items = new ArrayList<>();
        items.add(new SeparatorMenuItem());
        for (ViewType type : ViewType.values()) {
            MenuItem item = new MenuItem(
                    "Switch to " + type.displayName());
            item.setDisable(type == currentType);
            if (onViewSwitch != null) {
                item.setOnAction(e ->
                        onViewSwitch.accept(type.name()));
            }
            items.add(item);
        }
        return List.copyOf(items);
    }
}
