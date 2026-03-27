package com.embervault.application.port.in;

import com.embervault.domain.Project;

/**
 * Inbound port defining the project management use cases.
 */
public interface ProjectService {

    /**
     * Creates a new empty project with a default root note.
     */
    Project createEmptyProject();
}
