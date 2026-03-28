package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataChangeSupportTest {

    private DataChangeSupport support;

    @BeforeEach
    void setUp() {
        support = new DataChangeSupport();
    }

    @Test
    @DisplayName("notify does nothing when no callback is set")
    void notify_shouldDoNothingWhenNoCallback() {
        assertDoesNotThrow(() -> support.notifyDataChanged());
    }

    @Test
    @DisplayName("notify invokes the registered callback")
    void notify_shouldInvokeCallback() {
        int[] count = {0};
        support.setOnDataChanged(() -> count[0]++);

        support.notifyDataChanged();

        assertEquals(1, count[0]);
    }

    @Test
    @DisplayName("notify invokes the callback each time")
    void notify_shouldInvokeCallbackEachTime() {
        int[] count = {0};
        support.setOnDataChanged(() -> count[0]++);

        support.notifyDataChanged();
        support.notifyDataChanged();
        support.notifyDataChanged();

        assertEquals(3, count[0]);
    }

    @Test
    @DisplayName("setting callback to null clears it")
    void setOnDataChanged_null_shouldClearCallback() {
        int[] count = {0};
        support.setOnDataChanged(() -> count[0]++);
        support.setOnDataChanged(null);

        support.notifyDataChanged();

        assertEquals(0, count[0]);
    }

    @Test
    @DisplayName("replacing callback uses the new one")
    void setOnDataChanged_shouldReplaceCallback() {
        int[] countA = {0};
        int[] countB = {0};
        support.setOnDataChanged(() -> countA[0]++);
        support.setOnDataChanged(() -> countB[0]++);

        support.notifyDataChanged();

        assertEquals(0, countA[0]);
        assertEquals(1, countB[0]);
    }
}
