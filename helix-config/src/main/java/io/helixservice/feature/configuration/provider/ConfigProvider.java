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

import io.helixservice.feature.configuration.locator.ResourceLocator;
import io.helixservice.feature.configuration.resolver.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * ConfigProvider is a convenient interface for finding
 * properties and resources without having to work directly with
 * ResourceLocators and PropertyResolvers.
 * <p>
 * The ConfigProvider will consult each ResourceLocator in order
 * in attempt to find the requested property or resource.  Once found,
 * the PropertyResolvers are called in order so each has a chance to
 * decode the returned value.
 * <p>
 * In addition, the ConfigProvider attempts to refresh the properties
 * list to check for changes. It does this by creating a polling thread
 * that wakes up at a configurable interval
 */
public class ConfigProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigProvider.class);

    private String fileName;
    private final String[] profilePaths;
    private final Supplier<ResourceLocator[]> resourceLocators;
    private final Supplier<PropertyResolver[]> propertyResolvers;

    private Map<String, Property> propertyMap = new HashMap<>();

    private WeakHashMap<ConfigProviderPropertiesChangedListener, Boolean> propertiesChangedListeners = new WeakHashMap<>();
    private Timer reloadTimer;

    /**
     * Create the configuration provider. See ConfigProviderFactory, as this method is package protected.
     *
     * @param profilePaths List of configuration profile paths to load in order (ex: default, prod)
     * @param propertyFileName Name of the base property file (application.yml, usually)
     * @param reloadIntervalInSeconds Interval in seconds that the configuration properties are loaded
     * @param resourceLocators  A list of resource locators, the order is important
     * @param propertyResolvers A list of resource resolvers, the order is important
     *
     * @throws ConfigProviderException If there was a critical error, for example if no properties could be found.
     */
    ConfigProvider(String[] profilePaths,
            String propertyFileName,
            int reloadIntervalInSeconds,
            Supplier<ResourceLocator[]> resourceLocators,
            Supplier<PropertyResolver[]> propertyResolvers) throws ConfigProviderException {
        this.resourceLocators = resourceLocators;
        this.fileName = propertyFileName;
        this.profilePaths = profilePaths;
        this.propertyResolvers = propertyResolvers;

        loadInitialPropertiesSet(propertyFileName);
        startReloadTimer(reloadIntervalInSeconds);
    }

    /**
     * Read a resource by name from one of the ResourceLocators
     *
     * @param name Resource name
     * @return The resource as an InputStream
     * @throws ConfigProviderException If the resource cannot be found by any ResourceLocator.
     */
    public InputStream resourceAsInputStream(String name) {
        // Find a file in this order: CloudConfig (.secure), FileSystem, Classpath
        ResourceLocator[] resourceLocators = this.resourceLocators.get();
        for (int i = resourceLocators.length - 1; i >= 0; i--) {
            for (String profilePath : profilePaths) {
                String resourcePath = profilePath + File.separator + name;

                Optional<InputStream> stream = resourceLocators[i].getStream(resourcePath);
                if (stream.isPresent()) {
                    return stream.get();
                }
            }
        }

        throw new ConfigProviderException("Resource not found resourceName=" + name + " in:\n" + searchLocationsForDebugging(name));
    }

    /**
     * Read a resource by name from one of the ResourceLocators
     *
     * @param name Resource name
     * @return The resource as a String
     * @throws ConfigProviderException If the resource cannot be found by any ResourceLocator.
     */
    public String resourceAsString(String name) {
        // Find a file in this order: CloudConfig (.secure), FileSystem, Classpath
        ResourceLocator[] resourceLocators = this.resourceLocators.get();
        for (int i = resourceLocators.length - 1; i >= 0; i--) {
            for (String profilePath : profilePaths) {
                String resourcePath = profilePath + File.separator + name;

                Optional<String> string = resourceLocators[i].getString(resourcePath);
                if (string.isPresent()) {
                    return string.get();
                }
            }
        }

        throw new ConfigProviderException("Resource not found resourceName=" + name + " in:\n" + searchLocationsForDebugging(name));
    }

    /**
     * Return a single configuration property given its name
     *
     * @param name Property name
     * @return Property object containing the property's value or NULL if the property does not exist
     */
    public synchronized Property propertyByName(String name) {
        return propertyMap.get(name);
    }

    /**
     * Return a single configuration property given its name,
     * and if none is found then a default value is used
     *
     * @param name Property name
     * @param defaultValue Default value if the property is missing
     * @return Property object containing the property's value, or the default value if the property does not exist
     */
    public synchronized Property propertyByName(String name, String defaultValue) {
        Property property = propertyByName(name);

        if (property == null && defaultValue != null) {
            property = new Property(name, defaultValue, defaultValue);
            propertyMap.put(name, property);
        }

        return property;
    }

    /**
     * Return a map of properties with the given prefix.
     *
     * @param propertyPrefix Property prefix to match
     * @return A map containing key with the property name and Property as value
     */
    public Map<String, Property> propertiesByPrefix(String propertyPrefix) {
        return propertyMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(propertyPrefix + "."))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Request that the properties be reloaded immediately. If any properties
     * have changed, then any registered change listeners will be notified.
     *
     * @return The number of property files loaded, 0 indicating a possible error.
     */
    public int reloadProperties() {
        int countPropFileLoaded = 0;

        List<String> newProperties = new ArrayList<>();
        List<String> changedProperties = new ArrayList<>();
        List<String> deletedProperties = new ArrayList<>(propertyMap.keySet());

        Map<String, Object> yamlProperties = new HashMap<>();

        for (ResourceLocator resourceLocator : resourceLocators.get()) {
            for (String profilePath : profilePaths) {
                Optional<InputStream> streamFromClasspath = resourceLocator.getStream(profilePath + File.separator + fileName);
                if (streamFromClasspath != null && streamFromClasspath.isPresent()) {
                    YamlPropertiesLoader propertiesLoader = new YamlPropertiesLoader(streamFromClasspath.get());
                    Map properties = propertiesLoader.getFlattenedProperties();
                    if (properties != null) {
                        countPropFileLoaded++;
                        //noinspection unchecked
                        yamlProperties.putAll(properties);
                    }
                }
            }
        }

        // Update all properties
        for (String name : new TreeSet<>(yamlProperties.keySet())) {
            deletedProperties.remove(name);
            String unresolvedValue = yamlProperties.get(name) == null ? "" : yamlProperties.get(name).toString();

            Property property = propertyMap.get(name);
            if (property == null) {
                property = new Property(name, resolveValue(name, unresolvedValue), unresolvedValue);
                propertyMap.put(name, property);

                newProperties.add(name);
                logPropertyValue(property);
            } else {
                property.setUnresolvedValue(unresolvedValue);

                if (property.hasChanged()) {
                    changedProperties.add(name);
                    property.setValue(resolveValue(name, unresolvedValue));
                    logPropertyValue(property);
                }
            }
        }

        // Delete missing properties
        deletedProperties.forEach(propertyMap::remove);
        deletedProperties.forEach(p -> LOG.info("Property " + p + " deleted"));

        // Notify observers only afterEndpoint all properties are updated
        for (String name : yamlProperties.keySet()) {
            Property property = propertyMap.get(name);
            property.notifyObservers();
        }

        // Notify config change listeners
        if (newProperties.size() > 0 || changedProperties.size() > 0 || deletedProperties.size() > 0) {
            for (ConfigProviderPropertiesChangedListener configProviderPropertiesChangedListener : propertiesChangedListeners.keySet()) {
                configProviderPropertiesChangedListener.configChanged(newProperties, changedProperties, deletedProperties);
            }
        }

        return countPropFileLoaded;
    }

    /**
     * Add a property change listener, which will be called whenever properties change.
     * @param listener Change listener to add
     */
    public void addPropertiesChangedListener(ConfigProviderPropertiesChangedListener listener) {
        propertiesChangedListeners.put(listener, true);
    }

    /**
     * Remove a property change listener.
     * @param listener Change listener to remove
     */
    public void removeConfigChangeHandler(ConfigProviderPropertiesChangedListener listener) {
        propertiesChangedListeners.remove(listener);
    }

    /**
     * Stops the property reload timer
     */
    public void stopReloadingProperties() {
        reloadTimer.cancel();
        reloadTimer.purge();
    }


    private void logPropertyValue(Property property) {
        if (!isSensitiveProperty(property.getName(), property.getUnresolvedValue())) {
            LOG.info("Property " + property.getName() + "=" + property.getValue());
        } else {
            LOG.info("Property " + property.getName() + "=[sensitive]");
        }
    }

    private String resolveValue(String name, String unresolvedValue) {
        String value = unresolvedValue;

        for (PropertyResolver propertyResolver : propertyResolvers.get()) {
            value = propertyResolver.resolve(name, unresolvedValue);
        }

        return value;
    }

    private boolean isSensitiveProperty(String name, String value) {
        boolean sensitive = false;

        for (PropertyResolver propertyResolver : propertyResolvers.get()) {
            sensitive = sensitive || propertyResolver.sensitive(name, value);
        }

        return sensitive;
    }

    private void startReloadTimer(int reloadIntervalInSeconds) {
        if (reloadIntervalInSeconds > 0) {
            reloadTimer = new Timer(getClass().getSimpleName() + " property reload timer", true);
            reloadTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        reloadProperties();
                    } catch (Throwable t) {
                        LOG.error("Unable to reload properties", t);
                    }
                }
            }, reloadIntervalInSeconds * 1000L, reloadIntervalInSeconds * 1000L);
        }
    }

    private void loadInitialPropertiesSet(String propertyFileName) throws ConfigProviderException {
        int propFilesLoadedCount = reloadProperties();
        if (propFilesLoadedCount == 0) {
            String errorMessage = "No property files were loaded for propertyFileName=" + propertyFileName;
            LOG.error(errorMessage);
            throw new ConfigProviderException(errorMessage);
        }
    }

    private List<String> searchLocationsForDebugging(String name) {
        List<String> searchLocations = new ArrayList<>();
        for (ResourceLocator resourceLocator : resourceLocators.get()) {
            for (String profilePath : profilePaths) {
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }
                searchLocations.add(resourceLocator.getBasePath() + profilePath + File.separator + name);
            }
        }
        return searchLocations;
    }
}
