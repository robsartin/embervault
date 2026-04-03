package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that HyperbolicViewModel publishes typed events via EventBus.
 */
class HyperbolicViewModelEventBusTest {

    private HyperbolicViewModel viewModel;
    private NoteService noteService;
    private LinkService linkService;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        var noteRepo = new InMemoryNoteRepository();
        var linkRepo = new InMemoryLinkRepository();
        noteService = new NoteServiceImpl(noteRepo);
        linkService = new LinkServiceImpl(linkRepo);
        AppState appState = new AppState();
        eventBus = new EventBus();
        viewModel = new HyperbolicViewModel(
                noteService, linkService, appState, eventBus,
                new HyperbolicLayoutStrategy());
    }

    @Test
    @DisplayName("createLink publishes LinkCreatedEvent")
    void createLink_publishesLinkCreatedEvent() {
        Note a = noteService.createNote("A", "");
        Note b = noteService.createNote("B", "");
        List<LinkCreatedEvent> events = new ArrayList<>();
        eventBus.subscribe(LinkCreatedEvent.class, events::add);

        viewModel.createLink(a.getId(), b.getId());

        assertEquals(1, events.size());
        assertEquals(a.getId(), events.get(0).sourceId());
        assertEquals(b.getId(), events.get(0).destinationId());
    }
}
