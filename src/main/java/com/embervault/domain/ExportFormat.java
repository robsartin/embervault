package com.embervault.domain;

/**
 * Supported export formats for project data.
 */
public enum ExportFormat {

    /** Full-fidelity JSON with notes, links, and stamps. */
    JSON,

    /** Markdown archive with one .md file per note. */
    MARKDOWN,

    /** OPML 2.0 outline format. */
    OPML
}
