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

package io.helixservice.feature.configuration.locator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Locate Resources from the file system
 */
public class FileSystemResourceLocator extends AbstractResourceLocator {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemResourceLocator.class);
    private String basePath;

    /**
     * Create a FileSystemResourceLocator that will find all its resources under the given basePath.
     * @param basePath Path to the resources in the file system
     */
    public FileSystemResourceLocator(String basePath) {
        this.basePath = basePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<InputStream> getStream(String resourcePath) {
        LOG.debug("Looking for file:" + resourcePath);

        Optional<InputStream> result = Optional.empty();

        File file = new File(basePath + File.separator + resourcePath);
        if (file.exists()) {
            try {
                result = Optional.of(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                LOG.error("Unexpected file not found for file=" + file.getAbsolutePath(), e);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBasePath() {
        return "file://" + basePath + "/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription() {
        return "File System Resource Locator";
    }
}
