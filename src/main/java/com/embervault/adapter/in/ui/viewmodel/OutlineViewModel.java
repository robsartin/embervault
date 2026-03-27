package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the Outline view tab.
 *
 * <p>Computes a tab title of the form "Outline: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes.</p>
 */
public final class OutlineViewModel {

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();

    /**
     * Constructs an OutlineViewModel that derives its tab title from the given note title property.
     */
    public OutlineViewModel(StringProperty noteTitle) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        tabTitle.bind(Bindings.concat("Outline: ", noteTitle));
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }
}
