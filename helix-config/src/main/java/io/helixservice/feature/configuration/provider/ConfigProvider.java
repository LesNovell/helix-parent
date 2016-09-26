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

import io.helixservice.core.component.Component;

import java.io.InputStream;
import java.util.Map;

public interface ConfigProvider extends Component {
    String CONFIG_PROVIDER_TYPE = "ConfigProvider";

    /**
     * {@inheritDoc}
     */
    @Override
    default String getComponentType() {
        return CONFIG_PROVIDER_TYPE;
    }

    /**
     * Get the currently configured paths to find configuration files
     *
     * @return Path list as an array, ex: ["default", "prod"]
     */
    String[] getProfilePaths();

    /**
     * Read a resource by name from one of the ResourceLocators
     *
     * @param name Resource name
     * @return The resource as an InputStream
     * @throws ConfigProviderException If the resource cannot be found by any ResourceLocator.
     */
    InputStream resourceAsInputStream(String name);

    /**
     * Read a resource by name from one of the ResourceLocators
     *
     * @param name Resource name
     * @return The resource as a String
     * @throws ConfigProviderException If the resource cannot be found by any ResourceLocator.
     */
    String resourceAsString(String name);

    /**
     * Return a single configuration property given its name
     *
     * @param name Property name
     * @return Property object containing the property's value or NULL if the property does not exist
     */
    Property propertyByName(String name);

    /**
     * Return a single configuration property given its name,
     * and if none is found then a default value is used
     *
     * @param name Property name
     * @param defaultValue Default value if the property is missing
     * @return Property object containing the property's value, or the default value if the property does not exist
     */
    Property propertyByName(String name, String defaultValue);

    /**
     * Return a map of properties with the given prefix.
     *
     * @param propertyPrefix Property prefix to match
     * @return A map containing key with the property name and Property as value
     */
    Map<String, Property> propertiesByPrefix(String propertyPrefix);

    /**
     * Request that the properties be reloaded immediately. If any properties
     * have changed, then any registered change listeners will be notified.
     *
     * @return The number of property files loaded, 0 indicating a possible error.
     */
    int reloadProperties();

    /**
     * Add a property change listener, which will be called whenever properties change.
     * @param listener Change listener to add
     */
    void addPropertiesChangedListener(ConfigProviderPropertiesChangedListener listener);

    /**
     * Remove a property change listener.
     * @param listener Change listener to remove
     */
    void removeConfigChangeHandler(ConfigProviderPropertiesChangedListener listener);

    /**
     * Set the paths to search for configuration files
     *
     * @param profilePaths List of configuration profile paths to load in order (ex: default, prod)
     * @param reloadImmediately If true, forces reloading of properties before this method returns
     */
    void setProfilePaths(String[] profilePaths, boolean reloadImmediately);
}
