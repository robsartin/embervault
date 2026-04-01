package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for AppState centralized data-change notification.
 */
class AppStateTest {

  private AppState appState;

  @BeforeEach
  void setUp() {
    appState = new AppState();
  }

  @Test
  @DisplayName("new AppState has zero data version")
  void newAppState_hasZeroDataVersion() {
    assertEquals(0, appState.getDataVersion());
  }

  @Test
  @DisplayName("notifyDataChanged increments version")
  void notifyDataChanged_incrementsVersion() {
    appState.notifyDataChanged();

    assertEquals(1, appState.getDataVersion());
  }

  @Test
  @DisplayName("multiple notifyDataChanged calls increment each time")
  void notifyDataChanged_incrementsEachTime() {
    appState.notifyDataChanged();
    appState.notifyDataChanged();
    appState.notifyDataChanged();

    assertEquals(3, appState.getDataVersion());
  }

  @Test
  @DisplayName("dataVersionProperty notifies listeners on change")
  void dataVersionProperty_notifiesListenersOnChange() {
    boolean[] notified = {false};
    appState.dataVersionProperty().addListener(
        (obs, oldVal, newVal) -> notified[0] = true);

    appState.notifyDataChanged();

    assertTrue(notified[0]);
  }

  @Test
  @DisplayName("dataVersionProperty listener receives correct old and new values")
  void dataVersionProperty_receivesCorrectValues() {
    int[] capturedOld = {-1};
    int[] capturedNew = {-1};
    appState.dataVersionProperty().addListener(
        (obs, oldVal, newVal) -> {
          capturedOld[0] = oldVal.intValue();
          capturedNew[0] = newVal.intValue();
        });

    appState.notifyDataChanged();

    assertEquals(0, capturedOld[0]);
    assertEquals(1, capturedNew[0]);
  }
}
