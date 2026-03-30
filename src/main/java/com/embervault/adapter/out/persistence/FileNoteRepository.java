package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;

/**
 * File-based {@link NoteRepository} using UUID-sharded directories.
 *
 * <p>Notes are stored as Markdown files with YAML front matter in
 * {@code projectDir/notes/<first-8-uuid-chars>/<full-uuid>.md}.</p>
 */
public final class FileNoteRepository implements NoteRepository {

    private final Path notesDir;
    private final NoteFileSerializer serializer;
    private final NoteFileDeserializer deserializer;

    /**
     * Creates a repository backed by the given project directory.
     *
     * @param projectDir the project root directory
     * @param registry   the attribute schema registry
     */
    public FileNoteRepository(Path projectDir,
            AttributeSchemaRegistry registry) {
        Objects.requireNonNull(projectDir);
        Objects.requireNonNull(registry);
        this.notesDir = projectDir.resolve("notes");
        this.serializer = new NoteFileSerializer();
        this.deserializer = new NoteFileDeserializer(registry);
    }

    @Override
    public Note save(Note note) {
        Path file = noteFile(note.getId());
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file,
                    serializer.serialize(note),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return note;
    }

    @Override
    public Optional<Note> findById(UUID id) {
        Path file = noteFile(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(file,
                    StandardCharsets.UTF_8);
            return Optional.of(
                    deserializer.deserialize(content, id));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Note> findAll() {
        if (!Files.exists(notesDir)) {
            return List.of();
        }
        try (Stream<Path> shards = Files.list(notesDir)) {
            return shards
                    .filter(Files::isDirectory)
                    .flatMap(this::listNoteFiles)
                    .map(this::readNote)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Note> findChildren(UUID parentId) {
        return findAll().stream()
                .filter(note -> containerUuid(note)
                        .filter(parentId::equals)
                        .isPresent())
                .sorted((a, b) -> Double.compare(
                        outlineOrder(a), outlineOrder(b)))
                .toList();
    }

    @Override
    public Set<UUID> findNoteIdsWithChildren(
            Collection<UUID> noteIds) {
        Set<UUID> candidates = new HashSet<>(noteIds);
        Set<UUID> result = new HashSet<>();
        for (Note note : findAll()) {
            containerUuid(note).ifPresent(parentId -> {
                if (candidates.contains(parentId)) {
                    result.add(parentId);
                }
            });
        }
        return result;
    }

    @Override
    public void delete(UUID id) {
        Path file = noteFile(id);
        try {
            Files.deleteIfExists(file);
            Path shardDir = file.getParent();
            if (shardDir != null && Files.exists(shardDir)
                    && isEmptyDir(shardDir)) {
                Files.delete(shardDir);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path noteFile(UUID id) {
        String idStr = id.toString();
        String shard = idStr.substring(0, 8);
        return notesDir.resolve(shard)
                .resolve(idStr + ".md");
    }

    private Optional<UUID> containerUuid(Note note) {
        return note.getAttribute(Attributes.CONTAINER)
                .filter(AttributeValue.StringValue.class
                        ::isInstance)
                .map(v -> ((AttributeValue.StringValue) v)
                        .value())
                .flatMap(s -> {
                    try {
                        return Optional.of(UUID.fromString(s));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    private double outlineOrder(Note note) {
        return note.getAttribute(Attributes.OUTLINE_ORDER)
                .filter(AttributeValue.NumberValue.class
                        ::isInstance)
                .map(v -> ((AttributeValue.NumberValue) v)
                        .value())
                .orElse(0.0);
    }

    private Stream<Path> listNoteFiles(Path shardDir) {
        try {
            return Files.list(shardDir)
                    .filter(p -> p.toString().endsWith(".md"));
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    private Note readNote(Path file) {
        try {
            String name = file.getFileName().toString();
            String idStr = name.substring(
                    0, name.length() - 3);
            UUID id = UUID.fromString(idStr);
            String content = Files.readString(file,
                    StandardCharsets.UTF_8);
            return deserializer.deserialize(content, id);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isEmptyDir(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.findFirst().isEmpty();
        }
    }
}
