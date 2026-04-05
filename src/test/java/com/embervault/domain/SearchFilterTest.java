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
  void emptyQueryParsesToEmptyList() {
    assertTrue(SearchFilter.parse("").isEmpty());
    assertTrue(SearchFilter.parse("  ").isEmpty());
    assertTrue(SearchFilter.parse(null).isEmpty());
  }
}
