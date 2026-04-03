package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;

/**
 * Bridges EventBus events to {@link AppState#notifyDataChanged()}.
 *
 * <p>Subscribes to all domain events on the EventBus and calls
 * {@code appState.notifyDataChanged()} for each, preserving the existing
 * cross-window refresh mechanism driven by the data version counter.</p>
 */
public class AppStateEventBridge {

    /**
     * Creates the bridge, subscribing to all relevant events.
     *
     * @param eventBus the event bus to subscribe to
     * @param appState the app state to notify
     */
    public AppStateEventBridge(EventBus eventBus, AppState appState) {
        Objects.requireNonNull(eventBus, "eventBus must not be null");
        Objects.requireNonNull(appState, "appState must not be null");

        eventBus.subscribe(NoteCreatedEvent.class,
                e -> appState.notifyDataChanged());
        eventBus.subscribe(NoteUpdatedEvent.class,
                e -> appState.notifyDataChanged());
        eventBus.subscribe(NoteDeletedEvent.class,
                e -> appState.notifyDataChanged());
        eventBus.subscribe(NoteMovedEvent.class,
                e -> appState.notifyDataChanged());
        eventBus.subscribe(NoteRenamedEvent.class,
                e -> appState.notifyDataChanged());
        eventBus.subscribe(LinkCreatedEvent.class,
                e -> appState.notifyDataChanged());
        eventBus.subscribe(LinkDeletedEvent.class,
                e -> appState.notifyDataChanged());
    }
}
