package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.embervault.domain.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryLinkRepositoryTest {

    private InMemoryLinkRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLinkRepository();
    }

    @Test
    @DisplayName("save and findById round-trips a link")
    void saveAndFindById_shouldRoundTrip() {
        Link link = Link.create(UUID.randomUUID(), UUID.randomUUID());
        repository.save(link);

        assertTrue(repository.findById(link.id()).isPresent());
        assertEquals(link, repository.findById(link.id()).get());
    }

    @Test
    @DisplayName("findById returns empty for unknown id")
    void findById_shouldReturnEmptyForUnknownId() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("delete removes the link")
    void delete_shouldRemoveLink() {
        Link link = Link.create(UUID.randomUUID(), UUID.randomUUID());
        repository.save(link);

        repository.delete(link.id());

        assertTrue(repository.findById(link.id()).isEmpty());
    }

    @Test
    @DisplayName("findLinksFrom returns outbound links")
    void findLinksFrom_shouldReturnOutboundLinks() {
        UUID source = UUID.randomUUID();
        Link link1 = Link.create(source, UUID.randomUUID());
        Link link2 = Link.create(source, UUID.randomUUID());
        Link other = Link.create(UUID.randomUUID(), UUID.randomUUID());
        repository.save(link1);
        repository.save(link2);
        repository.save(other);

        List<Link> links = repository.findLinksFrom(source);

        assertEquals(2, links.size());
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
    }

    @Test
    @DisplayName("findLinksTo returns inbound links")
    void findLinksTo_shouldReturnInboundLinks() {
        UUID dest = UUID.randomUUID();
        Link link1 = Link.create(UUID.randomUUID(), dest);
        Link link2 = Link.create(UUID.randomUUID(), dest);
        Link other = Link.create(UUID.randomUUID(), UUID.randomUUID());
        repository.save(link1);
        repository.save(link2);
        repository.save(other);

        List<Link> links = repository.findLinksTo(dest);

        assertEquals(2, links.size());
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
    }

    @Test
    @DisplayName("findAllLinksFor returns links in both directions")
    void findAllLinksFor_shouldReturnBothDirections() {
        UUID noteId = UUID.randomUUID();
        Link outbound = Link.create(noteId, UUID.randomUUID());
        Link inbound = Link.create(UUID.randomUUID(), noteId);
        Link other = Link.create(UUID.randomUUID(), UUID.randomUUID());
        repository.save(outbound);
        repository.save(inbound);
        repository.save(other);

        List<Link> links = repository.findAllLinksFor(noteId);

        assertEquals(2, links.size());
        assertTrue(links.contains(outbound));
        assertTrue(links.contains(inbound));
    }

    @Test
    @DisplayName("findLinksFrom returns empty list for unknown source")
    void findLinksFrom_shouldReturnEmptyForUnknown() {
        assertTrue(repository.findLinksFrom(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("findLinksTo returns empty list for unknown dest")
    void findLinksTo_shouldReturnEmptyForUnknown() {
        assertTrue(repository.findLinksTo(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("findAllLinksFor returns empty list for unknown note")
    void findAllLinksFor_shouldReturnEmptyForUnknown() {
        assertTrue(repository.findAllLinksFor(UUID.randomUUID()).isEmpty());
    }
}
