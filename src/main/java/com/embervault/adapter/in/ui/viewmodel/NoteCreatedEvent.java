package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a note is created.
 *
 * @param noteId the ID of the created note
 */
public record NoteCreatedEvent(UUID noteId) {
}
