/*
 *  Copyright (c) 2016 Les Novell
 *  ------------------------------------------------------
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 */

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
    Multimap<String, Component> getRegistrationMap();

    /**
     * Find a list of components by type name
     *
     * @param componentType Type name of the components to find
     * @param <T>           Resulting class of the components expected
     *
     * @return A collection of the components found, or empty list of none is found.
     */
    <T extends Component> Collection<T> findByType(String componentType);

    /**
     * Finds the a registered component by type name.
     * This method guarantees that the last (most recently) registered component will be returned.
     *
     * @param componentType Type name of the component to find
     * @param defaultValue  Default component if none is found (null is okay)
     * @param <T>           Resulting class of the component expected
     *
     * @return The found component, or the default component if no component was found.
     */
    <T extends Component> T findByType(String componentType, T defaultValue);
}
