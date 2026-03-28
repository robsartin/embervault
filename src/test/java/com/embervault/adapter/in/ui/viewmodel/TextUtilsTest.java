package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TextUtilsTest {

    @Test
    @DisplayName("truncate returns null for null input")
    void truncate_shouldReturnNullForNullInput() {
        assertNull(TextUtils.truncate(null, 20));
    }

    @Test
    @DisplayName("truncate returns empty string unchanged")
    void truncate_shouldReturnEmptyStringUnchanged() {
        assertEquals("", TextUtils.truncate("", 20));
    }

    @Test
    @DisplayName("truncate returns short string unchanged")
    void truncate_shouldReturnShortStringUnchanged() {
        assertEquals("Hello", TextUtils.truncate("Hello", 20));
    }

    @Test
    @DisplayName("truncate returns exact-length string unchanged")
    void truncate_shouldReturnExactLengthStringUnchanged() {
        String text = "12345678901234567890"; // exactly 20 chars
        assertEquals(text, TextUtils.truncate(text, 20));
    }

    @Test
    @DisplayName("truncate appends ellipsis when over max length")
    void truncate_shouldAppendEllipsisWhenOverMaxLength() {
        String text = "123456789012345678901"; // 21 chars
        assertEquals("12345678901234567890\u2026", TextUtils.truncate(text, 20));
    }

    @Test
    @DisplayName("truncate works with max length of 1")
    void truncate_shouldWorkWithMaxLengthOfOne() {
        assertEquals("a\u2026", TextUtils.truncate("abc", 1));
    }
}
