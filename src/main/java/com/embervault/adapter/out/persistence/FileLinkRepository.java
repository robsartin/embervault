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

import com.embervault.application.port.out.LinkRepository;
import com.embervault.domain.Link;

/**
 * File-based {@link LinkRepository} using a single YAML file.
 */
public final class FileLinkRepository implements LinkRepository {

    private final Path linksFile;
    private final List<Link> links = new ArrayList<>();

    /**
     * Creates a repository backed by links.yaml in the project dir.
     */
    public FileLinkRepository(Path projectDir) {
        Objects.requireNonNull(projectDir);
        this.linksFile = projectDir.resolve("links.yaml");
        load();
    }

    @Override
    public Link save(Link link) {
        links.removeIf(l -> l.id().equals(link.id()));
        links.add(link);
        persist();
        return link;
    }

    @Override
    public void delete(UUID id) {
        links.removeIf(l -> l.id().equals(id));
        persist();
    }

    @Override
    public Optional<Link> findById(UUID id) {
        return links.stream()
                .filter(l -> l.id().equals(id))
                .findFirst();
    }

    @Override
    public List<Link> findLinksFrom(UUID sourceId) {
        return links.stream()
                .filter(l -> l.sourceId().equals(sourceId))
                .toList();
    }

    @Override
    public List<Link> findLinksTo(UUID destId) {
        return links.stream()
                .filter(l -> l.destinationId().equals(destId))
                .toList();
    }

    @Override
    public List<Link> findAllLinksFor(UUID noteId) {
        return links.stream()
                .filter(l -> l.sourceId().equals(noteId)
                        || l.destinationId().equals(noteId))
                .toList();
    }

    private void load() {
        if (Files.exists(linksFile)) {
            try {
                String yaml = Files.readString(linksFile,
                        StandardCharsets.UTF_8);
                links.addAll(
                        LinkYamlSerializer.deserialize(yaml));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void persist() {
        try {
            Files.createDirectories(linksFile.getParent());
            Files.writeString(linksFile,
                    LinkYamlSerializer.serialize(links),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
