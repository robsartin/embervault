package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
    private final ObservableList<BreadcrumbEntry> breadcrumbs =
            FXCollections.observableArrayList();
    private final List<BreadcrumbEntry> breadcrumbPath =
            new ArrayList<>();
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
     * Sets the current id with a display name, updating the breadcrumb trail.
     *
     * @param id          the current id
     * @param displayName the human-readable label for the breadcrumb
     */
    public void setCurrentId(UUID id, String displayName) {
        this.currentId = id;
        breadcrumbPath.clear();
        breadcrumbPath.add(new BreadcrumbEntry(id, displayName));
        rebuildBreadcrumbs();
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
     * Pushes the current id onto the history stack and sets a new current id
     * with a display name, updating the breadcrumb trail.
     *
     * @param newId       the new current id to navigate to
     * @param displayName the human-readable label for the breadcrumb
     */
    public void push(UUID newId, String displayName) {
        push(newId);
        breadcrumbPath.add(new BreadcrumbEntry(newId, displayName));
        rebuildBreadcrumbs();
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
        if (!breadcrumbPath.isEmpty()) {
            breadcrumbPath.removeLast();
            rebuildBreadcrumbs();
        }
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

    /**
     * Navigates to a specific ancestor in the breadcrumb trail by index.
     *
     * <p>Index 0 is the root, and the last index is the current note.
     * Navigating to the current index is a no-op. Navigating to an earlier
     * index truncates the history and breadcrumb trail accordingly.</p>
     *
     * @param index the zero-based index in the breadcrumb trail
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public void navigateTo(int index) {
        if (index < 0 || index >= breadcrumbPath.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of range for breadcrumb size "
                    + breadcrumbPath.size());
        }
        if (index == breadcrumbPath.size() - 1) {
            return; // already at this position
        }
        // Truncate to keep entries 0..index
        int toRemove = breadcrumbPath.size() - 1 - index;
        for (int i = 0; i < toRemove; i++) {
            breadcrumbPath.removeLast();
            if (!history.isEmpty()) {
                history.pop();
            }
        }
        currentId = breadcrumbPath.get(index).noteId();
        canNavigateBack.set(!history.isEmpty());
        rebuildBreadcrumbs();
    }

    /**
     * Returns an observable list of breadcrumb entries representing the
     * full drill-down path from root to the current note.
     *
     * @return the unmodifiable observable breadcrumb list
     */
    public ObservableList<BreadcrumbEntry> getBreadcrumbs() {
        return FXCollections.unmodifiableObservableList(breadcrumbs);
    }

    private void rebuildBreadcrumbs() {
        breadcrumbs.setAll(breadcrumbPath);
    }
}
