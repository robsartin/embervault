package com.embervault.adapter.out.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.out.LinkRepository;
import com.embervault.domain.Link;

/**
 * In-memory implementation of {@link LinkRepository} backed by a {@link LinkedHashMap}.
 */
public final class InMemoryLinkRepository implements LinkRepository {

    private final Map<UUID, Link> store = new LinkedHashMap<>();

    @Override
    public Link save(Link link) {
        store.put(link.id(), link);
        return link;
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }

    @Override
    public Optional<Link> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Link> findLinksFrom(UUID sourceId) {
        List<Link> result = new ArrayList<>();
        for (Link link : store.values()) {
            if (link.sourceId().equals(sourceId)) {
                result.add(link);
            }
        }
        return result;
    }

    @Override
    public List<Link> findLinksTo(UUID destId) {
        List<Link> result = new ArrayList<>();
        for (Link link : store.values()) {
            if (link.destinationId().equals(destId)) {
                result.add(link);
            }
        }
        return result;
    }

    @Override
    public List<Link> findAllLinksFor(UUID noteId) {
        List<Link> result = new ArrayList<>();
        for (Link link : store.values()) {
            if (link.sourceId().equals(noteId) || link.destinationId().equals(noteId)) {
                result.add(link);
            }
        }
        return result;
    }
}
