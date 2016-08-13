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

import com.helix.core.component.Component;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.util.Optional;

/**
 * All resource locators must implement this interface
 */
public interface ResourceLocator extends Component {
    String TYPE_NAME = "ResourceLocator";

    /**
     * @return "ResourceLocator" type name
     */
    @Override
    default String getComponentType() {
        return TYPE_NAME;
    }

    /**
     * Requests that this resource locator attempt to find a file.
     *
     * @param resourcePath The resource path for the file
     * @return An optional, if found containing the InputStream to read the file.
     */
    Optional<InputStream> getStream(String resourcePath);

    /**
     * Requests that this resource locator attempt to load a file as a String.
     *
     * @param resourcePath The resource path for the file
     * @return An optional, if found containing the contents of the file as a String
     */
    Optional<String> getString(String resourcePath);

    /**
     * Requests that this resource locator attempt to load a file as JsonObject.
     *
     * @param resourcePath The resource path for the file
     * @return An optional, if found containing the contents of the file as a JsonObject
     * @throws DecodeException If the JsonObject cannot be decoded
     */
    Optional<JsonObject> getJsonObject(String resourcePath);

    /**
     * Returns a representation of the base path for this resource locator.
     * This is mostly used for logging, it will not be used to attempt to access the resource.
     *
     * @return A string representing the base path.
     */
    String getBasePath();
}
