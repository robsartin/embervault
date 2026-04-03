package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for EventBus fire-and-forget event delivery.
 */
class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }

    @Test
    @DisplayName("publish delivers event to subscriber")
    void publish_deliversEventToSubscriber() {
        List<String> received = new ArrayList<>();
        eventBus.subscribe(String.class, received::add);

        eventBus.publish("hello");

        assertEquals(List.of("hello"), received);
    }

    @Test
    @DisplayName("publish delivers event to multiple subscribers")
    void publish_deliversToMultipleSubscribers() {
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        eventBus.subscribe(String.class, first::add);
        eventBus.subscribe(String.class, second::add);

        eventBus.publish("hi");

        assertEquals(List.of("hi"), first);
        assertEquals(List.of("hi"), second);
    }

    @Test
    @DisplayName("publish with no subscribers does not throw")
    void publish_withNoSubscribers_doesNotThrow() {
        eventBus.publish("orphan");
    }

    @Test
    @DisplayName("unsubscribe removes handler so it no longer receives events")
    void unsubscribe_removesHandler() {
        List<String> received = new ArrayList<>();
        Consumer<String> handler = received::add;
        eventBus.subscribe(String.class, handler);
        eventBus.unsubscribe(String.class, handler);

        eventBus.publish("after-unsub");

        assertTrue(received.isEmpty());
    }

    @Test
    @DisplayName("subscribers only receive events of their registered type")
    void subscribe_onlyReceivesMatchingType() {
        List<String> strings = new ArrayList<>();
        List<Integer> ints = new ArrayList<>();
        eventBus.subscribe(String.class, strings::add);
        eventBus.subscribe(Integer.class, ints::add);

        eventBus.publish("text");
        eventBus.publish(42);

        assertEquals(List.of("text"), strings);
        assertEquals(List.of(42), ints);
    }

    @Test
    @DisplayName("unsubscribe for non-existent type does not throw")
    void unsubscribe_nonExistentType_doesNotThrow() {
        eventBus.unsubscribe(String.class, s -> { });
    }
}
