package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the Map view tab.
 *
 * <p>Computes a tab title of the form "Map: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes.</p>
 */
public final class MapViewModel {

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();

    /**
     * Constructs a MapViewModel that derives its tab title from the given note title property.
     */
    public MapViewModel(StringProperty noteTitle) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        tabTitle.bind(Bindings.concat("Map: ", noteTitle));
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }
}
