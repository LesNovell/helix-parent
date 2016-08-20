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

import java.util.Observable;

/**
 * Property holds the state, name, and values of a configuration property
 */
public class Property extends Observable {
    private String name;
    private String value;
    private String unresolvedValue;

    /**
     * Create a Property object
     *
     * @param name Name of the property
     * @param value Value of the property (Already resolved and decrypted value)
     * @param unresolvedValue Value of the property before resolving (still encrypted)
     */
    public Property(String name, String value, String unresolvedValue) {
        this.name = name;
        this.value = value;
        this.unresolvedValue = unresolvedValue;
    }

    /**
     * @return The property name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The property's value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set a new resolved value, marking the observable as changed if it has changed
     * @param value The non-NULL resolved value
     */
    public void setValue(String value) {
        if (!value.equals(this.value)) {
            this.value = value;
            setChanged();
        }
    }

    /**
     * @return The unresolved value
     */
    public String getUnresolvedValue() {
        return unresolvedValue;
    }

    /**
     * Set a new unresolved value, marking the observable as changed if it has changed
     * @param unresolvedValue The non-NULL unresolved value
     */
    public void setUnresolvedValue(String unresolvedValue) {
        if (!unresolvedValue.equals(this.unresolvedValue)) {
            this.unresolvedValue = unresolvedValue;
            setChanged();
        }
    }


    /**
     * Attempt to parse the value of this property as an int
     *
     * @return The parsed int
     * @throws NumberFormatException if the value is not an int
     */
    public int asInt() {
        return Integer.parseInt(value);
    }

    /**
     * Attempt to parse the value of this property as a long
     *
     * @return The parsed long
     * @throws NumberFormatException if the value is not a long
     */
    public long asLong() {
        return Long.parseLong(value);
    }
}
