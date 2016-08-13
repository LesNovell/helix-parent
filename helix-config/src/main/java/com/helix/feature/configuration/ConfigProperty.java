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

package com.helix.feature.configuration;

import com.helix.feature.configuration.provider.ConfigProviderPropertiesChangedListener;
import com.helix.feature.configuration.provider.ConfigProvider;
import com.helix.feature.configuration.provider.ConfigProviderException;
import com.helix.feature.configuration.provider.ConfigProviderFactory;
import com.helix.feature.configuration.provider.Property;

import java.util.List;

/**
 * Represents a single configuration property.
 * <p>
 * The property returned will be from the current environment's
 * configuration or the default configuration.
 */
public class ConfigProperty implements ConfigProviderPropertiesChangedListener {
    private ConfigProvider configProvider = ConfigProviderFactory.singleton();
    private ConfigPropertyChangedListener configPropertyChangedListener;

    private String propertyName;
    private String defaultValue;
    private Property property;

    /**
     * Load a property with the given name
     *
     * @param propertyName Name of property to load
     * @throws ConfigProviderException if the property does not exist
     */
    public ConfigProperty(String propertyName) {
        this.propertyName = propertyName;
        loadProperty();
        configProvider.addPropertiesChangedListener(this);
    }

    /**
     * Load a property with the given name and default value
     *
     * @param propertyName Name of the property to load
     * @param defaultValue Default value of this property if it does not exist
     */
    public ConfigProperty(String propertyName, String defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
        loadProperty();
        configProvider.addPropertiesChangedListener(this);
    }

    private synchronized void loadProperty() {
        if (property == null) {
            property = configProvider.propertyByName(propertyName, defaultValue);

            if (property == null) {
                throw new ConfigProviderException("Property not found propertyName=" + propertyName);
            }
        }
    }

    /**
     * @return The value of this property as a string
     */
    public String getValue() {
        return property.getValue();
    }

    /**
     * @return The value of this property as an int value
     * @throws NumberFormatException if the value is not an int
     */
    public int asInt() {
        return property.asInt();
    }

    /**
     * @return The value of this property as a long value
     * @throws NumberFormatException if the value is not a long
     */
    public long asLong() {
        return property.asLong();
    }

    /**
     * @return True if the string value is "true" (case insensitive)
     */
    public boolean isTrue() {
        return "true".equalsIgnoreCase(property.getValue());
    }

    public String toString() {
        return property.getValue();
    }

    /**
     * Set a change listener for this property. An initial property value will be sent.
     * @param configPropertyChangedListener Change listener to set on this property
     */
    public void setChangeListener(ConfigPropertyChangedListener configPropertyChangedListener) {
        setChangeListener(true, configPropertyChangedListener);
    }

    /**
     * Set a change listener for this property
     * <p>
     * @param fireInitial If true, the change listener will be called with the initial property value
     * @param configPropertyChangedListener Change listener to set on this property
     */
    public void setChangeListener(boolean fireInitial, ConfigPropertyChangedListener configPropertyChangedListener) {
        this.configPropertyChangedListener = configPropertyChangedListener;

        if (fireInitial) {
            configPropertyChangedListener.propertyChanged(this);
        }
    }

    /**
     * Remove the property change listener
     */
    public void clearPropertiesChangeHandler() {
        this.configPropertyChangedListener = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configChanged(List<String> newProperties, List<String> changedProperties, List<String> deletedProperties) {
        if (configPropertyChangedListener != null && changedProperties.contains(propertyName)) {
            configPropertyChangedListener.propertyChanged(this);
        }
    }
}
