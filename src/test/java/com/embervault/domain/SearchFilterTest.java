package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SearchFilterTest {

  @Test
  void plainTextQueryParsesToSubstringFilter() {
    List<SearchFilter> filters = SearchFilter.parse("meeting");

    assertEquals(1, filters.size());
    SearchFilter filter = filters.getFirst();
    assertTrue(filter instanceof SearchFilter.SubstringFilter);
    assertEquals("meeting",
        ((SearchFilter.SubstringFilter) filter).text());
  }

  @Test
  void keyValueQueryParsesToAttributeFilter() {
    List<SearchFilter> filters = SearchFilter.parse("color:red");

    assertEquals(1, filters.size());
    SearchFilter filter = filters.getFirst();
    assertTrue(filter instanceof SearchFilter.AttributeFilter);
    SearchFilter.AttributeFilter af =
        (SearchFilter.AttributeFilter) filter;
    assertEquals("color", af.attributeKey());
    assertEquals("red", af.value());
  }

  @Test
  void compoundQueryParsesMultipleFilters() {
    List<SearchFilter> filters =
        SearchFilter.parse("badge:star checked:true");

    assertEquals(2, filters.size());
    assertTrue(filters.get(0) instanceof SearchFilter.AttributeFilter);
    assertTrue(filters.get(1) instanceof SearchFilter.AttributeFilter);
    assertEquals("badge",
        ((SearchFilter.AttributeFilter) filters.get(0)).attributeKey());
    assertEquals("checked",
        ((SearchFilter.AttributeFilter) filters.get(1)).attributeKey());
  }

  @Test
  void mixedQueryParsesSubstringAndAttributeFilters() {
    List<SearchFilter> filters =
        SearchFilter.parse("meeting color:red");

    assertEquals(2, filters.size());
    assertTrue(filters.get(0) instanceof SearchFilter.SubstringFilter);
    assertTrue(filters.get(1) instanceof SearchFilter.AttributeFilter);
  }

  @Test
  void hasRelationParsesToRelationFilter() {
    List<SearchFilter> filters = SearchFilter.parse("has:children");

    assertEquals(1, filters.size());
    assertTrue(filters.getFirst()
        instanceof SearchFilter.RelationFilter);
    assertEquals("children",
        ((SearchFilter.RelationFilter) filters.getFirst()).relation());
  }

  @Test
  void emptyQueryParsesToEmptyList() {
    assertTrue(SearchFilter.parse("").isEmpty());
    assertTrue(SearchFilter.parse("  ").isEmpty());
    assertTrue(SearchFilter.parse(null).isEmpty());
  }
}
