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
package com.helix.core.component;

/**
 * Helix Component Interface
 */
public interface Component {
    /**
     * Each component must define it's type name.
     * The type is used for lookup in the component registry.
     * <p>
     * Example component types include "Controller", "ErrorHandler"
     * <p>
     * @return The type of component
     */
    String getComponentType();

    /**
     * Return a human readable short description of this component.
     * This will be used to log or describe each registered component.
     *
     * @return The component description; may be null
     */
    String getComponentDescription();

    Component[] EMPTY = new Component[0];
    /**
     * Return list of components contained within this component.
     * <p>
     * This is most useful if the component is a Builder or Factory which
     * produces additional components that need to be registered with ComponentRegistry.
     * <p>
     * This method must the same array of components each time;
     * Do not re-construct the contained components on each call.
     * <p>
     * This method will be automatically called by the ComponentRegistry
     * to register all of its contained components.
     * <p>
     * @return An array of contained components. Default is an empty array.
     */
    default Component[] getContainedComponents() {
        return EMPTY;
    }
}
