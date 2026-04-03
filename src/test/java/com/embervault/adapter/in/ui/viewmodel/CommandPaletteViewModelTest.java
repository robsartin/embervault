package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandPaletteViewModelTest {

    private ShortcutRegistry registry;
    private CommandPaletteViewModel viewModel;

    @BeforeEach
    void setUp() {
        registry = new ShortcutRegistry();
        registry.register("Shortcut+N", "New Note",
                "Create a new note", () -> { });
        registry.register("Shortcut+F", "Find",
                "Open search", () -> { });
        registry.register("Shortcut+S", "Save",
                "Save project", () -> { });
        viewModel = new CommandPaletteViewModel(registry);
    }

    @Test
    @DisplayName("initially not visible")
    void shouldBeHiddenInitially() {
        assertFalse(viewModel.visibleProperty().get());
    }

    @Test
    @DisplayName("show makes palette visible and lists all shortcuts")
    void show_shouldMakeVisibleAndListAll() {
        viewModel.show();

        assertTrue(viewModel.visibleProperty().get());
        assertEquals(3, viewModel.getFilteredActions().size());
    }

    @Test
    @DisplayName("hide clears visibility and query")
    void hide_shouldClearVisibilityAndQuery() {
        viewModel.show();
        viewModel.queryProperty().set("note");
        viewModel.hide();

        assertFalse(viewModel.visibleProperty().get());
        assertEquals("", viewModel.queryProperty().get());
    }

    @Test
    @DisplayName("query filters the action list")
    void query_shouldFilterActions() {
        viewModel.show();
        viewModel.queryProperty().set("note");

        assertEquals(1, viewModel.getFilteredActions().size());
        assertEquals("New Note",
                viewModel.getFilteredActions().get(0).name());
    }

    @Test
    @DisplayName("execute runs the selected action and hides palette")
    void execute_shouldRunActionAndHide() {
        boolean[] executed = {false};
        registry.register("Shortcut+T", "Test Action",
                "Test", () -> executed[0] = true);
        viewModel.show();
        viewModel.queryProperty().set("test");

        viewModel.executeSelected(
                viewModel.getFilteredActions().get(0));

        assertTrue(executed[0]);
        assertFalse(viewModel.visibleProperty().get());
    }
}
