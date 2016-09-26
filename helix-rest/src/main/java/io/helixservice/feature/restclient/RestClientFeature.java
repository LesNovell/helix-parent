
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

package io.helixservice.feature.restclient;

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.restservice.marshal.Marshaller;
import io.helixservice.feature.vertx.VertxNativeFeature;
import io.vertx.core.http.HttpClientOptions;

/**
 * Provides a simple API for calling REST endpoints.
 * <p>
 * The implementation is non-blocking and asynchronous, without
 * the need for handlers.  Relies on Vert.x sync to provide
 * non-blocking through Quasar Fibers.
 * <p>
 * This API is loosely based upon the UniRest API, however it provides
 * support for multiple REST clients that are configured differently.
 * <p>
 * This feature uses the Marshaller registered with this Feature.
 * By Default, Jackson's ObjectMapper is used.  To register a different
 * Marshaller, call restClientFeature.register(customMarshaller).
 *
 * @see <a href="http://unirest.io/java.html">unirest.io</a>
 */
public class RestClientFeature extends AbstractFeature {
    private VertxNativeFeature vertxNativeFeature;

    /**
     * Create this REST client feature, which uses Vert.x for its underlying HTTP implementation
     *
     * @param vertxNativeFeature For Vert.x native access
     */
    public RestClientFeature(VertxNativeFeature vertxNativeFeature) {
        this.vertxNativeFeature = vertxNativeFeature;
    }

    /**
     * Create a new REST client. The REST client here is not cached,
     * so only call this method once and reuse the returned REST client.
     *
     * @return A new REST client
     */
    public RestClient restClient() {
        return restClient(VertxNativeFeature.DEFAULT_HTTPCLIENT_NAME);
    }

    /**
     * Create a new REST client. The REST client here is not cached,
     * so only call this method once and reuse the returned REST client.
     *
     * @param httpClientName Name of the HTTP client
     * @return A new REST client, with the given name
     */
    public RestClient restClient(String httpClientName) {
        return new RestClient(this::getMarshaller, vertxNativeFeature.httpClient(httpClientName));
    }

    /**
     * Create a new REST client. The REST client here is not cached,
     * so only call this method once and reuse the returned REST client.
     *
     * @param httpClientName Name of the HTTP client
     * @param httpClientOptions Vert.x HTTP client options. Important for configuring timeout, SSL, etc.
     * @return A new REST client, with the given name
     */
    public RestClient restClient(String httpClientName, HttpClientOptions httpClientOptions) {
        return new RestClient(this::getMarshaller, vertxNativeFeature.httpClient(httpClientName, httpClientOptions));
    }

    private Marshaller getMarshaller() {
        return findComponentByType(Marshaller.TYPE_NAME, Marshaller.DEFAULT);
    }
}
