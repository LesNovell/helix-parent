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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.helix.core.feature.AbstractFeature;
import com.helix.core.server.Server;
import com.helix.feature.restservice.controller.component.ControllerComponent;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import java.util.concurrent.TimeUnit;

/**
 * Provides Metrics Collection based on codahale metrics library.
 * <p>
 * Registers Thread, GC, and Memory usage metrics by default.
 * In addition, accepts metrics reported via the Vert.x event bus.
 * <h2>Vert.x Event Bus Consumers (Local Consumer only):</h2>
 * <pre>
 * Address: "metrics.histogram"  Body: "metricKey=metricValue"
 * Address: "metrics.timer"      Body: "metricKey=metricValueInMillis"
 * Address: "metrics.counter"    Body: "metricKey=metricValue"
 * </pre>
 * <h2>Metrics Publishing</h2>
 * Metric publishing can be configured by getting the MetricRegistry
 * and adding the desired publisher.  Currently, the only publishing
 * mechanism is through a HTTP request "GET /metrics".
 */
public class MetricsFeature extends AbstractFeature {
    private static final String SEPARATOR_REGEX = "=";
    private MetricRegistry metricRegistry;

    public MetricsFeature() {
        metricRegistry = new MetricRegistry();
        metricRegistry.registerAll(new ThreadStatesGaugeSet());
        metricRegistry.registerAll(new GarbageCollectorMetricSet());
        metricRegistry.registerAll(new MemoryUsageGaugeSet());

        MetricsController metricsController = new MetricsController(metricRegistry);
        register(ControllerComponent.fromAnnotationsOn(metricsController));
    }

    public MetricRegistry metricRegistry() {
        return metricRegistry;
    }

    @Override
    public void start(Server server) {
        addEventBusConsumers(server.getVertx().get());
    }

    private void addEventBusConsumers(Vertx vertx) {
        EventBus eventBus = vertx.eventBus();

        eventBus.localConsumer("metrics.histogram").handler(histogram -> {
            String[] bodySplit = splitRequest(histogram);
            metricRegistry.histogram(bodySplit[0])
                    .update(Long.valueOf(bodySplit[1]));
        });

        eventBus.localConsumer("metrics.timer").handler(timer -> {
            String[] bodySplit = splitRequest(timer);
            metricRegistry.timer(bodySplit[0])
                    .update(Long.valueOf(bodySplit[1]), TimeUnit.MILLISECONDS);
        });

        eventBus.localConsumer("metrics.counter").handler(timer -> {
            String[] bodySplit = splitRequest(timer);
            metricRegistry.counter(bodySplit[0])
                    .inc(Long.valueOf(bodySplit[1]));
        });
    }

    private String[] splitRequest(Message<Object> histogram) {
        String body = (String) histogram.body();
        return body.split(SEPARATOR_REGEX);
    }
}
