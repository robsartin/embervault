package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.out.StampRepository;
import com.embervault.domain.Stamp;

/**
 * File-based {@link StampRepository} using a single YAML file.
 */
public final class FileStampRepository implements StampRepository {

    private final Path stampsFile;
    private final List<Stamp> stamps = new ArrayList<>();

    /**
     * Creates a repository backed by stamps.yaml in the project dir.
     */
    public FileStampRepository(Path projectDir) {
        Objects.requireNonNull(projectDir);
        this.stampsFile = projectDir.resolve("stamps.yaml");
        load();
    }

    @Override
    public Stamp save(Stamp stamp) {
        stamps.removeIf(s -> s.id().equals(stamp.id()));
        stamps.add(stamp);
        persist();
        return stamp;
    }

    @Override
    public void delete(UUID id) {
        stamps.removeIf(s -> s.id().equals(id));
        persist();
    }

    @Override
    public Optional<Stamp> findById(UUID id) {
        return stamps.stream()
                .filter(s -> s.id().equals(id))
                .findFirst();
    }

    @Override
    public List<Stamp> findAll() {
        return List.copyOf(stamps);
    }

    @Override
    public Optional<Stamp> findByName(String name) {
        return stamps.stream()
                .filter(s -> s.name().equals(name))
                .findFirst();
    }

    private void load() {
        if (Files.exists(stampsFile)) {
            try {
                String yaml = Files.readString(stampsFile,
                        StandardCharsets.UTF_8);
                stamps.addAll(
                        StampYamlSerializer.deserialize(yaml));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void persist() {
        try {
            Files.createDirectories(stampsFile.getParent());
            Files.writeString(stampsFile,
                    StampYamlSerializer.serialize(stamps),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
