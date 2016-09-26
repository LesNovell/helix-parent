
/*
 * @author Les Novell
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 */

package io.helixservice.core.component;

import com.google.common.collect.Multimap;

import java.util.Collection;

/**
 * Registry for Helix Components
 */
public interface ComponentRegistry {
    /**
     * Returns the full map of registered components by type name.
     * <br>
     * Do not modify the map, it is only for finding components.
     *
     * @return The registered components map.
     */
    Multimap<String, Component> getComponentMap();

    /**
     * Register one or more components
     *
     * @param component Components to register
     * @return This component registry
     */
    ComponentRegistry register(Component... component);

    /**
     * Register all of the components from another
     * registry, into the current one.
     *
     * @param registry Registry to copy the components from
     * @return This component registry
     */
    ComponentRegistry registerAllFrom(ComponentRegistry registry);

    /**
     * Returns all registered components
     *
     * @return Collection of all registered components
     */
    Collection<Component> findAllComponents();

    /**
     * Find a list of components by type name
     *
     * @param componentType Type name of the components to find
     * @param <T>           Resulting class of the components expected
     *
     * @return A collection of the components found, or empty list of none is found.
     */
    <T extends Component> Collection<T> findComponentByType(String componentType);

    /**
     * Finds the a single registered component by type name.
     * This method guarantees that the last (most recently) registered component will be returned.
     *
     * @param componentType Type name of the component to find
     * @param defaultValue  Default component if none is found (null is okay)
     * @param <T>           Resulting class of the component expected
     *
     * @return The found component, or the default component if no component was found.
     */
    <T extends Component> T findComponentByType(String componentType, T defaultValue);
}
