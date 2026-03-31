package com.embervault.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Attributes;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.WikiLinkParser;

/**
 * Synchronizes wiki-link references in note text with Link records.
 *
 * <p>When a note's {@code $Text} is updated, call {@link #syncLinks(Note)}
 * to create or remove {@code "text"}-type links based on the
 * {@code [[Note Title]]} references found in the text.</p>
 */
public class TextLinkService {

    /** Link type used for wiki-link references originating from note text. */
    public static final String TEXT_LINK_TYPE = "text";

    private final NoteService noteService;
    private final LinkService linkService;

    /**
     * Constructs a TextLinkService.
     *
     * @param noteService the note service for resolving titles to note ids
     * @param linkService the link service for managing link records
     */
    public TextLinkService(NoteService noteService, LinkService linkService) {
        this.noteService = Objects.requireNonNull(noteService);
        this.linkService = Objects.requireNonNull(linkService);
    }

    /**
     * Syncs text-type links for the given note based on its current $Text.
     *
     * <p>Parses [[Note Title]] references, resolves them to existing notes,
     * and creates or removes links as needed.</p>
     *
     * @param note the note whose text links should be synced
     */
    public void syncLinks(Note note) {
        UUID sourceId = note.getId();
        String text = note.getAttribute(Attributes.TEXT)
                .map(v -> ((com.embervault.domain.AttributeValue.StringValue) v).value())
                .orElse("");

        // Parse desired targets from text
        List<String> titles = WikiLinkParser.parse(text);
        List<UUID> desiredTargets = new ArrayList<>();
        for (String title : titles) {
            findNoteByTitle(title).ifPresent(targetNote -> {
                if (!targetNote.getId().equals(sourceId)) {
                    desiredTargets.add(targetNote.getId());
                }
            });
        }

        // Get existing text links from this note
        List<Link> existingTextLinks = linkService.getLinksFrom(sourceId).stream()
                .filter(l -> TEXT_LINK_TYPE.equals(l.type()))
                .toList();

        // Remove links that are no longer referenced
        for (Link link : existingTextLinks) {
            if (!desiredTargets.contains(link.destinationId())) {
                linkService.deleteLink(link.id());
            }
        }

        // Add links for new references
        List<UUID> existingTargets = existingTextLinks.stream()
                .map(Link::destinationId)
                .toList();
        for (UUID targetId : desiredTargets) {
            if (!existingTargets.contains(targetId)) {
                linkService.createLink(sourceId, targetId, TEXT_LINK_TYPE);
            }
        }
    }

    /**
     * Returns notes that have text-type links pointing to the given note.
     *
     * @param noteId the target note id
     * @return source notes that link to this note via wiki-links
     */
    public List<Note> getBacklinks(UUID noteId) {
        return linkService.getLinksTo(noteId).stream()
                .filter(l -> TEXT_LINK_TYPE.equals(l.type()))
                .map(l -> noteService.getNote(l.sourceId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Note> findNoteByTitle(String title) {
        return noteService.getAllNotes().stream()
                .filter(n -> title.equals(n.getTitle()))
                .findFirst();
    }
}
