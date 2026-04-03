package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Link;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Hyperbolic view tab.
 *
 * <p>Computes a hyperbolic layout of the link graph centered on a focus note.
 * Provides observable properties for the view layer to bind to, including
 * positioned nodes, edges, tab title, and navigation state.</p>
 */
public final class HyperbolicViewModel {

    private static final int MAX_TITLE_LENGTH = 20;
    private static final double DEFAULT_VIEWPORT_RADIUS = 300.0;
    private static final Set<String> IGNORED_LINK_TYPES = Set.of("web", "prototype");

    private final NoteService noteService;
    private final LinkService linkService;
    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<PositionedNode> nodes =
            FXCollections.observableArrayList();
    private final ObservableList<HyperbolicEdge> edges =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> focusNoteId =
            new SimpleObjectProperty<>();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NavigationStack navigationStack = new NavigationStack();
    private double viewportRadius = DEFAULT_VIEWPORT_RADIUS;
    private final AppState appState;
    private final LayoutStrategy layoutStrategy;

    /**
     * Constructs a HyperbolicViewModel with the given services and
     * default hyperbolic layout strategy.
     *
     * @param noteService the note service for querying notes
     * @param linkService the link service for querying links
     * @param appState    the shared application state for data-change notification
     */
    public HyperbolicViewModel(NoteService noteService, LinkService linkService,
            AppState appState) {
        this(noteService, linkService, appState, new HyperbolicLayoutStrategy());
    }

    /**
     * Constructs a HyperbolicViewModel with the given services and
     * layout strategy.
     *
     * @param noteService    the note service for querying notes
     * @param linkService    the link service for querying links
     * @param appState       the shared application state for data-change notification
     * @param layoutStrategy the strategy for computing node positions
     */
    public HyperbolicViewModel(NoteService noteService, LinkService linkService,
            AppState appState, LayoutStrategy layoutStrategy) {
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
        this.linkService = Objects.requireNonNull(linkService,
                "linkService must not be null");
        this.appState = Objects.requireNonNull(appState,
                "appState must not be null");
        this.layoutStrategy = Objects.requireNonNull(layoutStrategy,
                "layoutStrategy must not be null");
        tabTitle.set("Hyperbolic");
    }

    /**
     * Sets the viewport radius for layout computation.
     *
     * @param radius the viewport radius in pixels
     */
    public void setViewportRadius(double radius) {
        this.viewportRadius = radius;
    }

    /**
     * Sets the focus note and computes the hyperbolic layout.
     *
     * @param noteId the note id to focus on
     */
    public void setFocusNote(UUID noteId) {
        Objects.requireNonNull(noteId, "noteId must not be null");
        focusNoteId.set(noteId);

        noteService.getNote(noteId).ifPresent(note ->
                tabTitle.set(TextUtils.tabTitle("Hyperbolic",
                        note.getTitle(), MAX_TITLE_LENGTH)));

        computeLayout();
    }

    /**
     * Drills down to a new focus note, pushing the current focus onto the history stack.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        UUID current = focusNoteId.get();
        if (current != null) {
            navigationStack.setCurrentId(current);
            navigationStack.push(noteId);
        }
        setFocusNote(noteId);
    }

    /**
     * Navigates back to the previous focus note.
     */
    public void navigateBack() {
        UUID previous = navigationStack.pop();
        if (previous == null) {
            return;
        }
        setFocusNote(previous);
    }

    /**
     * Selects a note by id.
     *
     * @param noteId the note id to select, or null to clear
     */
    public void selectNote(UUID noteId) {
        selectedNoteId.set(noteId);
    }

    /**
     * Creates a link between two notes.
     *
     * @param source the source note id
     * @param dest   the destination note id
     */
    public void createLink(UUID source, UUID dest) {
        linkService.createLink(source, dest);
        computeLayout();
        appState.notifyDataChanged();
    }

    /** Returns the focus note id property. */
    public ObjectProperty<UUID> focusNoteIdProperty() {
        return focusNoteId;
    }

    /** Returns the current focus note id. */
    public UUID getFocusNoteId() {
        return focusNoteId.get();
    }

    /** Returns the selected note id property. */
    public ObjectProperty<UUID> selectedNoteIdProperty() {
        return selectedNoteId;
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }

    /** Returns the observable list of positioned nodes. */
    public ObservableList<PositionedNode> getNodes() {
        return nodes;
    }

    /** Returns the observable list of edges. */
    public ObservableList<HyperbolicEdge> getEdges() {
        return edges;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return navigationStack.canNavigateBackProperty();
    }

    /**
     * Returns the title of the note with the given id, or an empty string.
     *
     * @param noteId the note id
     * @return the note title, or empty string if not found
     */
    public String getNoteTitle(UUID noteId) {
        return noteService.getNote(noteId)
                .map(note -> note.getTitle())
                .orElse("");
    }

    /**
     * Returns the badge Unicode symbol for the note with the given id,
     * or an empty string if no badge is set.
     *
     * @param noteId the note id
     * @return the badge symbol, or empty string
     */
    public String getNoteBadge(UUID noteId) {
        return noteService.getNote(noteId)
                .map(NoteDisplayHelper::resolveBadge)
                .orElse("");
    }

    private void computeLayout() {
        UUID focus = focusNoteId.get();
        if (focus == null) {
            nodes.clear();
            edges.clear();
            return;
        }

        // Build adjacency from links, ignoring web and prototype
        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        List<HyperbolicEdge> edgeList = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Deque<UUID> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(focus);
        visited.add(focus);

        while (!bfsQueue.isEmpty()) {
            UUID current = bfsQueue.poll();
            List<Link> links = linkService.getAllLinksFor(current);
            for (Link link : links) {
                if (IGNORED_LINK_TYPES.contains(link.type())) {
                    continue;
                }
                UUID other = link.sourceId().equals(current)
                        ? link.destinationId() : link.sourceId();
                adjacency.computeIfAbsent(current, k -> new HashSet<>()).add(other);
                adjacency.computeIfAbsent(other, k -> new HashSet<>()).add(current);

                if (!visited.contains(other)) {
                    visited.add(other);
                    bfsQueue.add(other);
                    edgeList.add(new HyperbolicEdge(
                            link.sourceId(), link.destinationId()));
                }
            }
        }

        List<PositionedNode> layoutNodes = layoutStrategy.layout(
                focus, adjacency, viewportRadius);

        nodes.setAll(layoutNodes);
        edges.setAll(edgeList);
    }
}
