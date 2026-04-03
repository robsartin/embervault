package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for typed event records used with EventBus.
 */
class EventBusTypedEventsTest {

  private EventBus eventBus;

  @BeforeEach
  void setUp() {
    eventBus = new EventBus();
  }

  @Test
  @DisplayName("NoteDeletedEvent carries note ID through EventBus")
  void noteDeletedEvent_carriesNoteId() {
    UUID noteId = UUID.randomUUID();
    List<UUID> deletedIds = new ArrayList<>();
    eventBus.subscribe(NoteDeletedEvent.class,
        e -> deletedIds.add(e.noteId()));

    eventBus.publish(new NoteDeletedEvent(noteId));

    assertEquals(List.of(noteId), deletedIds);
  }

  @Test
  @DisplayName("ViewRefreshedEvent carries view type name through EventBus")
  void viewRefreshedEvent_carriesViewType() {
    List<String> refreshedViews = new ArrayList<>();
    eventBus.subscribe(ViewRefreshedEvent.class,
        e -> refreshedViews.add(e.viewTypeName()));

    eventBus.publish(new ViewRefreshedEvent("Map"));

    assertEquals(List.of("Map"), refreshedViews);
  }

  @Test
  @DisplayName("different event types do not interfere")
  void differentEventTypes_doNotInterfere() {
    List<NoteDeletedEvent> deleted = new ArrayList<>();
    List<ViewRefreshedEvent> refreshed = new ArrayList<>();
    eventBus.subscribe(NoteDeletedEvent.class, deleted::add);
    eventBus.subscribe(ViewRefreshedEvent.class, refreshed::add);

    UUID noteId = UUID.randomUUID();
    eventBus.publish(new NoteDeletedEvent(noteId));

    assertEquals(1, deleted.size());
    assertTrue(refreshed.isEmpty());
  }
}
