package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.SearchNotesQuery;
import com.embervault.domain.Attributes;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.SearchFilter;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FilteredSearchTest {

  private SearchNotesQuery searchQuery;
  private InMemoryNoteRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryNoteRepository();
    NoteServiceImpl service = new NoteServiceImpl(repository);
    searchQuery = service;
  }

  @Test
  @DisplayName("searchWithFilters returns notes matching color attribute")
  void searchWithFilters_shouldMatchColorAttribute() {
    Note red = Note.create("Red Note", "");
    red.setAttribute(Attributes.COLOR,
        new AttributeValue.ColorValue(TbxColor.named("red")));
    repository.save(red);

    Note blue = Note.create("Blue Note", "");
    blue.setAttribute(Attributes.COLOR,
        new AttributeValue.ColorValue(TbxColor.named("blue")));
    repository.save(blue);

    List<SearchFilter> filters = SearchFilter.parse("color:red");
    List<Note> results = searchQuery.searchWithFilters(filters);

    assertEquals(1, results.size());
    assertEquals("Red Note", results.getFirst().getTitle());
  }

  @Test
  @DisplayName("searchWithFilters with empty filters returns all notes")
  void searchWithFilters_emptyFilters_shouldReturnAll() {
    Note note = Note.create("Test", "content");
    repository.save(note);

    List<Note> results = searchQuery.searchWithFilters(List.of());

    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("searchWithFilters with substring filter matches title")
  void searchWithFilters_substringFilter_shouldMatchTitle() {
    Note match = Note.create("Meeting notes", "");
    repository.save(match);
    Note noMatch = Note.create("Shopping list", "");
    repository.save(noMatch);

    List<SearchFilter> filters = SearchFilter.parse("meeting");
    List<Note> results = searchQuery.searchWithFilters(filters);

    assertEquals(1, results.size());
    assertEquals("Meeting notes", results.getFirst().getTitle());
  }

  @Test
  @DisplayName("searchWithFilters compound filter requires all to match")
  void searchWithFilters_compoundFilter_shouldRequireAll() {
    Note match = Note.create("Task", "");
    match.setAttribute(Attributes.COLOR,
        new AttributeValue.ColorValue(TbxColor.named("red")));
    match.setAttribute(Attributes.BADGE,
        new AttributeValue.StringValue("star"));
    repository.save(match);

    Note partial = Note.create("Other", "");
    partial.setAttribute(Attributes.COLOR,
        new AttributeValue.ColorValue(TbxColor.named("red")));
    repository.save(partial);

    List<SearchFilter> filters =
        SearchFilter.parse("color:red badge:star");
    List<Note> results = searchQuery.searchWithFilters(filters);

    assertEquals(1, results.size());
    assertEquals("Task", results.getFirst().getTitle());
  }

  @Test
  @DisplayName("searchWithFilters checked:true matches boolean attr")
  void searchWithFilters_checkedFilter_shouldMatchBoolean() {
    Note checked = Note.create("Done", "");
    checked.setAttribute(Attributes.CHECKED,
        new AttributeValue.BooleanValue(true));
    repository.save(checked);

    Note unchecked = Note.create("Pending", "");
    unchecked.setAttribute(Attributes.CHECKED,
        new AttributeValue.BooleanValue(false));
    repository.save(unchecked);

    List<SearchFilter> filters = SearchFilter.parse("checked:true");
    List<Note> results = searchQuery.searchWithFilters(filters);

    assertEquals(1, results.size());
    assertEquals("Done", results.getFirst().getTitle());
  }

  @Test
  @DisplayName("searchWithFilters has:children matches parent notes")
  void searchWithFilters_hasChildren_shouldMatchParents() {
    Note parent = Note.create("Parent", "");
    repository.save(parent);

    Note child = Note.create("Child", "");
    child.setAttribute(Attributes.CONTAINER,
        new AttributeValue.StringValue(parent.getId().toString()));
    repository.save(child);

    Note lonely = Note.create("Lonely", "");
    repository.save(lonely);

    List<SearchFilter> filters = SearchFilter.parse("has:children");
    List<Note> results = searchQuery.searchWithFilters(filters);

    assertEquals(1, results.size());
    assertEquals("Parent", results.getFirst().getTitle());
  }
}
