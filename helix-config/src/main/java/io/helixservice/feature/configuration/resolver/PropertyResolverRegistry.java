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

package io.helixservice.feature.configuration.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Singleton registry for PropertyResolvers. It is not necessary
 * to use this registry directly, since the ConfigurationFeature
 * will look for and find all PropertyResolvers registered by
 * Bootstrap-scope features.
 */
public class PropertyResolverRegistry implements Supplier<PropertyResolver[]> {
    private final static PropertyResolverRegistry INSTANCE = new PropertyResolverRegistry();

    private List<PropertyResolver> resolvers = new ArrayList<>();

    private PropertyResolverRegistry() {
    }

    /**
     * @return The PropertyResolverRegistry singleton
     */
    public static PropertyResolverRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Add a PropertyResolver to the registry
     * @param propertyResolver PropertyResolver to add
     */
    public static void add(PropertyResolver propertyResolver) {
        INSTANCE.resolvers.add(propertyResolver);
    }

    /**
     * Set all the PropertyResolver, removing any existing ones.
     * @param propertyResolvers List of all PropertyResolvers to register
     */
    public static void set(Collection<PropertyResolver> propertyResolvers) {
        INSTANCE.resolvers.clear();
        INSTANCE.resolvers.addAll(propertyResolvers);
    }

    /**
     * Get all PropertyResolvers, in order of their registry
     * @return Array of PropertyResolvers
     */
    @Override
    public PropertyResolver[] get() {
        return resolvers.toArray(new PropertyResolver[resolvers.size()]);
    }

}
