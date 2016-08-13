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

package com.helix.feature.configuration.locator;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Optional;

/**
 * ResourceLocators should extend this base class
 */
public abstract class AbstractResourceLocator implements ResourceLocator {
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<JsonObject> getJsonObject(String resourcePath) {
        Optional<String> string = getString(resourcePath);

        if (string.isPresent()) {
            return Optional.of(new JsonObject(string.get()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getString(String resourcePath) {
        Optional<String> result = Optional.empty();

        try  {
            InputStream in = getStream(resourcePath).get();
            result = Optional.of(IOUtils.toString(in));
        } catch (Throwable t) {
            // Possibly expected
        }

        return result;
    }
}
