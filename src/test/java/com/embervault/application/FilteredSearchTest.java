package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import com.embervault.domain.SearchFilter;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FilteredSearchTest {

    private NoteServiceImpl noteService;
    private FilteredSearchService searchService;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        searchService = new FilteredSearchService(repository);
    }

    @Test
    @DisplayName("empty filter returns empty results")
    void emptyFilter_shouldReturnEmpty() {
        noteService.createNote("Alpha", "content");

        SearchFilter filter = new SearchFilter("", Map.of(), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("text-only filter matches like basic search")
    void textOnlyFilter_shouldMatchLikeBasicSearch() {
        noteService.createNote("Meeting notes", "agenda");
        noteService.createNote("Unrelated", "stuff");

        SearchFilter filter = new SearchFilter(
                "meeting", Map.of(), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
        assertEquals("Meeting notes", results.get(0).getTitle());
    }

    @Test
    @DisplayName("color filter matches notes with matching color")
    void colorFilter_shouldMatchColorAttribute() {
        Note red = noteService.createNote("Red note", "");
        red.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("red")));
        repository.save(red);

        Note blue = noteService.createNote("Blue note", "");
        blue.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("blue")));
        repository.save(blue);

        SearchFilter filter = new SearchFilter(
                "", Map.of(Attributes.COLOR, "red"), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
        assertEquals("Red note", results.get(0).getTitle());
    }

    @Test
    @DisplayName("badge filter matches notes with matching badge")
    void badgeFilter_shouldMatchBadgeAttribute() {
        Note starred = noteService.createNote("Starred", "");
        starred.setAttribute(Attributes.BADGE,
                new AttributeValue.StringValue("star"));
        repository.save(starred);

        noteService.createNote("Plain", "");

        SearchFilter filter = new SearchFilter(
                "", Map.of(Attributes.BADGE, "star"), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
        assertEquals("Starred", results.get(0).getTitle());
    }

    @Test
    @DisplayName("checked filter matches notes with checked=true")
    void checkedFilter_shouldMatchCheckedAttribute() {
        Note checked = noteService.createNote("Done", "");
        checked.setAttribute(Attributes.CHECKED,
                new AttributeValue.BooleanValue(true));
        repository.save(checked);

        Note unchecked = noteService.createNote("Todo", "");
        unchecked.setAttribute(Attributes.CHECKED,
                new AttributeValue.BooleanValue(false));
        repository.save(unchecked);

        SearchFilter filter = new SearchFilter(
                "", Map.of(Attributes.CHECKED, "true"), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
        assertEquals("Done", results.get(0).getTitle());
    }

    @Test
    @DisplayName("text and attribute filter both applied (AND logic)")
    void textAndAttributeFilter_shouldApplyBoth() {
        Note match = noteService.createNote("Meeting agenda", "");
        match.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("red")));
        repository.save(match);

        noteService.createNote("Meeting notes", "");

        Note colorOnly = noteService.createNote("Unrelated", "");
        colorOnly.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("red")));
        repository.save(colorOnly);

        SearchFilter filter = new SearchFilter(
                "meeting", Map.of(Attributes.COLOR, "red"), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
        assertEquals("Meeting agenda", results.get(0).getTitle());
    }

    @Test
    @DisplayName("has:children filter matches notes with children")
    void hasChildrenFilter_shouldMatchParentNotes() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child");

        noteService.createNote("Leaf", "");

        SearchFilter filter = new SearchFilter(
                "", Map.of(), List.of("children"));
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
        assertEquals("Parent", results.get(0).getTitle());
    }

    @Test
    @DisplayName("string attribute filter uses case-insensitive match")
    void stringFilter_shouldBeCaseInsensitive() {
        Note note = noteService.createNote("Test", "");
        note.setAttribute(Attributes.BADGE,
                new AttributeValue.StringValue("Star"));
        repository.save(note);

        SearchFilter filter = new SearchFilter(
                "", Map.of(Attributes.BADGE, "star"), List.of());
        List<Note> results = searchService.searchNotesFiltered(filter);

        assertEquals(1, results.size());
    }
}
