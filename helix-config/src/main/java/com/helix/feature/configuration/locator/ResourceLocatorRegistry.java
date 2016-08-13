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

package com.helix.feature.configuration.locator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Singleton registry for resource locators.  It is not necessary
 * to use this registry directly, since the ConfigurationFeature
 * will look for and find all ResourceLocators registered by
 * Bootstrap-scope features.
 */
public class ResourceLocatorRegistry implements Supplier<ResourceLocator[]> {
    private final static ResourceLocatorRegistry INSTANCE = new ResourceLocatorRegistry();
    private List<ResourceLocator> resourceLocators = new ArrayList<>();

    private ResourceLocatorRegistry() {
    }

    /**
     * @return The ResourceLocatorRegistry singleton
     */
    public static ResourceLocatorRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Add a ResourceLocator to the registry
     * @param resourceLocator ResourceLocator to add
     */
    public static void add(ResourceLocator resourceLocator) {
        INSTANCE.resourceLocators.add(resourceLocator);
    }

    /**
     * Set all the ResourceLocators, removing any existing ones.
     * @param resourceLocators List of all ResourceLocators to register
     */
    public static void set(Collection<ResourceLocator> resourceLocators) {
        INSTANCE.resourceLocators.clear();
        INSTANCE.resourceLocators.addAll(resourceLocators);
    }

    /**
     * Specifically add a ResourceLocator as the first.
     * @param resourceLocator ResourceLocator to insert first in the list.
     */
    public static void addAsFirst(ResourceLocator resourceLocator) {
        INSTANCE.resourceLocators.add(0, resourceLocator);
    }

    /**
     * Get all resource locators, in order of their registry
     * @return Array of ResourceLocators
     */
    @Override
    public ResourceLocator[] get() {
        return resourceLocators.toArray(new ResourceLocator[resourceLocators.size()]);
    }
}
