package com.embervault.adapter.out.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.out.StampRepository;
import com.embervault.domain.Stamp;

/**
 * In-memory implementation of {@link StampRepository} backed by a {@link LinkedHashMap}.
 */
public final class InMemoryStampRepository implements StampRepository {

    private final Map<UUID, Stamp> store = new LinkedHashMap<>();

    @Override
    public Stamp save(Stamp stamp) {
        store.put(stamp.id(), stamp);
        return stamp;
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }

    @Override
    public Optional<Stamp> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Stamp> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Stamp> findByName(String name) {
        return store.values().stream()
                .filter(s -> s.name().equals(name))
                .findFirst();
    }
}
