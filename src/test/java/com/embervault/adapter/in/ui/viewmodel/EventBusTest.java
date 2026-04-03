package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
}
