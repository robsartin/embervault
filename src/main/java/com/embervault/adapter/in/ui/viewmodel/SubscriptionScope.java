package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Tracks a group of {@link EventBus} subscriptions so they can all be
 * unsubscribed at once via {@link #close()}.
 *
 * <p>Typical usage: a ViewModel creates a scope on initialization and calls
 * {@code close()} when the ViewModel is disposed, preventing handler leaks.</p>
 */
public class SubscriptionScope {

    private final EventBus eventBus;
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    SubscriptionScope(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Subscribes a handler through this scope. The subscription is tracked
     * and will be unsubscribed when {@link #close()} is called.
     *
     * @param eventType the class of the event to subscribe to
     * @param handler   the handler to invoke when an event is published
     * @param <T>       the event type
     * @return the individual {@link Subscription}
     */
    public <T> Subscription subscribe(Class<T> eventType,
            Consumer<T> handler) {
        Subscription subscription = eventBus.subscribe(eventType, handler);
        subscriptions.add(subscription);
        return subscription;
    }

    /**
     * Unsubscribes all handlers registered through this scope.
     */
    public void close() {
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }
}
