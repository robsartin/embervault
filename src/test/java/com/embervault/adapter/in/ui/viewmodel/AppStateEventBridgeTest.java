package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AppStateEventBridge}.
 */
class AppStateEventBridgeTest {

    private EventBus eventBus;
    private AppState appState;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
        appState = new AppState();
        new AppStateEventBridge(eventBus, appState);
    }

    @Test
    @DisplayName("NoteCreatedEvent triggers notifyDataChanged")
    void noteCreatedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(new NoteCreatedEvent(UUID.randomUUID()));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("NoteUpdatedEvent triggers notifyDataChanged")
    void noteUpdatedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(new NoteUpdatedEvent(UUID.randomUUID()));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("NoteDeletedEvent triggers notifyDataChanged")
    void noteDeletedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(new NoteDeletedEvent(UUID.randomUUID()));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("NoteMovedEvent triggers notifyDataChanged")
    void noteMovedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(new NoteMovedEvent(UUID.randomUUID()));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("NoteRenamedEvent triggers notifyDataChanged")
    void noteRenamedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(
                new NoteRenamedEvent(UUID.randomUUID(), "new title"));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("LinkCreatedEvent triggers notifyDataChanged")
    void linkCreatedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(new LinkCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID()));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("LinkDeletedEvent triggers notifyDataChanged")
    void linkDeletedEvent_triggersNotifyDataChanged() {
        int before = appState.getDataVersion();

        eventBus.publish(new LinkDeletedEvent(
                UUID.randomUUID(), UUID.randomUUID()));

        assertEquals(before + 1, appState.getDataVersion());
    }

    @Test
    @DisplayName("multiple events increment version multiple times")
    void multipleEvents_incrementVersionMultipleTimes() {
        int before = appState.getDataVersion();

        eventBus.publish(new NoteCreatedEvent(UUID.randomUUID()));
        eventBus.publish(new NoteUpdatedEvent(UUID.randomUUID()));
        eventBus.publish(new NoteDeletedEvent(UUID.randomUUID()));

        assertEquals(before + 3, appState.getDataVersion());
    }
}
