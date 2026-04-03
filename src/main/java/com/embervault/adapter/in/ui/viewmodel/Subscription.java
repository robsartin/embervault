package com.embervault.adapter.in.ui.viewmodel;

/**
 * Represents a subscription to an {@link EventBus} event type.
 *
 * <p>Calling {@link #unsubscribe()} removes the handler so it no longer
 * receives events. This is a functional interface, typically returned by
 * {@link EventBus#subscribe}.</p>
 */
@FunctionalInterface
public interface Subscription {

    /**
     * Removes the associated handler from the EventBus.
     */
    void unsubscribe();
}
