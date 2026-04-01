package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Factory for creating views within a {@link ViewPaneContext}.
 *
 * <p>Each implementation knows how to create the ViewModel, configure
 * it, and produce a {@link ViewCreationResult} containing the pieces
 * that the pane context needs for wiring.</p>
 */
public interface ViewFactory {

    /**
     * Creates a view with its ViewModel and wiring callbacks.
     *
     * @param deps          the shared dependencies
     * @param baseNoteId    the base note id for the pane
     * @param onViewSwitch  callback to trigger a view switch
     * @return the creation result
     */
    ViewCreationResult create(
            ViewPaneDeps deps,
            UUID baseNoteId,
            Consumer<String> onViewSwitch);
}
