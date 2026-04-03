package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a note is renamed.
 *
 * @param noteId   the ID of the renamed note
 * @param newTitle the new title
 */
public record NoteRenamedEvent(UUID noteId, String newTitle) {
}
