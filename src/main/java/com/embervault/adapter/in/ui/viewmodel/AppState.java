package com.embervault.adapter.in.ui.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Centralized observable application state.
 *
 * <p>Replaces direct callback coupling between ViewModels by providing an
 * observable data-version counter. ViewModels call {@link #notifyDataChanged()}
 * after mutations, and consumers observe {@link #dataVersionProperty()} to
 * react to changes.</p>
 */
public class AppState {

    private final IntegerProperty dataVersion = new SimpleIntegerProperty(0);

    /** Increment the data version, notifying all observers. */
    public void notifyDataChanged() {
        dataVersion.set(dataVersion.get() + 1);
    }

    /** Observable property that changes when any data is modified. */
    public ReadOnlyIntegerProperty dataVersionProperty() {
        return dataVersion;
    }

    /** Returns the current data version. */
    public int getDataVersion() {
        return dataVersion.get();
    }
}
