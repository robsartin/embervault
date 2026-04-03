package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a note is deleted.
 *
 * <p>Allows ViewModels to react to note deletion without direct coupling
 * to the ViewModel that performed the deletion.</p>
 *
 * @param noteId the ID of the deleted note
 */
public record NoteDeletedEvent(UUID noteId) {
}
