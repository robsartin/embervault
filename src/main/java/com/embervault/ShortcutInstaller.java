package com.embervault;

import com.embervault.adapter.in.ui.viewmodel.ShortcutAction;
import com.embervault.adapter.in.ui.viewmodel.ShortcutRegistry;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs {@link ShortcutRegistry} actions as scene-level key event filters.
 *
 * <p>Translates the registry's string-based key combinations into JavaFX
 * {@link KeyCombination} objects and adds a single event filter to the
 * scene that dispatches matching key events to the registered actions.</p>
 */
final class ShortcutInstaller {

    private static final Logger LOG =
            LoggerFactory.getLogger(ShortcutInstaller.class);

    private ShortcutInstaller() { }

    /**
     * Installs a key event filter on the given scene that routes key
     * presses to matching actions in the registry.
     *
     * @param scene    the scene to install the filter on
     * @param registry the shortcut registry to consult
     */
    static void install(Scene scene, ShortcutRegistry registry) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            for (ShortcutAction action : registry.getAll()) {
                try {
                    KeyCombination combo =
                            KeyCombination.valueOf(
                                    action.keyCombination());
                    if (combo.match(event)) {
                        action.action().run();
                        event.consume();
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    LOG.warn("Invalid key combination: {}",
                            action.keyCombination(), e);
                }
            }
        });
    }
}
