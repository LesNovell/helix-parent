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

import com.helix.feature.configuration.provider.ConfigProvider;
import com.helix.feature.configuration.provider.ConfigProviderFactory;

import java.io.InputStream;

/**
 * Read a file from any of the registered ResourceLocators
 */
public class ConfigFile {
    private String fileName;

    /**
     * Create a reference to the configuration file.
     * The file will not be loaded until one of the read methods is called.
     *
     * @param fileName Path to the file
     */
    public ConfigFile(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Read this configuration file as an InputStream.
     * Multiple calls to this method will open and read the file again.
     *
     * @return The input stream
     * @throws ConfigProviderException if the file cannot be found
     */
    public InputStream readAsInputStream() {
        ConfigProvider configProvider = ConfigProviderFactory.singleton();
        return configProvider.resourceAsInputStream(fileName);
    }

    /**
     * Reads the entire configuration file as a string.
     * Multiple calls to this method will open and read the file again.
     *
     * @return The configuration file's contents
     * @throws ConfigProviderException if the file cannot be found
     */
    public String readAsString() {
        ConfigProvider configProvider = ConfigProviderFactory.singleton();
        return configProvider.resourceAsString(fileName);
    }
}
