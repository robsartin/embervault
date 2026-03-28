package com.embervault.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Stamp;

/**
 * Outbound port for persisting and retrieving stamps.
 */
public interface StampRepository {

    /**
     * Saves the given stamp, creating or replacing it by id.
     *
     * @param stamp the stamp to save
     * @return the saved stamp
     */
    Stamp save(Stamp stamp);

    /**
     * Deletes the stamp with the given id.
     *
     * @param id the stamp id
     */
    void delete(UUID id);

    /**
     * Finds a stamp by its unique identifier.
     *
     * @param id the stamp id
     * @return an optional containing the stamp, or empty
     */
    Optional<Stamp> findById(UUID id);

    /**
     * Returns all stamps.
     *
     * @return the list of all stamps
     */
    List<Stamp> findAll();

    /**
     * Finds a stamp by its name.
     *
     * @param name the stamp name
     * @return an optional containing the stamp, or empty
     */
    Optional<Stamp> findByName(String name);
}
