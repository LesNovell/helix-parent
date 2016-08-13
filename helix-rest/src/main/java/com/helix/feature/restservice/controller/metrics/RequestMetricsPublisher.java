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

package com.helix.feature.restservice.controller.metrics;

import io.vertx.core.eventbus.EventBus;

/**
 * Publishes metrics for REST Request to the EventBus.
 * <p>
 * The response duration, response size, response code, and path are published.
 * Metrics published here are picked up by the MetricsFeature for aggregation and reporting.
 *
 * @see com.helix.feature.metrics.MetricsFeature
 */
public class RequestMetricsPublisher {
    private EventBus eventBus;
    private String path;

    private long start = System.currentTimeMillis();
    private String httpMethod = "";
    private long responseSize = 0;
    private boolean success = false;

    /**
     * Create REST Endpoint Metrics Publisher
     *
     * @param eventBus Vert.x Event Bus
     * @param path REST URI path
     */
    public RequestMetricsPublisher(EventBus eventBus, String path) {
        this.eventBus = eventBus;
        this.path = path;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setResponseSize(long responseSize) {
        this.responseSize = responseSize;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Publish request data metrics to the event bus,
     * this information should be picked up by MetricsFeature
     */
    public void publish() {
        long duration = System.currentTimeMillis() - start;
        String metricName = httpMethod + "." + path;

        // Duration as histogram and timer
        eventBus.publish("metrics.timer", "controller.endpoint.duration." + metricName + "=" + duration);

        // Counters for Total, Success, and Failued request
        eventBus.publish("metrics.counter", "controller.endpoint.total.count." + metricName + "=1");
        eventBus.publish("metrics.counter", "controller.endpoint.success.count." + metricName + "=" + (success ? "1" : "0"));
        eventBus.publish("metrics.counter", "controller.endpoint.failure.count." + metricName + "=" + (success ? "0" : "1"));

        // Histogram of response size
        eventBus.publish("metrics.histogram", "controller.endpoint.response.size." + metricName + "=" + responseSize);
    }
}
