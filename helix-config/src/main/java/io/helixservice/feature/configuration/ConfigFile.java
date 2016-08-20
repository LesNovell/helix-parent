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

package io.helixservice.feature.configuration;

import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.configuration.provider.ConfigProviderFactory;

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
     */
    public String readAsString() {
        ConfigProvider configProvider = ConfigProviderFactory.singleton();
        return configProvider.resourceAsString(fileName);
    }
}
