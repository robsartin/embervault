package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a note is moved (indent, outdent, reorder).
 *
 * @param noteId the ID of the moved note
 */
public record NoteMovedEvent(UUID noteId) {
}
