package com.embervault.adapter.in.ui.viewmodel;

import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.BadgeRegistry;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;

/**
 * Shared utility for extracting display properties from notes.
 *
 * <p>Consolidates the duplicated badge resolution and color extraction
 * logic that was previously repeated across all ViewModels.</p>
 */
final class NoteDisplayHelper {

    static final String DEFAULT_COLOR_HEX = "#808080";

    private NoteDisplayHelper() {
        // utility class
    }

    static String resolveColorHex(Note note) {
        return note.getAttribute(Attributes.COLOR)
                .map(v -> ((AttributeValue.ColorValue) v).value())
                .map(TbxColor::toHex)
                .orElse(DEFAULT_COLOR_HEX);
    }

    static String resolveBadge(Note note) {
        return note.getAttribute(Attributes.BADGE)
                .map(v -> ((AttributeValue.StringValue) v).value())
                .flatMap(BadgeRegistry::getBadgeSymbol)
                .orElse("");
    }

    static double resolveNumber(Note note, String attr, double defaultVal) {
        return note.getAttribute(attr)
                .map(v -> ((AttributeValue.NumberValue) v).value())
                .orElse(defaultVal);
    }

}
