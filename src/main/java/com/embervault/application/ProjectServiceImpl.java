package com.embervault.application;

import com.embervault.application.port.in.ProjectService;
import com.embervault.domain.Project;

/**
 * Application service implementing project use cases.
 */
public final class ProjectServiceImpl implements ProjectService {

    @Override
    public Project createEmptyProject() {
        return Project.createEmpty();
    }
}
