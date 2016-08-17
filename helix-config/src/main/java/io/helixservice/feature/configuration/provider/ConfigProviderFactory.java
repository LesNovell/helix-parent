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

import io.helixservice.feature.configuration.locator.ResourceLocatorRegistry;
import io.helixservice.feature.configuration.resolver.PropertyResolverRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides ConfigProvider as a singleton
 */
public class ConfigProviderFactory {
    private static Logger LOG = LoggerFactory.getLogger(ConfigProviderFactory.class);

    private static ConfigProvider INSTANCE;

    private static final String DEFAULT_PROFILE = "default";
    private static final String PROFILE_SEPARATOR = ",";
    private static final String DEV_PROFILE = "dev";

    private static final String SYSTEM_PROPERTY_PROFILE = "profile";

    private static final String APPLICATION_YAML = "application.yml";
    private static final int RELOAD_INTERVAL_IN_SECONDS = 60;


    /**
     * Get the ConfigProvider instance
     *
     * @return A ConfigProvider singleton
     */
    public synchronized static ConfigProvider singleton() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigProvider(activeProfiles(),
                    APPLICATION_YAML,
                    RELOAD_INTERVAL_IN_SECONDS,
                    ResourceLocatorRegistry.getInstance(),
                    PropertyResolverRegistry.getInstance());
        }

        return INSTANCE;
    }

    /**
     * Close down the running ConfigProvider,
     * and remove the singleton from memory.
     */
    public synchronized static void cleanupConfigProvider() {
        if (INSTANCE != null) {
            INSTANCE.stopReloadingProperties();
            INSTANCE = null;
        }
    }

    private static String[] activeProfiles() {
        String envProfile = System.getProperty(SYSTEM_PROPERTY_PROFILE, DEV_PROFILE);
        envProfile = DEFAULT_PROFILE + PROFILE_SEPARATOR + envProfile;

        LOG.info("Active Profiles: " + envProfile);

        return envProfile.split(PROFILE_SEPARATOR);
    }
}
