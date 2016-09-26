
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

import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.configuration.provider.ConfigProviderPropertiesChangedListener;
import io.helixservice.feature.configuration.provider.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a subset of properties from the configuration.
 * <p>
 * Allows you to work with only a portion of the configuration.
 * Can provide notification if any of the properties with this
 * prefix have changed.
 * <p>
 * Properties returned will be from the current environment's
 * configuration or the default configuration.
 */
public class ConfigProperties implements ConfigProviderPropertiesChangedListener {
    private ConfigProvider configProvider;
    private String propertyPrefix;
    private Map<String, Property> propertiesMap = new ConcurrentHashMap<>();
    private ConfigPropertiesChangedListener configPropertiesChangedListener;

    /**
     * Read all properties contained under the given propertyPrefix.
     *
     * @param propertyPrefix
     */
    public ConfigProperties(ConfigProvider configProvider, String propertyPrefix) {
        this.configProvider = configProvider;
        this.propertyPrefix = propertyPrefix;
        loadProperties();
        configProvider.addPropertiesChangedListener(this);
    }

    /**
     * Get a property's String value by name
     *
     * @param propertyName Full property name
     * @return The value of the property, or an empty Optional if the property was not found
     */
    public Optional<String> getProperty(String propertyName) {
        String result = null;

        Property property = propertiesMap.get(propertyName);
        if (property != null) {
            result = property.getValue();
        }

        return Optional.ofNullable(result);
    }

    /**
     * Get a property's Integer value by name
     *
     * @param propertyName Full property name
     * @return The Integer value of the property, or an empty Optional if the property was not found
     * @throws NumberFormatException If the property is not a valid Integer value
     */
    public Optional<Integer> asInt(String propertyName) {
        Integer result = null;

        Property property = propertiesMap.get(propertyName);
        if (property != null) {
            result = property.asInt();
        }

        return Optional.ofNullable(result);
    }

    /**
     * Get a property's Long value by name
     *
     * @param propertyName Full property name
     * @return The Long value of the property, or an empty Optional if the property was not found
     * @throws NumberFormatException If the property is not a valid Long value
     */
    public Optional<Long> asLong(String propertyName) {
        Long result = null;

        Property property = propertiesMap.get(propertyName);
        if (property != null) {
            result = property.asLong();
        }

        return Optional.ofNullable(result);
    }

    /**
     * Convert all properties defined to a Java Properties object
     *
     * @param removePrefix If true, this removes the prefix from the property name
     * @return Java properties object
     */
    public Properties toJavaProperties(boolean removePrefix) {
        Properties properties = new Properties();
        toMapOfProperties(removePrefix).forEach(properties::put);
        return properties;
    }

    /**
     * Convert all properties defined to a Map object
     *
     * @param removePrefix If true, this removes the prefix from the property name
     * @return Map object
     */
    public Map<String, String> toMapOfProperties(boolean removePrefix) {
        Map<String, String> result = new HashMap<>();

        propertiesMap.forEach((k, v) -> {
            String key = removePrefix ? k.substring(propertyPrefix.length() + 1) : k;
            result.put(key, v.getValue());
        });

        return result;
    }

    /**
     * Set a change listener that will be called if any of
     * the properties with the prefix is changed, added, or removed.
     * <p>
     * A single notification will be sent with all of the changes at once,
     * to simplify the work of the listener.
     * <p>
     * An initial properties change will be sent.
     *
     * @param configPropertiesChangedListener Listener to set for these properties
     */
    public void setChangeListener(ConfigPropertiesChangedListener configPropertiesChangedListener) {
        setChangeListener(true, configPropertiesChangedListener);
    }

    /**
     * Set a change listener that will be called if any of
     * the properties with the prefix is changed, added, or removed.
     * <p>
     * A single notification will be sent with all of the changes at once,
     * to simplify the work of the listener.
     *
     * @param fireInitial If true, the change listener will be notified immediately so it can process the current set of properties
     * @param configPropertiesChangedListener Listener to set for these properties
     */
    public void setChangeListener(boolean fireInitial, ConfigPropertiesChangedListener configPropertiesChangedListener) {
        this.configPropertiesChangedListener = configPropertiesChangedListener;

        if (fireInitial) {
            configPropertiesChangedListener.propertiesChanged(this,
                    new ArrayList<>(propertiesMap.keySet()),
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList());
        }
    }

    /**
     * Remove the registered change listener
     */
    public void clearPropertiesChangeHandler() {
        this.configPropertiesChangedListener = null;
    }

    private void loadProperties() {
        propertiesMap.clear();
        propertiesMap.putAll(configProvider.propertiesByPrefix(propertyPrefix));
    }

    private boolean intersect(List<String> properties) {
        boolean result = false;

        for (String property : properties) {
            result = result || property.startsWith(propertyPrefix);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configChanged(List<String> newProperties, List<String> changedProperties, List<String> deletedProperties) {
        loadProperties();

        if (configPropertiesChangedListener != null &&
                (intersect(changedProperties) || intersect(newProperties) || intersect(deletedProperties))) {

            configPropertiesChangedListener.propertiesChanged(this,
                    newProperties, changedProperties, deletedProperties);
        }
    }
}
