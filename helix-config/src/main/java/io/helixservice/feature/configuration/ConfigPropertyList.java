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

package io.helixservice.feature.configuration;

import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.configuration.provider.ConfigProviderFactory;
import io.helixservice.feature.configuration.provider.ConfigProviderPropertiesChangedListener;
import io.helixservice.feature.configuration.provider.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a configuration property which is a list of items.
 * <p>
 * The property returned will be from the current environment's
 * configuration or the default configuration.
 */
public class ConfigPropertyList implements ConfigProviderPropertiesChangedListener {
    private ConfigProvider configProvider = ConfigProviderFactory.singleton();
    private ConfigPropertyListChangedListener configPropertyListChangedListener;

    private String propertyName;
    private List<Property> properties = new ArrayList<>();

    public ConfigPropertyList(String propertyName) {
        this.propertyName = propertyName;
        rebuildList();
    }

    /**
     * The size of the list of values in this property
     * @return Size of the list
     */
    public int size() {
        return properties.size();
    }

    /**
     * Get the value at the given index
     *
     * @param index Index into the property list
     * @return The value
     */
    public String valueAt(int index) {
        return properties.get(index).getValue();
    }

    /**
     * Get the entire property value list
     *
     * @return A list of property values
     */
    public List<String> toList() {
        return properties.stream().map(Property::getValue).collect(Collectors.toList());
    }

    /**
     * Set a change listener for this property. An initial property value will be sent.
     * @param configPropertyChangedListener Change listener to set on this property
     */
    public void setChangeListener(ConfigPropertyListChangedListener configPropertyChangedListener) {
        setChangeListener(true, configPropertyChangedListener);
    }

    /**
     * Set a change listener for this property
     * <p>
     * @param fireInitial If true, the change listener will be called with the initial property value
     * @param configPropertyChangedListener Change listener to set on this property
     */
    public void setChangeListener(boolean fireInitial, ConfigPropertyListChangedListener configPropertyChangedListener) {
        this.configPropertyListChangedListener = configPropertyChangedListener;

        if (fireInitial) {
            configPropertyChangedListener.propertyChanged(this);
        }
    }

    /**
     * Remove the property change listener
     */
    public void clearPropertiesChangeHandler() {
        this.configPropertyListChangedListener = null;
    }

    @Override
    public void configChanged(List<String> newProperties, List<String> changedProperties, List<String> deletedProperties) {
        for (String changedProperty : changedProperties) {
            if (changedProperty.startsWith(propertyName + "[")) {
                rebuildList();
                configPropertyListChangedListener.propertyChanged(this);
                break;
            }
        }
    }


    private void rebuildList() {
        properties.clear();
        Property nextProperty;
        for(int index=0; null != (nextProperty = configProvider.propertyByName(propertyName + "[" + index + "]")); index++) {
            properties.add(nextProperty);
        }
        configProvider.addPropertiesChangedListener(this);
    }
}