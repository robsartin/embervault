package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Reusable navigation history stack for ViewModels that support drill-down navigation.
 *
 * <p>Encapsulates a {@link Deque} of {@link UUID} entries representing previously
 * visited note ids, along with a {@code currentId} tracking the active note.
 * Exposes an observable {@code canNavigateBack} property that the view layer
 * can bind to for enabling/disabling back-navigation controls.</p>
 */
public final class NavigationStack {

    private final Deque<UUID> history = new ArrayDeque<>();
    private final BooleanProperty canNavigateBack =
            new SimpleBooleanProperty(false);
    private UUID currentId;

    /**
     * Returns the id currently at the top of the stack (the active note).
     *
     * @return the current id, or {@code null} if not set
     */
    public UUID getCurrentId() {
        return currentId;
    }

    /**
     * Sets the current id without affecting the navigation history.
     *
     * <p>Use this for initial setup; use {@link #push(UUID)} for drill-down.</p>
     *
     * @param id the current id
     */
    public void setCurrentId(UUID id) {
        this.currentId = id;
    }

    /**
     * Pushes the current id onto the history stack and sets a new current id.
     *
     * @param newId the new current id to navigate to
     */
    public void push(UUID newId) {
        history.push(currentId);
        currentId = newId;
        canNavigateBack.set(true);
    }

    /**
     * Pops the most recent id from the history and makes it the current id.
     *
     * @return the restored id, or {@code null} if the history was empty
     */
    public UUID pop() {
        if (history.isEmpty()) {
            return null;
        }
        UUID previous = history.pop();
        currentId = previous;
        canNavigateBack.set(!history.isEmpty());
        return previous;
    }

    /**
     * Returns whether the navigation history is empty (i.e., we are at the root level).
     *
     * @return {@code true} if the history is empty
     */
    public boolean isAtRoot() {
        return history.isEmpty();
    }

    /**
     * Returns the observable property indicating whether back-navigation is possible.
     *
     * @return the read-only boolean property
     */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return canNavigateBack;
    }
}
