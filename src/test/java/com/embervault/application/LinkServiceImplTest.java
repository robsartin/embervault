package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.domain.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinkServiceImplTest {

    private LinkServiceImpl service;
    private InMemoryLinkRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLinkRepository();
        service = new LinkServiceImpl(repository);
    }

    @Test
    @DisplayName("constructor rejects null repository")
    void constructor_shouldRejectNullRepository() {
        assertThrows(NullPointerException.class, () -> new LinkServiceImpl(null));
    }

    @Test
    @DisplayName("createLink creates link with default type")
    void createLink_shouldCreateWithDefaultType() {
        UUID source = UUID.randomUUID();
        UUID dest = UUID.randomUUID();

        Link link = service.createLink(source, dest);

        assertNotNull(link);
        assertEquals(source, link.sourceId());
        assertEquals(dest, link.destinationId());
        assertEquals("untitled", link.type());
        assertTrue(repository.findById(link.id()).isPresent());
    }

    @Test
    @DisplayName("createLink with type creates link with specified type")
    void createLink_withType_shouldCreateWithSpecifiedType() {
        UUID source = UUID.randomUUID();
        UUID dest = UUID.randomUUID();

        Link link = service.createLink(source, dest, "web");

        assertEquals("web", link.type());
    }

    @Test
    @DisplayName("getLinksFrom returns outbound links")
    void getLinksFrom_shouldReturnOutboundLinks() {
        UUID source = UUID.randomUUID();
        service.createLink(source, UUID.randomUUID());
        service.createLink(source, UUID.randomUUID());

        List<Link> links = service.getLinksFrom(source);

        assertEquals(2, links.size());
    }

    @Test
    @DisplayName("getLinksTo returns inbound links")
    void getLinksTo_shouldReturnInboundLinks() {
        UUID dest = UUID.randomUUID();
        service.createLink(UUID.randomUUID(), dest);
        service.createLink(UUID.randomUUID(), dest);

        List<Link> links = service.getLinksTo(dest);

        assertEquals(2, links.size());
    }

    @Test
    @DisplayName("getAllLinksFor returns all connected links")
    void getAllLinksFor_shouldReturnAllConnectedLinks() {
        UUID noteId = UUID.randomUUID();
        service.createLink(noteId, UUID.randomUUID());
        service.createLink(UUID.randomUUID(), noteId);

        List<Link> links = service.getAllLinksFor(noteId);

        assertEquals(2, links.size());
    }

    @Test
    @DisplayName("deleteLink removes the link")
    void deleteLink_shouldRemoveLink() {
        Link link = service.createLink(UUID.randomUUID(), UUID.randomUUID());

        service.deleteLink(link.id());

        assertTrue(repository.findById(link.id()).isEmpty());
    }
}
