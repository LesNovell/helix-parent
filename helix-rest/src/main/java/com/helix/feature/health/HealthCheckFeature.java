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

package com.helix.feature.health;

import com.helix.core.feature.AbstractFeature;
import com.helix.feature.configuration.ConfigProperty;
import com.helix.feature.configuration.locator.ClasspathResourceLocator;
import com.helix.feature.restservice.controller.component.ControllerComponent;

import java.time.Clock;

/**
 * Health check adds a simple health check page indicating the status of the server.
 * <b>
 *      <li>HEAD|GET /health/healthcheck    => HTTP 200=Healthy, HTTP 599=Offline</li>
 *      <li>HEAD|GET /health/heartbeat      => HTTP 200=Healthy, HTTP 599=Offline</li>
 * </b>
 * <p>
 * Health check endpoint also supports marking the server as offline by sending:
 * <br>
 * <b>GET /health/heartbeat?offline=(true|false)&password=(health-check.forced-down-password)</b>
 * <p>
 * Password must match the configured password in the application.yml file.
 *
 */
public class HealthCheckFeature extends AbstractFeature {
    ConfigProperty forcedDownPassword = new ConfigProperty("health-check.forced-down-password");

    public HealthCheckFeature() {
        HealthController healthController =
                new HealthController(new QueryParameterOfflineProcessor(forcedDownPassword.getValue()),
                        ClasspathResourceLocator.INSTANCE, Clock.systemDefaultZone());

        register(ControllerComponent.fromAnnotationsOn(healthController));
    }
}
