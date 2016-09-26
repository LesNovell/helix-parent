
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

package io.helixservice.feature.configuration;

import java.util.List;

/**
 * Listener interface for ConfigProperties change events
 */
@FunctionalInterface
public interface ConfigPropertiesChangedListener {
    void propertiesChanged(ConfigProperties configProperties,
            List<String> newProperties, List<String> changedProperties, List<String> deletedProperties);
}
