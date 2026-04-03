package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the command palette overlay.
 *
 * <p>Exposes a query property that filters registered shortcuts
 * from the {@link ShortcutRegistry}, and a visibility flag for
 * toggling the palette open/closed.</p>
 */
public class CommandPaletteViewModel {

    private final ShortcutRegistry registry;
    private final BooleanProperty visible =
            new SimpleBooleanProperty(false);
    private final StringProperty query =
            new SimpleStringProperty("");
    private final ObservableList<ShortcutAction> filteredActions =
            FXCollections.observableArrayList();

    public CommandPaletteViewModel(ShortcutRegistry registry) {
        this.registry = registry;
        query.addListener((obs, oldVal, newVal) -> updateFilter());
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public StringProperty queryProperty() {
        return query;
    }

    public ObservableList<ShortcutAction> getFilteredActions() {
        return filteredActions;
    }

    /**
     * Shows the command palette with all shortcuts visible.
     */
    public void show() {
        visible.set(true);
        updateFilter();
    }

    /**
     * Hides the command palette and resets the query.
     */
    public void hide() {
        visible.set(false);
        query.set("");
    }

    /**
     * Executes the given action and hides the palette.
     *
     * @param action the shortcut action to execute
     */
    public void executeSelected(ShortcutAction action) {
        action.action().run();
        hide();
    }

    private void updateFilter() {
        List<ShortcutAction> results = registry.search(query.get());
        filteredActions.setAll(results);
    }
}
