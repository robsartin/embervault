package com.embervault;

import java.util.Map;

/**
 * Registry that maps each {@link ViewType} to its {@link ViewFactory}.
 *
 * <p>Replaces the switch statement in {@link ViewPaneContext} with a
 * lookup-based dispatch, making it easy to add new view types without
 * modifying the switching logic.</p>
 */
public final class ViewFactoryRegistry {

    private final Map<ViewType, ViewFactory> factories;

    /**
     * Creates a registry pre-populated with all known view factories.
     */
    public ViewFactoryRegistry() {
        factories = Map.of(
                ViewType.MAP, new MapViewFactory(),
                ViewType.OUTLINE, new OutlineViewFactory(),
                ViewType.TREEMAP, new TreemapViewFactory(),
                ViewType.HYPERBOLIC, new HyperbolicViewFactory(),
                ViewType.BROWSER,
                new AttributeBrowserViewFactory());
    }

    /**
     * Returns the factory for the given view type.
     *
     * @param type the view type
     * @return the corresponding factory
     * @throws IllegalArgumentException if no factory is registered
     */
    public ViewFactory getFactory(ViewType type) {
        ViewFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException(
                    "No factory registered for " + type);
        }
        return factory;
    }
}
