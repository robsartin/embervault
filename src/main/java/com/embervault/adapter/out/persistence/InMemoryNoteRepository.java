package com.embervault.adapter.out.persistence;

import static com.embervault.domain.Attributes.CONTAINER;
import static com.embervault.domain.Attributes.OUTLINE_ORDER;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeValue;
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
        String parentIdStr = parentId.toString();
        return store.values().stream()
                .filter(note -> note.getAttribute(CONTAINER)
                        .filter(v -> v instanceof AttributeValue.StringValue sv
                                && parentIdStr.equals(sv.value()))
                        .isPresent())
                .sorted(Comparator.comparingDouble(note ->
                        note.getAttribute(OUTLINE_ORDER)
                                .filter(v -> v instanceof AttributeValue.NumberValue)
                                .map(v -> ((AttributeValue.NumberValue) v).value())
                                .orElse(0.0)))
                .toList();
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }
}
