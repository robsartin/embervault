package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class HyperbolicViewModelLayoutStrategyTest {

    private NoteService noteService;
    private LinkService linkService;
    private AppState appState;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(noteRepo);
        InMemoryLinkRepository linkRepo = new InMemoryLinkRepository();
        linkService = new LinkServiceImpl(linkRepo);
        appState = new AppState();
    }

    @Test
    @DisplayName("constructor rejects null layoutStrategy")
    void constructor_shouldRejectNullLayoutStrategy() {
        assertThrows(NullPointerException.class,
                () -> new HyperbolicViewModel(
                        noteService, linkService, appState, null));
    }

    @Test
    @DisplayName("setFocusNote delegates to injected LayoutStrategy")
    void setFocusNote_shouldDelegateToInjectedStrategy() {
        List<UUID> capturedFocusIds = new ArrayList<>();
        LayoutStrategy spyStrategy = (focusId, adjacency, viewportRadius) -> {
            capturedFocusIds.add(focusId);
            return List.of(new PositionedNode(focusId, 0, 0, 36.0, 0));
        };

        HyperbolicViewModel vm = new HyperbolicViewModel(
                noteService, linkService, appState, spyStrategy);
        Note note = noteService.createNote("Test", "");

        vm.setFocusNote(note.getId());

        assertEquals(1, capturedFocusIds.size());
        assertEquals(note.getId(), capturedFocusIds.get(0));
        assertEquals(1, vm.getNodes().size());
    }

    @Test
    @DisplayName("custom strategy result is reflected in ViewModel nodes")
    void customStrategy_shouldPopulateViewModelNodes() {
        UUID fakeId = UUID.randomUUID();
        LayoutStrategy customStrategy = (focusId, adjacency, viewportRadius) ->
                List.of(
                        new PositionedNode(focusId, 0, 0, 50.0, 0),
                        new PositionedNode(fakeId, 10, 20, 25.0, 1)
                );

        HyperbolicViewModel vm = new HyperbolicViewModel(
                noteService, linkService, appState, customStrategy);
        Note note = noteService.createNote("Test", "");

        vm.setFocusNote(note.getId());

        assertEquals(2, vm.getNodes().size());
        assertEquals(fakeId, vm.getNodes().get(1).noteId());
        assertEquals(10, vm.getNodes().get(1).x(), 0.001);
    }

    @Test
    @DisplayName("three-arg constructor still works with default strategy")
    void threeArgConstructor_shouldUseDefaultStrategy() {
        HyperbolicViewModel vm = new HyperbolicViewModel(
                noteService, linkService, appState);
        Note note = noteService.createNote("Solo", "");

        vm.setFocusNote(note.getId());

        assertEquals(1, vm.getNodes().size());
        assertTrue(vm.getEdges().isEmpty());
    }
}
