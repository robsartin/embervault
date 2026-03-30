package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import com.embervault.domain.Stamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link FileStampRepository}.
 */
class FileStampRepositoryTest {

    @Test
    @DisplayName("save and findById round-trips")
    void save_findById(@TempDir Path dir) {
        FileStampRepository repo =
                new FileStampRepository(dir);
        Stamp stamp = Stamp.create("Color:red", "$Color=red");
        repo.save(stamp);

        assertTrue(repo.findById(stamp.id()).isPresent());
    }

    @Test
    @DisplayName("findAll returns all stamps")
    void findAll(@TempDir Path dir) {
        FileStampRepository repo =
                new FileStampRepository(dir);
        repo.save(Stamp.create("A", "$Color=red"));
        repo.save(Stamp.create("B", "$Color=blue"));

        assertEquals(2, repo.findAll().size());
    }

    @Test
    @DisplayName("findByName finds stamp")
    void findByName(@TempDir Path dir) {
        FileStampRepository repo =
                new FileStampRepository(dir);
        repo.save(Stamp.create("Mark Done", "$Checked=true"));

        assertTrue(repo.findByName("Mark Done").isPresent());
        assertFalse(repo.findByName("Unknown").isPresent());
    }

    @Test
    @DisplayName("delete removes stamp")
    void delete_removesStamp(@TempDir Path dir) {
        FileStampRepository repo =
                new FileStampRepository(dir);
        Stamp stamp = Stamp.create("ToDelete", "$Color=red");
        repo.save(stamp);
        repo.delete(stamp.id());

        assertFalse(repo.findById(stamp.id()).isPresent());
    }

    @Test
    @DisplayName("data survives reload")
    void reload_preservesData(@TempDir Path dir) {
        FileStampRepository repo1 =
                new FileStampRepository(dir);
        repo1.save(Stamp.create("Test", "$Badge=star"));

        FileStampRepository repo2 =
                new FileStampRepository(dir);
        assertEquals(1, repo2.findAll().size());
    }
}
