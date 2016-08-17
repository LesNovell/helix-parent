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

/**
 * Provides a default (no-op) implementation for property resolver
 */
public class DefaultPropertyResolver implements PropertyResolver {
    /** {@inheritDoc} */
    @Override
    public String resolve(String name, String value) {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean sensitive(String name, String value) {
        return value.startsWith("{cipher}") ||
                name.endsWith(".secret") ||
                name.endsWith(".password") ||
                name.endsWith(".key");
    }

    /** {@inheritDoc} */
    @Override
    public String getComponentDescription() {
        return "Default Property Resolver";
    }
}
