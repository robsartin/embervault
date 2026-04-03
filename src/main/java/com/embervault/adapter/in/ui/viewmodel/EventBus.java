package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Lightweight fire-and-forget event bus for cross-ViewModel communication.
 *
 * <p>Complements {@link AppState} (observable state) by providing a mechanism
 * for one-shot event delivery. Subscribers register by event type and receive
 * events synchronously when published.</p>
 */
public class EventBus {

  private final Map<Class<?>, List<Consumer<?>>> subscribers =
      new ConcurrentHashMap<>();

  /**
   * Publishes an event to all subscribers of its type.
   *
   * @param event the event to publish
   * @param <T>   the event type
   */
  @SuppressWarnings("unchecked")
  public <T> void publish(T event) {
    List<Consumer<?>> handlers = subscribers.get(event.getClass());
    if (handlers != null) {
      for (Consumer<?> handler : handlers) {
        ((Consumer<T>) handler).accept(event);
      }
    }
  }

  /**
   * Subscribes a handler for the given event type.
   *
   * @param eventType the class of the event to subscribe to
   * @param handler   the handler to invoke when an event of this type is published
   * @param <T>       the event type
   */
  public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
    subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
        .add(handler);
  }

  /**
   * Unsubscribes a previously registered handler for the given event type.
   *
   * <p>If the handler was not subscribed, this method does nothing.</p>
   *
   * @param eventType the class of the event to unsubscribe from
   * @param handler   the handler to remove
   * @param <T>       the event type
   */
  public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
    List<Consumer<?>> handlers = subscribers.get(eventType);
    if (handlers != null) {
      handlers.remove(handler);
    }
  }
}
