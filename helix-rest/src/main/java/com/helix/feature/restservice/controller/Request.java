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

package com.helix.feature.restservice.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * REST Request information, sent to Controller Endpoints
 *
 * @param <T> The type the request body should be marshaled to
 */
public class Request<T> {
    private T body;
    private Multimap<String,String> params;
    private Multimap<String,String> headers;
    private Multimap<String,String> headersUppercase;
    private String method;
    private String requestURI;
    private String remoteAddr;
    private String httpVersion;

    /**
     * Create Request
     */
    public Request(String method, String requestURI, Multimap<String,String> params,
            Multimap<String,String> headers, T body, String remoteAddr, String httpVersion) {
        this.method = method;
        this.requestURI = requestURI;
        this.body = body;
        this.params = HashMultimap.create(params);
        this.headers = HashMultimap.create(headers);
        this.remoteAddr = remoteAddr;
        this.httpVersion = httpVersion;

        headersUppercase = HashMultimap.create();
        headers.entries().forEach(e -> headersUppercase.put(e.getKey().toUpperCase(), e.getValue()));
    }

    /**
     * Version of the HTTP protocol
     * @return The version string
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Query parameters from the request
     *
     * @return Multimap of parameters to values
     */
    public Multimap<String,String> getParams() {
        return ImmutableMultimap.copyOf(params);
    }

    /**
     * HTTP Headers from the request
     *
     * @return Multimap of Headers to values
     */
    public Multimap<String,String> getHeaders() {
        return ImmutableMultimap.copyOf(headers);
    }


    /**
     * Gets the first value found for the given header
     *
     * @param headerName Header name
     * @param defaultValue Default value if the header does not exist
     * @return The first header value found, or the default value
     */
    public String getHeader(String headerName, String defaultValue) {
        return Iterables.getFirst(headersUppercase.get(headerName.toUpperCase()), defaultValue);
    }

    /**
     * Gets the first value found for the given  query parameter
     *
     * @param parameterName Query parameter name
     * @param defaultValue Default value if the parameter does not exist
     * @return The first parameter value found, or the default value
     */
    public String getParam(String parameterName, String defaultValue) {
        return Iterables.getFirst(params.get(parameterName), defaultValue);
    }

    /**
     * Gets the HTTP Method String
     *
     * @return HTTP Method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get the HTTP URI
     *
     * @return Request URI
     */
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * Get remote client address
     *
     * @return Remote client address
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Get the request body, marshaled to the desired type
     *
     * @return The request body object
     */
    public T getBody() {
        return body;
    }

    void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    Request<T> addHeader(String headerName, String value) {
        headers.put(headerName, value);
        headersUppercase.put(headerName.toUpperCase(), value);
        return this;
    }

    Request<T> addParam(String paramName, String value) {
        params.put(paramName, value);
        return this;
    }
}
