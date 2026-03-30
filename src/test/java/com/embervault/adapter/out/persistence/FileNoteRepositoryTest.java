package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link FileNoteRepository}.
 */
class FileNoteRepositoryTest {

    private FileNoteRepository repository;

    @BeforeEach
    void setUp(@TempDir Path dir) {
        repository = new FileNoteRepository(dir,
                new AttributeSchemaRegistry());
    }

    @Test
    @DisplayName("save creates sharded file")
    void save_createsFile(@TempDir Path dir) {
        FileNoteRepository repo = new FileNoteRepository(dir,
                new AttributeSchemaRegistry());
        Note note = Note.create("Hello", "World");
        repo.save(note);

        String shard = note.getId().toString().substring(0, 8);
        Path file = dir.resolve("notes").resolve(shard)
                .resolve(note.getId() + ".md");
        assertTrue(Files.exists(file));
    }

    @Test
    @DisplayName("findById after save returns note")
    void findById_afterSave() {
        Note note = Note.create("Test", "Content");
        repository.save(note);

        Optional<Note> found =
                repository.findById(note.getId());
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getTitle());
        assertEquals("Content", found.get().getContent());
    }

    @Test
    @DisplayName("findById unknown returns empty")
    void findById_unknown() {
        Optional<Note> found =
                repository.findById(UUID.randomUUID());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAll returns all saved notes")
    void findAll_afterSaves() {
        repository.save(Note.create("A", ""));
        repository.save(Note.create("B", ""));
        repository.save(Note.create("C", ""));

        List<Note> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("findAll on empty returns empty list")
    void findAll_empty() {
        assertEquals(0, repository.findAll().size());
    }

    @Test
    @DisplayName("delete removes file")
    void delete_removesFile() {
        Note note = Note.create("ToDelete", "");
        repository.save(note);
        assertTrue(repository.findById(note.getId()).isPresent());

        repository.delete(note.getId());
        assertFalse(
                repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("save overwrites existing note")
    void save_overwrites() {
        Note note = Note.create("Original", "");
        repository.save(note);

        note.update("Updated", "New content");
        repository.save(note);

        Note found = repository.findById(note.getId())
                .orElseThrow();
        assertEquals("Updated", found.getTitle());
        assertEquals("New content", found.getContent());
    }

    @Test
    @DisplayName("findChildren returns sorted by OutlineOrder")
    void findChildren_sorted() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);

        Note child1 = Note.create("First", "");
        child1.setAttribute(Attributes.CONTAINER,
                new AttributeValue.StringValue(
                        parent.getId().toString()));
        child1.setAttribute(Attributes.OUTLINE_ORDER,
                new AttributeValue.NumberValue(2.0));
        repository.save(child1);

        Note child2 = Note.create("Second", "");
        child2.setAttribute(Attributes.CONTAINER,
                new AttributeValue.StringValue(
                        parent.getId().toString()));
        child2.setAttribute(Attributes.OUTLINE_ORDER,
                new AttributeValue.NumberValue(1.0));
        repository.save(child2);

        List<Note> children =
                repository.findChildren(parent.getId());
        assertEquals(2, children.size());
        assertEquals("Second", children.get(0).getTitle());
        assertEquals("First", children.get(1).getTitle());
    }

    @Test
    @DisplayName("findNoteIdsWithChildren returns correct set")
    void findNoteIdsWithChildren_works() {
        Note parent = Note.create("Parent", "");
        Note lonely = Note.create("Lonely", "");
        repository.save(parent);
        repository.save(lonely);

        Note child = Note.create("Child", "");
        child.setAttribute(Attributes.CONTAINER,
                new AttributeValue.StringValue(
                        parent.getId().toString()));
        repository.save(child);

        Set<UUID> result = repository.findNoteIdsWithChildren(
                Set.of(parent.getId(), lonely.getId()));
        assertEquals(1, result.size());
        assertTrue(result.contains(parent.getId()));
    }
}
