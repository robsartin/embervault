package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.embervault.application.port.in.ProjectService;
import com.embervault.domain.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectServiceImplTest {

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectServiceImpl();
    }

    @Test
    @DisplayName("createEmptyProject() returns a project with Untitled name")
    void createEmptyProject_shouldReturnUntitledProject() {
        Project project = service.createEmptyProject();

        assertNotNull(project);
        assertEquals("Untitled", project.getName());
    }

    @Test
    @DisplayName("createEmptyProject() returns a project with a root note")
    void createEmptyProject_shouldHaveRootNote() {
        Project project = service.createEmptyProject();

        assertNotNull(project.getRootNote());
        assertEquals("Untitled", project.getRootNote().getTitle());
    }

    @Test
    @DisplayName("createEmptyProject() returns a project with a valid id")
    void createEmptyProject_shouldHaveValidId() {
        Project project = service.createEmptyProject();

        assertNotNull(project.getId());
    }
}
