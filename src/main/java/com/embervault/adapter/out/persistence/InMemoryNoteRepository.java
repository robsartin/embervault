package com.embervault.adapter.out.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Note;

/**
 * In-memory implementation of {@link NoteRepository} backed by a {@link LinkedHashMap}.
 */
public final class InMemoryNoteRepository implements NoteRepository {

    private final Map<UUID, Note> store = new LinkedHashMap<>();

    @Override
    public Note save(Note note) {
        store.put(note.getId(), note);
        return note;
    }

    @Override
    public Optional<Note> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Note> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Note> findChildren(UUID parentId) {
        Note parent = store.get(parentId);
        if (parent == null) {
            return List.of();
        }
        return parent.getChildIds().stream()
                .map(store::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }
}
