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

package io.helixservice.feature.configuration.provider;

import java.util.List;

@FunctionalInterface
public interface ConfigProviderPropertiesChangedListener {
    /**
     * Called when a ConfigProvider's properties have been changed, added, or removed.
     *
     * @param newProperties New property names
     * @param changedProperties Changed property names
     * @param deletedProperties Deleted property names
     */
    void configChanged(List<String> newProperties, List<String> changedProperties, List<String> deletedProperties);
}
