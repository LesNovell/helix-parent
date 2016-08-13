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
package com.helix.feature.vertx;

import com.helix.core.feature.AbstractFeature;
import com.helix.core.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Provides access to Vert.x native resources.
 *
 * Features may choose to use the Vert.x Native functionality
 * to gain direct access to underlying Vert.x capabilities.
 *
 * <h2>Resource Ownership</h2>
 * This feature owns any Vert.x objects it creates, so that when
 * the server is shut down or restarted all memory and resources
 * are cleaned up and cleared.
 *
 * Currently, only HTTPClient is supported but Vert.x offers
 * a ton more resources that could be provided through this interface.
 */
public class VertxNativeFeature extends AbstractFeature {
    public static final String DEFAULT_HTTPCLIENT_NAME = "DefaultHttpClient";

    private Map<String, HttpClient> httpClientMap = new ConcurrentHashMap<>();
    private Vertx vertx;

    @Override
    public void start(Server server)  {
        vertx = server.getVertx().get();
    }

    @Override
    public void stop(Server server) {
        closeHttpClients();
        vertx = null;
    }

    private void closeHttpClients() {
        httpClientMap.values().forEach(HttpClient::close);
        httpClientMap.clear();
    }

    public Supplier<HttpClient> httpClient() {
        return httpClient(DEFAULT_HTTPCLIENT_NAME);
    }

    public Supplier<HttpClient> httpClient(String httpClientName) {
        return () -> httpClientMap.computeIfAbsent(httpClientName, key -> vertx().createHttpClient());
    }

    public Supplier<HttpClient> httpClient(String httpClientName, HttpClientOptions httpClientOptions) {
        return () -> httpClientMap.computeIfAbsent(httpClientName, key -> vertx().createHttpClient(httpClientOptions));
    }

    private Vertx vertx() {
        checkInitialized();
        return vertx;
    }

    private Vertx checkInitialized() {
        return Objects.requireNonNull(vertx, "Vert.x native feature has not been initialized");
    }
}
