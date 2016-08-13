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

package com.helix.feature.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.helix.feature.restservice.controller.HttpMethod;
import com.helix.feature.restservice.controller.Request;
import com.helix.feature.restservice.controller.Response;
import com.helix.feature.restservice.controller.annotation.Controller;
import com.helix.feature.restservice.controller.annotation.Endpoint;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Supports HTTP request for metrics snapshot
 */
@Controller
public class MetricsController {
    private MetricRegistry metricRegistry;

    public MetricsController(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Endpoint(value = "/metrics", methods = HttpMethod.GET)
    public Response<String> getConsoleMetrics(Request request) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).outputTo(printStream).build();
        consoleReporter.report();

        return Response.successResponse(byteArrayOutputStream.toString());
    }
}
