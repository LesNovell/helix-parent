
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

import io.helixservice.core.component.ComponentRegistry;
import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.core.container.Container;
import io.helixservice.feature.configuration.locator.ClasspathResourceLocator;
import io.helixservice.feature.configuration.locator.FileSystemResourceLocator;
import io.helixservice.feature.configuration.locator.ResourceLocator;
import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.configuration.provider.DefaultReloadableConfigProvider;
import io.helixservice.feature.configuration.resolver.DefaultPropertyResolver;
import io.helixservice.feature.configuration.resolver.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Provides a simple and extensible way of expressing configuration for services.
 * <p>
 * Features include:
 * <ul>
 * <li>Loading properties from yaml files</li>
 * <li>Loading of properties from a default and environment specific yaml files</li>
 * <li>Loading of configuration files</li>
 * <li>Installable ResourceLoaders (Filesystem, Classpath provided)</li>
 * <li>Installable ResourceResolvers, useful for decryption or variable replacement</li>
 * <li>Notification of property changes</li>
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
    private static final String DEFAULT_PROFILE = "default";
    private static final String PROFILE_SEPARATOR = ",";
    private static final String DEV_PROFILE = "dev";
    private static final String SYSTEM_PROPERTY_PROFILE = "profile";
    private static final String APPLICATION_YAML = "application.yml";
    private static final int RELOAD_INTERVAL_IN_SECONDS = 60;
    private static Logger LOG = LoggerFactory.getLogger(ConfigurationFeature.class);
    private ResourceLocator[] resourceLocators = new ResourceLocator[0];
    private PropertyResolver[] propertyResolvers = new PropertyResolver[0];

    private DefaultReloadableConfigProvider configProvider;

    public ConfigurationFeature() {
        configure(profilesFromEnvironment(), getConfigPath(), true);
    }

    private void configure(CharSequence[] activeProfiles, String configPath, boolean registerDefaultLocators) {
        LOG.info("Active Profiles: " + String.join(",", activeProfiles));

        if (registerDefaultLocators) {
            register(new ClasspathResourceLocator(configPath), new FileSystemResourceLocator(configPath), new DefaultPropertyResolver());
        }

        configProvider = new DefaultReloadableConfigProvider((String[]) activeProfiles,
                APPLICATION_YAML, RELOAD_INTERVAL_IN_SECONDS,
                () -> resourceLocators, () -> propertyResolvers);

        // Register config provider so other features can use it
        register(configProvider);

        // Attach the "bootstrap" configuration components
        // Usually this is file system and classpath
        attachConfigurationComponents(this);
    }

    private static String[] profilesFromEnvironment() {
        return (DEFAULT_PROFILE + PROFILE_SEPARATOR + System.getProperty(SYSTEM_PROPERTY_PROFILE, DEV_PROFILE)).split(PROFILE_SEPARATOR);
    }

    private static String getConfigPath() {
        return System.getProperty(SYSTEM_PROPERTY_APP_CONFIG, DEFAULT_APP_CONFIG_PATH);
    }

    private void attachConfigurationComponents(ComponentRegistry registry) {
        Collection<ResourceLocator> resourceLocators = registry.findComponentByType(ResourceLocator.TYPE_NAME);
        Collection<PropertyResolver> propertyResolvers = registry.findComponentByType(PropertyResolver.TYPE_NAME);

        if (resourceLocators.size() > 0 || propertyResolvers.size() > 0) {
            this.resourceLocators = resourceLocators.toArray(new ResourceLocator[resourceLocators.size()]);
            this.propertyResolvers = propertyResolvers.toArray(new PropertyResolver[propertyResolvers.size()]);
            configProvider.reloadProperties();
        }
    }

    public static String getConfigBaseFilePath() {
        return System.getProperty(SYSTEM_PROPERTY_APP_CONFIG, DEFAULT_APP_CONFIG_PATH);
    }

    @Override
    public boolean shouldStartDuringBootstrapPhase() {
        return true;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    @Override
    public void start(Container container) {
        // Attach all configuration components (across server context)
        attachConfigurationComponents(container);
    }

    @Override
    public void stop(Container container) {
        configProvider.stopReloadingProperties();
    }
}
