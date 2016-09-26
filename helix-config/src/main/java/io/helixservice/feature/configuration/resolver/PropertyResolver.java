
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

import io.helixservice.core.component.Component;

/**
 * Interface that all PropertyResolvers must implement.
 * <p>
 * A PropertyResolver has an opportunity to modify a
 * properties value.  This will support features such as
 * variable substitution or decryption.
 */
public interface PropertyResolver extends Component {
    String TYPE_NAME = "PropertyResolver";

    /**
     * @return "PropertyResolver" as a type name
     */
    @Override
    default String getComponentType() {
        return TYPE_NAME;
    }

    /**
     * Resolve a property's value
     *
     * @param name Name of the property
     * @param value Current value of the property, before resolving
     * @return New value of the property (may be the same as current value)
     */
    String resolve(String name, String value);

    /**
     * Determine if a property is sensitive, preventing logging
     *
     * @param name Name of the property
     * @param value Value of the property, before resolving
     * @return True if the property should never be displayed or logged
     */
    boolean sensitive(String name, String value);
}
