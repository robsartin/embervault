package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Represents one entry in a breadcrumb trail.
 *
 * @param noteId      the id of the note this entry represents
 * @param displayName the human-readable label to show in the breadcrumb
 */
public record BreadcrumbEntry(UUID noteId, String displayName) {
}
