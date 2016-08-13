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

import com.helix.core.feature.AbstractFeature;
import com.helix.core.server.Server;
import com.helix.feature.configuration.locator.ClasspathResourceLocator;
import com.helix.feature.configuration.locator.FileSystemResourceLocator;
import com.helix.feature.configuration.locator.ResourceLocator;
import com.helix.feature.configuration.locator.ResourceLocatorRegistry;
import com.helix.feature.configuration.provider.ConfigProviderFactory;
import com.helix.feature.configuration.resolver.DefaultPropertyResolver;
import com.helix.feature.configuration.resolver.PropertyResolver;
import com.helix.feature.configuration.resolver.PropertyResolverRegistry;

import java.util.Collection;

/**
 * Provides a simple and extensible way of expressing configuration for services.
 * <p>
 * Features include:
 * <ul>
 *     <li>Loading properties from yaml files</li>
 *     <li>Loading of properties from a default and environment specific yaml files</li>
 *     <li>Loading of configuration files</li>
 *     <li>Installable ResourceLoaders (Filesystem, Classpath provided)</li>
 *     <li>Installable ResourceResolvers, useful for decryption or variable replacement</li>
 *     <li>Notification of property changes</li>
 * </ul>
 * <h2>Implementing your own ResourceLoader or PropertyResolver</h2>
 * If you have a custom place you need to load configuration from, such as
 * S3 bucket you may write your own implementation of ResourceLocator.
 * To register a new ResourceLoader, you will need to create a Feature that
 * is configured to load during Bootstrap. Simply register your new ResourceLocator
 * a component with the name "ResourceLocator".
 * <p>
 * Custom PropertyResolvers can be written as well.  A property resolver has an
 * opportunity to change the value of the property before it's read by the application.
 * This allows for features such as automatic decryption support and variable substitution.
 * PropertyResolvers must also be registered as components by a Bootstrap Feature.
 * <p>
 * ResourceLoaders and PropertyResolvers will be found and installed by the ConfigurationFeature
 * in the order they're configured. No additional "registration" step is needed.
 */
public class ConfigurationFeature extends AbstractFeature {
    private static final String SYSTEM_PROPERTY_APP_CONFIG = "app.config";
    private static final String DEFAULT_APP_CONFIG_PATH = "config";

    public ConfigurationFeature() {
        register(
                new ClasspathResourceLocator(DEFAULT_APP_CONFIG_PATH),
                new FileSystemResourceLocator(getConfigBaseFilePath()),
                new DefaultPropertyResolver()
        );

        // Bootstrap the config
        ResourceLocatorRegistry.set(findByType(ResourceLocator.TYPE_NAME));
        PropertyResolverRegistry.set(findByType(PropertyResolver.TYPE_NAME));
    }

    public static String getConfigBaseFilePath() {
        return System.getProperty(SYSTEM_PROPERTY_APP_CONFIG, DEFAULT_APP_CONFIG_PATH);
    }

    @Override
    public void start(Server server) {
        Collection<ResourceLocator> resourceLocators = server.findByType(ResourceLocator.TYPE_NAME);
        Collection<PropertyResolver> propertyResolvers = server.findByType(PropertyResolver.TYPE_NAME);

        if (resourceLocators.size() > 0 || propertyResolvers.size() > 0) {
            ResourceLocatorRegistry.set(resourceLocators);
            PropertyResolverRegistry.set(propertyResolvers);
            ConfigProviderFactory.singleton().reloadProperties();
        }
    }

    @Override
    public void stop(Server server) {
        ConfigProviderFactory.cleanupConfigProvider();
    }
}
