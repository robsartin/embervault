package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a note's content or attributes are updated.
 *
 * @param noteId the ID of the updated note
 */
public record NoteUpdatedEvent(UUID noteId) {
}
