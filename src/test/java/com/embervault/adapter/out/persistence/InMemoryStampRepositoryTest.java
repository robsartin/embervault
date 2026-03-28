package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Stamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryStampRepositoryTest {

    private InMemoryStampRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryStampRepository();
    }

    @Test
    @DisplayName("save() persists and returns stamp")
    void save_shouldPersistStamp() {
        Stamp stamp = Stamp.create("Color:red", "$Color=red");

        Stamp saved = repository.save(stamp);

        assertEquals(stamp, saved);
        assertTrue(repository.findById(stamp.id()).isPresent());
    }

    @Test
    @DisplayName("findById() returns empty for unknown id")
    void findById_shouldReturnEmptyForUnknown() {
        Optional<Stamp> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved stamps")
    void findAll_shouldReturnAll() {
        repository.save(Stamp.create("A", "$A=1"));
        repository.save(Stamp.create("B", "$B=2"));

        List<Stamp> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("delete() removes the stamp")
    void delete_shouldRemoveStamp() {
        Stamp stamp = Stamp.create("Color:red", "$Color=red");
        repository.save(stamp);

        repository.delete(stamp.id());

        assertTrue(repository.findById(stamp.id()).isEmpty());
    }

    @Test
    @DisplayName("findByName() returns matching stamp")
    void findByName_shouldReturnMatch() {
        Stamp stamp = Stamp.create("Color:red", "$Color=red");
        repository.save(stamp);

        Optional<Stamp> found = repository.findByName("Color:red");

        assertTrue(found.isPresent());
        assertEquals(stamp, found.get());
    }

    @Test
    @DisplayName("findByName() returns empty when no match")
    void findByName_shouldReturnEmptyForNoMatch() {
        Optional<Stamp> found = repository.findByName("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("save() overwrites existing stamp with same id")
    void save_shouldOverwriteExisting() {
        UUID id = UUID.randomUUID();
        Stamp original = new Stamp(id, "Color:red", "$Color=red");
        Stamp updated = new Stamp(id, "Color:blue", "$Color=blue");

        repository.save(original);
        repository.save(updated);

        Stamp found = repository.findById(id).orElseThrow();
        assertEquals("Color:blue", found.name());
    }
}
