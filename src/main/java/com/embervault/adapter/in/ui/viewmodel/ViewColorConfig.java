package com.embervault.adapter.in.ui.viewmodel;

import com.embervault.domain.ColorScheme;
import com.embervault.domain.TextContrastUtil;

/**
 * Color configuration for views, derived from a domain ColorScheme.
 *
 * <p>This is a ViewModel-layer value object that carries scheme colors
 * as simple hex strings so that view controllers do not depend on the
 * domain layer directly (per ADR-0013).</p>
 *
 * @param canvasBackground  hex color for canvas backgrounds
 * @param panelBackground   hex color for panel backgrounds
 * @param textColor         hex color for primary UI text
 * @param secondaryTextColor hex color for secondary text
 * @param borderColor       hex color for note borders
 * @param selectionColor    hex color for selection highlights
 * @param toolbarBackground hex color for toolbar areas
 * @param accentColor       hex color for accent/interactive elements
 */
public record ViewColorConfig(
        String canvasBackground,
        String panelBackground,
        String textColor,
        String secondaryTextColor,
        String borderColor,
        String selectionColor,
        String toolbarBackground,
        String accentColor
) {

    /**
     * Creates a ViewColorConfig from a domain ColorScheme.
     *
     * @param scheme the color scheme
     * @return the view color config
     */
    public static ViewColorConfig fromScheme(ColorScheme scheme) {
        return new ViewColorConfig(
                scheme.canvasBackground(),
                scheme.panelBackground(),
                scheme.textColor(),
                scheme.secondaryTextColor(),
                scheme.borderColor(),
                scheme.selectionColor(),
                scheme.toolbarBackground(),
                scheme.accentColor());
    }

    /**
     * Returns "#000000" or "#FFFFFF" for readable text on the given
     * background hex color.
     *
     * @param backgroundHex hex color string
     * @return contrast text color hex
     */
    public static String contrastTextColor(String backgroundHex) {
        return TextContrastUtil.contrastTextColor(backgroundHex);
    }
}
