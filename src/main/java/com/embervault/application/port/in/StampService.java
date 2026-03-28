package com.embervault.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Stamp;

/**
 * Inbound port defining stamp management use cases.
 */
public interface StampService {

    /**
     * Creates a new stamp with the given name and action.
     *
     * @param name   the stamp name
     * @param action the action expression
     * @return the created stamp
     */
    Stamp createStamp(String name, String action);

    /**
     * Deletes the stamp with the given id.
     *
     * @param id the stamp id
     */
    void deleteStamp(UUID id);

    /**
     * Returns all stamps.
     *
     * @return the list of all stamps
     */
    List<Stamp> getAllStamps();

    /**
     * Gets a stamp by its id.
     *
     * @param id the stamp id
     * @return an optional containing the stamp, or empty
     */
    Optional<Stamp> getStamp(UUID id);

    /**
     * Applies a stamp to a note by parsing the stamp's action and setting
     * the corresponding attribute on the note.
     *
     * @param stampId the stamp id
     * @param noteId  the note id
     */
    void applyStamp(UUID stampId, UUID noteId);
}
