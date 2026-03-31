package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TextLinkService} — syncs wiki-links from note text to Link records.
 */
class TextLinkServiceTest {

    private TextLinkService textLinkService;
    private NoteService noteService;
    private LinkService linkService;
    private InMemoryLinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(noteRepository);
        linkRepository = new InMemoryLinkRepository();
        linkService = new LinkServiceImpl(linkRepository);
        textLinkService = new TextLinkService(noteService, linkService);
    }

    @Test
    @DisplayName("text with one wiki-link creates a text link")
    void singleWikiLink_createsLink() {
        Note source = noteService.createNote("Source", "");
        Note target = noteService.createNote("Target", "");

        source.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("See [[Target]] for details"));

        textLinkService.syncLinks(source);

        List<Link> links = linkService.getLinksFrom(source.getId());
        assertEquals(1, links.size());
        assertEquals("text", links.getFirst().type());
        assertEquals(target.getId(), links.getFirst().destinationId());
    }

    @Test
    @DisplayName("removing wiki-link from text deletes the link")
    void removeWikiLink_deletesLink() {
        Note source = noteService.createNote("Source", "");
        noteService.createNote("Target", "");

        source.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("See [[Target]]"));
        textLinkService.syncLinks(source);

        assertEquals(1, linkService.getLinksFrom(source.getId()).size());

        // Remove the wiki-link
        source.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("No links here"));
        textLinkService.syncLinks(source);

        assertTrue(linkService.getLinksFrom(source.getId()).isEmpty());
    }

    @Test
    @DisplayName("wiki-link to non-existent note creates no link")
    void nonExistentTarget_noLink() {
        Note source = noteService.createNote("Source", "");

        source.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("See [[No Such Note]]"));
        textLinkService.syncLinks(source);

        assertTrue(linkService.getLinksFrom(source.getId()).isEmpty());
    }

    @Test
    @DisplayName("multiple wiki-links create multiple links")
    void multipleWikiLinks_createMultipleLinks() {
        Note source = noteService.createNote("Source", "");
        Note a = noteService.createNote("Alpha", "");
        Note b = noteService.createNote("Beta", "");

        source.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("See [[Alpha]] and [[Beta]]"));
        textLinkService.syncLinks(source);

        List<Link> links = linkService.getLinksFrom(source.getId());
        assertEquals(2, links.size());
        assertTrue(links.stream().anyMatch(l -> l.destinationId().equals(a.getId())));
        assertTrue(links.stream().anyMatch(l -> l.destinationId().equals(b.getId())));
    }

    @Test
    @DisplayName("getBacklinks returns notes that link to a given note")
    void getBacklinks() {
        Note target = noteService.createNote("Target", "");
        Note source1 = noteService.createNote("Source1", "");
        Note source2 = noteService.createNote("Source2", "");

        source1.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("See [[Target]]"));
        textLinkService.syncLinks(source1);

        source2.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("Also [[Target]]"));
        textLinkService.syncLinks(source2);

        List<Note> backlinks = textLinkService.getBacklinks(target.getId());
        assertEquals(2, backlinks.size());
    }
}
