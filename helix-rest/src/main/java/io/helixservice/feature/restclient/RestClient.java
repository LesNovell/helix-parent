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

package io.helixservice.feature.restclient;

import io.helixservice.feature.restservice.marshal.Marshaller;
import io.vertx.core.http.HttpClient;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A simple API for calling REST endpoints, loosely based on UniRest API.
 * <p>
 * The implementation is non-blocking and asynchronous, without
 * the need for handlers.  Relies on Vert.x sync to provide
 * non-blocking through Quasar Fibers.
 *
 * @see <a href="http://unirest.io/java.html">unirest.io</a>
 */
public class RestClient {
    private Supplier<Marshaller> marshallerSupplier;
    private Supplier<HttpClient> httpClient;

    /**
     * Use RestClientFeature.restClient() to instantiate
     */
    RestClient(Supplier<Marshaller> marshallerSupplier, Supplier<HttpClient> httpClient) {
        this.marshallerSupplier = marshallerSupplier;
        this.httpClient = httpClient;
    }

    /**
     * Perform a GET Request
     * 
     * @param uriPath URI endpoint to call
     * @return The un-executed request object
     */
    public RestRequest get(String uriPath) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.GET, uriPath, Optional.empty());
    }

    /**
     * Perform a HEAD Request
     *
     * @param uriPath URI endpoint to call
     * @return The un-executed request object
     */
    public RestRequest head(String uriPath) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.HEAD, uriPath, Optional.empty());
    }

    /**
     * Perform a OPTIONS Request
     *
     * @param uriPath URI endpoint to call
     * @return The un-executed request object
     */
    public RestRequest options(String uriPath) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.OPTIONS, uriPath, Optional.empty());
    }

    /**
     * Perform a POST Request
     *
     * @param uriPath URI endpoint to call
     * @param requestBody Request body object, which will be marshaled using the registered Marshaller
     * @return The un-executed request object
     */
    public RestRequest post(String uriPath, Object requestBody) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.POST, uriPath, Optional.of(requestBody));
    }

    /**
     * Perform a PUT Request
     *
     * @param uriPath URI endpoint to call
     * @param requestBody Request body object, which will be marshaled using the registered Marshaller
     * @return The un-executed request object
     */
    public RestRequest put(String uriPath, Object requestBody) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.PUT, uriPath, Optional.of(requestBody));
    }

    /**
     * Perform a PATCH Request
     *
     * @param uriPath URI endpoint to call
     * @param requestBody Request body object, which will be marshaled using the registered Marshaller
     * @return The un-executed request object
     */
    public RestRequest patch(String uriPath, Object requestBody) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.PATCH, uriPath, Optional.of(requestBody));
    }

    /**
     * Perform a DELETE Request
     *
     * @param uriPath URI endpoint to call
     * @param requestBody Request body object, which will be marshaled using the registered Marshaller
     * @return The un-executed request object
     */
    public RestRequest delete(String uriPath, Object requestBody) {
        return new RestRequest(marshallerSupplier, httpClient, HttpMethod.DELETE, uriPath, Optional.of(requestBody));
    }

    /**
     * Perform a request, sending a request body
     *
     * @param httpMethod http method to use
     * @param uriPath URI endpoint to call
     * @param requestBody Request body object, which will be marshaled using the registered Marshaller
     * @return The un-executed request object
     */
    public RestRequest request(HttpMethod httpMethod, String uriPath, Object requestBody) {
        return new RestRequest(marshallerSupplier, httpClient, httpMethod, uriPath, Optional.ofNullable(requestBody));
    }


    /**
     * Perform a request, without a request body
     *
     * @param httpMethod http method to use
     * @param uriPath URI endpoint to call
     * @return The un-executed request object
     */
    public RestRequest request(HttpMethod httpMethod, String uriPath) {
        return new RestRequest(marshallerSupplier, httpClient, httpMethod, uriPath, Optional.empty());
    }
}
