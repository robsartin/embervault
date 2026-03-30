package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.UUID;

import com.embervault.domain.Link;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link FileLinkRepository}.
 */
class FileLinkRepositoryTest {

    @Test
    @DisplayName("save and findById round-trips")
    void save_findById(@TempDir Path dir) {
        FileLinkRepository repo = new FileLinkRepository(dir);
        Link link = Link.create(UUID.randomUUID(),
                UUID.randomUUID(), "web");
        repo.save(link);

        assertTrue(repo.findById(link.id()).isPresent());
        assertEquals("web",
                repo.findById(link.id()).get().type());
    }

    @Test
    @DisplayName("findLinksFrom filters by source")
    void findLinksFrom(@TempDir Path dir) {
        FileLinkRepository repo = new FileLinkRepository(dir);
        UUID src = UUID.randomUUID();
        repo.save(Link.create(src, UUID.randomUUID()));
        repo.save(Link.create(UUID.randomUUID(),
                UUID.randomUUID()));

        assertEquals(1, repo.findLinksFrom(src).size());
    }

    @Test
    @DisplayName("delete removes link")
    void delete_removesLink(@TempDir Path dir) {
        FileLinkRepository repo = new FileLinkRepository(dir);
        Link link = Link.create(UUID.randomUUID(),
                UUID.randomUUID());
        repo.save(link);
        repo.delete(link.id());

        assertFalse(repo.findById(link.id()).isPresent());
    }

    @Test
    @DisplayName("data survives reload")
    void reload_preservesData(@TempDir Path dir) {
        FileLinkRepository repo1 = new FileLinkRepository(dir);
        Link link = Link.create(UUID.randomUUID(),
                UUID.randomUUID(), "proto");
        repo1.save(link);

        FileLinkRepository repo2 = new FileLinkRepository(dir);
        assertTrue(repo2.findById(link.id()).isPresent());
    }
}
