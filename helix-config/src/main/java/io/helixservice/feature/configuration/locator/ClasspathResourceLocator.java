
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
import java.io.InputStream;
import java.util.Optional;

/**
 * Locate Resources from the Java Classpath
 */
public class ClasspathResourceLocator extends AbstractResourceLocator {
    private static final Logger LOG = LoggerFactory.getLogger(ClasspathResourceLocator.class);
    public static final ResourceLocator INSTANCE = new ClasspathResourceLocator();

    public String basePath = "";

    /**
     * Create a ClasspathResourceLocator using the root classpath
     */
    public ClasspathResourceLocator() {
    }

    /**
     * Create a ClasspathResourceLocator that will find all its resources under the given basePath.
     * @param basePath Path to the resources in the classpath
     */
    public ClasspathResourceLocator(String basePath) {
        this.basePath = basePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<InputStream> getStream(String resourcePath) {
        String fqResourcePath = (basePath.isEmpty() ? "" :  File.separator + basePath) + File.separator + resourcePath;
        LOG.debug("Looking for classpath://" + fqResourcePath);
        return Optional.ofNullable(this.getClass().getResourceAsStream(fqResourcePath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBasePath() {
        return "classpath://" + basePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription() {
        return "ClassPath Resource Locator";
    }
}
