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

package io.helixservice.core.server;

import io.helixservice.core.component.ComponentRegistry;
import io.helixservice.core.feature.Feature;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.Optional;

/**
 * Helix Server Interface
 */
public interface Server extends ComponentRegistry {
    /**
     * Get the Vert.x instance, available after the bootstrap features have started.
     *
     * @return The Vert.x instance, which will be present during lifecycle callbacks to core features.
     */
    Optional<Vertx> getVertx();

    /**
     * Get the Server's current state
     *
     * @return The server's current state
     */
    ServerState getServerState();

    /**
     * Get a list of features started
     *
     * @return The list of features installed, or null if the server is stopped
     */
    List<Feature> getFeatures();

    /**
     * Start the Helix Server, asynchronously
     * <p>
     * @return A thread which is running the start operation
     * @throws IllegalStateException if the server is not in the stopped state
     */
    Thread start();

    /**
     * Restart the Helix Server, asynchronously
     * <p>
     * This shuts down all the features, waits for the in-flight requests to complete,
     * then starts the server again. Restarting the server includes re-creating all
     * features.
     * <p>
     * @return A thread which is running the start operation
     * @throws IllegalStateException if the server is not in the started state
     */
    Thread restart();

    /**
     * Start the Helix Server, asynchronously
     * <p>
     * This shuts down all the features, waits for the in-flight requests to complete.
     * During shut-down, all new HTTP requests will fail with HTTP error code 599.
     * This signals the Load Balancer to take the service out of rotation, preventing
     * new requests from flowing to the service.
     * <p>
     * @return A thread which is running the start operation
     * @throws IllegalStateException if the server is not in the started state
     */
    Thread stop(boolean immediate);
}
