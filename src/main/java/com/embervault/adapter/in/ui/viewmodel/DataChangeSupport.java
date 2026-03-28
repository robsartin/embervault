package com.embervault.adapter.in.ui.viewmodel;

/**
 * Reusable helper that manages a single {@code Runnable} data-change callback.
 *
 * <p>ViewModels compose this class to avoid duplicating the
 * {@code setOnDataChanged / notifyDataChanged} boilerplate.</p>
 */
public final class DataChangeSupport {

    private Runnable onDataChanged;

    /**
     * Registers (or clears) the callback invoked by {@link #notifyDataChanged()}.
     *
     * @param callback the callback to invoke, or {@code null} to clear
     */
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    /**
     * Fires the registered callback, if any.
     */
    public void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
