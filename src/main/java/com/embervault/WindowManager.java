package com.embervault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.stage.Stage;

/**
 * Tracks all open application windows and coordinates cross-window refresh.
 *
 * <p>Each window registers itself on creation and unregisters on close.
 * When any window mutates shared data, {@link #notifyAllWindows()} triggers
 * refresh callbacks in every open window.</p>
 */
public final class WindowManager {

    private final Set<Stage> windows = new LinkedHashSet<>();
    private final List<Runnable> refreshListeners = new ArrayList<>();

    /**
     * Registers a window for tracking.
     *
     * @param stage the window to track
     */
    public void register(Stage stage) {
        windows.add(stage);
    }

    /**
     * Unregisters a window from tracking.
     *
     * @param stage the window to remove
     */
    public void unregister(Stage stage) {
        windows.remove(stage);
    }

    /**
     * Returns an unmodifiable view of all tracked windows.
     */
    public Set<Stage> getWindows() {
        return Collections.unmodifiableSet(windows);
    }

    /**
     * Returns true if the given stage is the last remaining window.
     */
    public boolean isLastWindow(Stage stage) {
        return windows.size() == 1 && windows.contains(stage);
    }

    /**
     * Adds a refresh listener called by {@link #notifyAllWindows()}.
     */
    public void addRefreshListener(Runnable listener) {
        refreshListeners.add(listener);
    }

    /**
     * Removes a previously added refresh listener.
     */
    public void removeRefreshListener(Runnable listener) {
        refreshListeners.remove(listener);
    }

    /**
     * Notifies all refresh listeners that shared data has changed.
     */
    public void notifyAllWindows() {
        for (Runnable listener : List.copyOf(refreshListeners)) {
            listener.run();
        }
    }

    /**
     * Closes all tracked windows and clears the registry.
     */
    public void closeAll() {
        for (Stage stage : List.copyOf(windows)) {
            stage.close();
        }
        windows.clear();
    }
}
