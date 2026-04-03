package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchQueryParserTest {

    @Test
    @DisplayName("parse plain text returns text-only filter")
    void parsePlainText_shouldReturnTextOnlyFilter() {
        SearchFilter filter = SearchQueryParser.parse("meeting notes");

        assertEquals("meeting notes", filter.textQuery());
        assertTrue(filter.attributeFilters().isEmpty());
        assertTrue(filter.relationshipFilters().isEmpty());
    }

    @Test
    @DisplayName("parse null returns empty filter")
    void parseNull_shouldReturnEmptyFilter() {
        SearchFilter filter = SearchQueryParser.parse(null);

        assertTrue(filter.isEmpty());
    }

    @Test
    @DisplayName("parse blank returns empty filter")
    void parseBlank_shouldReturnEmptyFilter() {
        SearchFilter filter = SearchQueryParser.parse("   ");

        assertTrue(filter.isEmpty());
    }

    @Test
    @DisplayName("parse color:red returns attribute filter for $Color")
    void parseColorFilter_shouldReturnAttributeFilter() {
        SearchFilter filter = SearchQueryParser.parse("color:red");

        assertTrue(filter.textQuery().isEmpty());
        assertEquals("red",
                filter.attributeFilters().get(Attributes.COLOR));
    }

    @Test
    @DisplayName("parse badge:star returns attribute filter for $Badge")
    void parseBadgeFilter_shouldReturnAttributeFilter() {
        SearchFilter filter = SearchQueryParser.parse("badge:star");

        assertEquals("star",
                filter.attributeFilters().get(Attributes.BADGE));
    }

    @Test
    @DisplayName("parse checked:true returns attribute filter")
    void parseCheckedFilter_shouldReturnAttributeFilter() {
        SearchFilter filter = SearchQueryParser.parse("checked:true");

        assertEquals("true",
                filter.attributeFilters().get(Attributes.CHECKED));
    }

    @Test
    @DisplayName("parse mixed text and filters separates them")
    void parseMixedInput_shouldSeparateTextAndFilters() {
        SearchFilter filter = SearchQueryParser.parse(
                "meeting color:red important");

        assertEquals("meeting important", filter.textQuery());
        assertEquals("red",
                filter.attributeFilters().get(Attributes.COLOR));
    }

    @Test
    @DisplayName("parse has:children returns relationship filter")
    void parseHasChildren_shouldReturnRelationshipFilter() {
        SearchFilter filter = SearchQueryParser.parse("has:children");

        assertTrue(filter.textQuery().isEmpty());
        assertEquals(1, filter.relationshipFilters().size());
        assertEquals("children",
                filter.relationshipFilters().get(0));
    }

    @Test
    @DisplayName("parse is case-insensitive for filter keys")
    void parseFilterKeys_shouldBeCaseInsensitive() {
        SearchFilter filter = SearchQueryParser.parse("Color:blue");

        assertEquals("blue",
                filter.attributeFilters().get(Attributes.COLOR));
    }

    @Test
    @DisplayName("parse name:foo returns attribute filter for $Name")
    void parseNameFilter_shouldReturnAttributeFilter() {
        SearchFilter filter = SearchQueryParser.parse("name:foo");

        assertEquals("foo",
                filter.attributeFilters().get(Attributes.NAME));
    }

    @Test
    @DisplayName("parse multiple filters retains all")
    void parseMultipleFilters_shouldRetainAll() {
        SearchFilter filter = SearchQueryParser.parse(
                "color:red badge:star");

        assertEquals("red",
                filter.attributeFilters().get(Attributes.COLOR));
        assertEquals("star",
                filter.attributeFilters().get(Attributes.BADGE));
    }

    @Test
    @DisplayName("parse shape:circle returns attribute filter")
    void parseShapeFilter_shouldReturnAttributeFilter() {
        SearchFilter filter = SearchQueryParser.parse("shape:circle");

        assertEquals("circle",
                filter.attributeFilters().get(Attributes.SHAPE));
    }
}
