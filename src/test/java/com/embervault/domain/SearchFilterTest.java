package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchFilterTest {

  @Test
  @DisplayName("SearchFilter with text only has empty filters")
  void textOnly_shouldHaveEmptyFilters() {
    SearchFilter filter = new SearchFilter("meeting", Map.of(), List.of());

    assertEquals("meeting", filter.textQuery());
    assertTrue(filter.attributeFilters().isEmpty());
    assertTrue(filter.relationshipFilters().isEmpty());
  }

  @Test
  @DisplayName("SearchFilter with attribute filters retains them")
  void withAttributeFilters_shouldRetainThem() {
    Map<String, String> attrs = Map.of("$Color", "red");
    SearchFilter filter = new SearchFilter("", attrs, List.of());

    assertEquals("red", filter.attributeFilters().get("$Color"));
    assertTrue(filter.textQuery().isEmpty());
  }

  @Test
  @DisplayName("SearchFilter with relationship filter retains it")
  void withRelationshipFilter_shouldRetainIt() {
    SearchFilter filter = new SearchFilter(
        "", Map.of(), List.of("children"));

    assertEquals(1, filter.relationshipFilters().size());
    assertEquals("children", filter.relationshipFilters().get(0));
  }

  @Test
  @DisplayName("SearchFilter is empty when all parts are empty")
  void isEmpty_shouldReturnTrueWhenAllEmpty() {
    SearchFilter filter = new SearchFilter("", Map.of(), List.of());

    assertTrue(filter.isEmpty());
  }

  @Test
  @DisplayName("SearchFilter maps are unmodifiable")
  void maps_shouldBeUnmodifiable() {
    Map<String, String> attrs = new java.util.HashMap<>();
    attrs.put("$Color", "red");
    SearchFilter filter = new SearchFilter("text", attrs, List.of());

    // Modifying original should not affect the filter
    attrs.put("$Badge", "star");
    assertEquals(1, filter.attributeFilters().size());
  }
}
