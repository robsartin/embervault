package com.embervault.domain;

/**
 * Constants for Tinderbox system attribute names.
 *
 * <p>Centralizes attribute name strings to avoid typos and enable
 * refactoring across the codebase.</p>
 */
public final class Attributes {

    // Identity & Structure
    public static final String NAME = "$Name";
    public static final String TEXT = "$Text";
    public static final String SUBTITLE = "$Subtitle";
    public static final String CAPTION = "$Caption";
    public static final String CONTAINER = "$Container";
    public static final String OUTLINE_ORDER = "$OutlineOrder";
    public static final String IS_PROTOTYPE = "$IsPrototype";
    public static final String PROTOTYPE = "$Prototype";

    // Appearance
    public static final String COLOR = "$Color";
    public static final String COLOR2 = "$Color2";
    public static final String BORDER_COLOR = "$BorderColor";
    public static final String XPOS = "$Xpos";
    public static final String YPOS = "$Ypos";
    public static final String WIDTH = "$Width";
    public static final String HEIGHT = "$Height";
    public static final String SHAPE = "$Shape";
    public static final String BADGE = "$Badge";

    // Dates
    public static final String CREATED = "$Created";
    public static final String MODIFIED = "$Modified";
    public static final String START_DATE = "$StartDate";
    public static final String END_DATE = "$EndDate";
    public static final String DUE_DATE = "$DueDate";

    // Display
    public static final String DISPLAYED_ATTRIBUTES = "$DisplayedAttributes";
    public static final String KEY_ATTRIBUTES = "$KeyAttributes";

    // Flags & State
    public static final String CHECKED = "$Checked";
    public static final String FLAGS = "$Flags";
    public static final String LOCK = "$Lock";

    // Web
    public static final String URL = "$URL";

    private Attributes() {
        // utility class
    }
}
