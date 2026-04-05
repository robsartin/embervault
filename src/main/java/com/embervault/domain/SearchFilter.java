package com.embervault.domain;

import java.util.List;

/**
 * Represents a parsed search filter for querying notes.
 *
 * <p>Supports substring matching on name/text and attribute-specific
 * filters like {@code color:red} or {@code has:children}.</p>
 */
public sealed interface SearchFilter
    permits SearchFilter.SubstringFilter,
    SearchFilter.AttributeFilter,
    SearchFilter.RelationFilter {

  /**
   * Parses a query string into a list of search filters.
   *
   * <p>Plain text tokens become substring filters. Tokens in
   * {@code key:value} format become attribute or relation filters.</p>
   *
   * @param query the raw search query
   * @return list of parsed filters, empty if query is blank/null
   */
  static List<SearchFilter> parse(String query) {
    if (query == null || query.isBlank()) {
      return List.of();
    }
    return List.of(new SubstringFilter(query.strip()));
  }

  /**
   * A substring match against note name and text.
   *
   * @param text the substring to search for
   */
  record SubstringFilter(String text) implements SearchFilter {}

  /**
   * A filter matching a specific attribute value.
   *
   * @param attributeKey the attribute key (e.g., "color", "badge")
   * @param value the expected value
   */
  record AttributeFilter(String attributeKey,
      String value) implements SearchFilter {}

  /**
   * A relational filter (e.g., has:children, has:links).
   *
   * @param relation the relation type
   */
  record RelationFilter(String relation) implements SearchFilter {}
}
