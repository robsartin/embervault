package com.embervault.domain;

import java.util.List;
import java.util.Map;

/**
 * A parsed search query containing text terms and attribute filters.
 *
 * <p>Represents the decomposition of a search string like
 * {@code "meeting color:red has:children"} into its constituent parts:
 * a plain text query, attribute key-value filters, and relationship
 * filters.</p>
 *
 * @param textQuery           the plain text search terms
 * @param attributeFilters    attribute name to expected value mappings
 * @param relationshipFilters relationship checks (e.g., "children")
 */
public record SearchFilter(
    String textQuery,
    Map<String, String> attributeFilters,
    List<String> relationshipFilters) {

  /**
   * Constructs a SearchFilter with defensive copies of collections.
   */
  public SearchFilter {
    textQuery = textQuery == null ? "" : textQuery;
    attributeFilters = attributeFilters == null
        ? Map.of() : Map.copyOf(attributeFilters);
    relationshipFilters = relationshipFilters == null
        ? List.of() : List.copyOf(relationshipFilters);
  }

  /**
   * Returns true if this filter has no text query, attribute filters,
   * or relationship filters.
   *
   * @return true if empty
   */
  public boolean isEmpty() {
    return textQuery.isBlank()
        && attributeFilters.isEmpty()
        && relationshipFilters.isEmpty();
  }
}
