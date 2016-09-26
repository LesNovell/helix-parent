
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

package io.helixservice.core.feature;

import io.helixservice.core.component.Component;
import io.helixservice.core.component.ComponentRegistry;
import io.helixservice.core.container.Container;
import org.slf4j.Logger;

/**
 * Features allow modular addition of new functionality to the Helix Server.
 * <p>
 * Reusable features can be shared across teams. Each reusable feature should be built into
 * in its own Maven artifact for ease of re-use across development teams.
 * <p>
 * Feature Concepts:
 * <ul>
 *     <li>Features are registered with the Helix Server</li>
 *     <li>Feature lifecycle is managed by the Helix Server</li>
 *     <li>Features own their Components, which they create, register, and destroy</li>
 *     <li>Features may define factory methods which can be called by other Features</li>
 *     <li>Features must track the resources (sockets, files, etc) they create</li>
 * </ul>
 * <p>
 */
public interface Feature extends ComponentRegistry, Component {
    String COMPONENT_TYPE = "Feature";

    @Override
    default String getComponentType() {
        return Feature.COMPONENT_TYPE;
    }

    /**
     * Returns the name of this feature.
     *
     * @return Feature name. By default, this is the Java class name.
     */
    String getFeatureName();

    /**
     * Bootstrap features usually reference no other features,
     * and need to be installed prior to other core features.
     * The default value is false.
     *
     * @return true If this feature must be started during the Helix bootstrap phase
     */
    default boolean shouldStartDuringBootstrapPhase() {
        return false;
    }

    /**
     * Logs the feature configuration to the provided logger.
     * Primarily used on server startup to expose the Helix Server configuration
     *
     * @param logger Logger to write the feature details to
     */
    void logFeatureDetails(Logger logger);

    /**
     * Called by Helix Server when the server is starting.
     * <p>
     * The order features will be called is the same order they initially
     * registered with Helix.  At the time this method is called, Vert.x
     * will be initialized enabling the feature to create any Vert.x related
     * resources.
     * <p>
     * Features should create and register any singleton components
     * and allocate resources during the start call.  Blocking operations
     * are allowed in this method.
     *
     * @param container Helix Server that is starting up
     */
    void start(Container container);

    /**
     * Called by Helix Server when the server going to stop.
     * <p>
     * The order features will be called is the reverse order they initially
     * registered with Helix.  Features should NOT release resources at this point.
     * <p>
     * Stop accepting new requests, and finish all ongoing requests.
     * There will be a configurable delay between the finish and stop lifecycle
     * callbacks to enable the server to finish in-flight requests.
     *
     * @param container Helix Server that is finishing in-flight requests
     */
    void finish(Container container);

    /**
     * Called by Helix Server when the server has stopped.
     * <p>
     * The order features will be called is the reverse order they initially
     * registered with Helix. Features must release all owned resources at this point.
     * Blocking operations are allowed in this method.
     *
     * @param container Helix Server that is being stopped
     */
    void stop(Container container);
}
