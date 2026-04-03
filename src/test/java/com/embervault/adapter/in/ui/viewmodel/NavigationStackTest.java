package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NavigationStackTest {

    private NavigationStack stack;

    @BeforeEach
    void setUp() {
        stack = new NavigationStack();
    }

    @Test
    @DisplayName("currentId is null initially")
    void currentId_shouldBeNullInitially() {
        assertNull(stack.getCurrentId());
    }

    @Test
    @DisplayName("canNavigateBack is false initially")
    void canNavigateBack_shouldBeFalseInitially() {
        assertFalse(stack.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("push sets currentId and enables back navigation")
    void push_shouldSetCurrentIdAndEnableBack() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        stack.setCurrentId(first);

        stack.push(second);

        assertEquals(second, stack.getCurrentId());
        assertTrue(stack.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("pop returns to previous id")
    void pop_shouldReturnToPreviousId() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        stack.setCurrentId(first);
        stack.push(second);

        UUID popped = stack.pop();

        assertEquals(first, popped);
        assertEquals(first, stack.getCurrentId());
    }

    @Test
    @DisplayName("canNavigateBack is false after popping all entries")
    void canNavigateBack_shouldBeFalseAfterPoppingAll() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        stack.setCurrentId(first);
        stack.push(second);

        stack.pop();

        assertFalse(stack.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("pop returns null when history is empty")
    void pop_shouldReturnNullWhenEmpty() {
        assertNull(stack.pop());
    }

    @Test
    @DisplayName("pop does not change currentId when history is empty")
    void pop_shouldNotChangeCurrentIdWhenEmpty() {
        UUID id = UUID.randomUUID();
        stack.setCurrentId(id);

        stack.pop();

        assertEquals(id, stack.getCurrentId());
    }

    @Test
    @DisplayName("multiple push/pop cycles work correctly")
    void multiplePushPop_shouldWorkCorrectly() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        stack.setCurrentId(first);

        stack.push(second);
        stack.push(third);

        assertTrue(stack.canNavigateBackProperty().get());
        assertEquals(third, stack.getCurrentId());

        stack.pop();
        assertEquals(second, stack.getCurrentId());
        assertTrue(stack.canNavigateBackProperty().get());

        stack.pop();
        assertEquals(first, stack.getCurrentId());
        assertFalse(stack.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("isAtRoot returns true when history is empty")
    void isAtRoot_shouldBeTrueWhenHistoryIsEmpty() {
        assertTrue(stack.isAtRoot());
    }

    @Test
    @DisplayName("isAtRoot returns false after push")
    void isAtRoot_shouldBeFalseAfterPush() {
        UUID first = UUID.randomUUID();
        stack.setCurrentId(first);
        stack.push(UUID.randomUUID());

        assertFalse(stack.isAtRoot());
    }

    @Test
    @DisplayName("isAtRoot returns true after popping all entries")
    void isAtRoot_shouldBeTrueAfterPoppingAll() {
        UUID first = UUID.randomUUID();
        stack.setCurrentId(first);
        stack.push(UUID.randomUUID());

        stack.pop();

        assertTrue(stack.isAtRoot());
    }

    @Test
    @DisplayName("setCurrentId does not affect navigation history")
    void setCurrentId_shouldNotAffectHistory() {
        UUID first = UUID.randomUUID();
        stack.setCurrentId(first);

        assertFalse(stack.canNavigateBackProperty().get());
        assertTrue(stack.isAtRoot());
    }

    @Test
    @DisplayName("canNavigateBack property is observable and updates on push")
    void canNavigateBackProperty_shouldUpdateOnPush() {
        boolean[] observed = {false};
        stack.canNavigateBackProperty().addListener(
                (obs, oldVal, newVal) -> observed[0] = newVal);

        UUID first = UUID.randomUUID();
        stack.setCurrentId(first);
        stack.push(UUID.randomUUID());

        assertTrue(observed[0]);
    }

    @Test
    @DisplayName("getBreadcrumbs returns empty list when no current id is set")
    void getBreadcrumbs_shouldBeEmptyWhenNoCurrentId() {
        assertTrue(stack.getBreadcrumbs().isEmpty());
    }

    @Test
    @DisplayName("getBreadcrumbs returns single entry for root note")
    void getBreadcrumbs_shouldReturnSingleEntryForRoot() {
        UUID root = UUID.randomUUID();
        stack.setCurrentId(root, "Root");

        assertEquals(1, stack.getBreadcrumbs().size());
        assertEquals(root, stack.getBreadcrumbs().get(0).noteId());
        assertEquals("Root", stack.getBreadcrumbs().get(0).displayName());
    }

    @Test
    @DisplayName("getBreadcrumbs returns full path after drill-down")
    void getBreadcrumbs_shouldReturnFullPathAfterDrillDown() {
        UUID root = UUID.randomUUID();
        UUID child = UUID.randomUUID();
        UUID grandchild = UUID.randomUUID();
        stack.setCurrentId(root, "Root");
        stack.push(child, "Child");
        stack.push(grandchild, "Grandchild");

        assertEquals(3, stack.getBreadcrumbs().size());
        assertEquals("Root", stack.getBreadcrumbs().get(0).displayName());
        assertEquals("Child", stack.getBreadcrumbs().get(1).displayName());
        assertEquals("Grandchild", stack.getBreadcrumbs().get(2).displayName());
    }

    @Test
    @DisplayName("getBreadcrumbs shrinks after pop")
    void getBreadcrumbs_shouldShrinkAfterPop() {
        UUID root = UUID.randomUUID();
        UUID child = UUID.randomUUID();
        stack.setCurrentId(root, "Root");
        stack.push(child, "Child");

        stack.pop();

        assertEquals(1, stack.getBreadcrumbs().size());
        assertEquals("Root", stack.getBreadcrumbs().get(0).displayName());
    }

    @Test
    @DisplayName("canNavigateBack property is observable and updates on pop")
    void canNavigateBackProperty_shouldUpdateOnPop() {
        UUID first = UUID.randomUUID();
        stack.setCurrentId(first);
        stack.push(UUID.randomUUID());

        boolean[] observed = {true};
        stack.canNavigateBackProperty().addListener(
                (obs, oldVal, newVal) -> observed[0] = newVal);

        stack.pop();

        assertFalse(observed[0]);
    }
}
