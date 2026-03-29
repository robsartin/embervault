package com.embervault.domain;

/**
 * A document-level color scheme that controls UI appearance.
 *
 * <p>Note {@code $Color} attributes are user data and render faithfully
 * regardless of the active scheme. The scheme controls everything around
 * the notes: canvas backgrounds, panels, toolbars, borders, and text.</p>
 *
 * @param name              human-readable scheme name
 * @param canvasBackground  hex color for Map/Treemap canvas background
 * @param panelBackground   hex color for Outline/Browser panel background
 * @param textColor         hex color for primary UI text
 * @param secondaryTextColor hex color for subtitles, labels, hints
 * @param borderColor       hex color for note borders and separators
 * @param selectionColor    hex color for selected note highlight
 * @param toolbarBackground hex color for zoom toolbar and search bar
 * @param accentColor       hex color for buttons, links, interactive elements
 */
public record ColorScheme(
        String name,
        String canvasBackground,
        String panelBackground,
        String textColor,
        String secondaryTextColor,
        String borderColor,
        String selectionColor,
        String toolbarBackground,
        String accentColor
) {
}
