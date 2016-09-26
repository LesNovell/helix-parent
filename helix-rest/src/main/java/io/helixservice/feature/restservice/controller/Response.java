
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

package io.helixservice.feature.restservice.controller;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.helixservice.feature.restservice.error.jsonapi.ErrorResponse;

/**
 * REST Response, created and returned by the REST controller
 *
 * @param <T> The type the response body
 */
public class Response<T> {
    private int httpStatusCode;
    private T responseBody;
    private Multimap<String, String> headers;

    private Response(int httpStatusCode, T responseBody) {
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;

        headers = LinkedHashMultimap.create();
    }

    /**
     * Create Response with HTTP 200 (Success)
     *
     * @param responseBody Response body object  (may be null)
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> successResponse(T responseBody) {
        return new Response<T>(200, responseBody);
    }

    /**
     * Create Response with HTTP 200 (Success)
     *
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> successResponse() {
        return new Response<T>(200, null);
    }

    /**
     * Create Response with HTTP 500 (Internal Server Error)
     *
     * @param responseBody Response body object  (may be null)
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> internalError(T responseBody) {
        return new Response<T>(500, responseBody);
    }

    /**
     * Create Response with HTTP 500 (Internal Server Error)
     *
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> internalError() {
        return new Response<T>(500, null);
    }

    /**
     * Create Response with HTTP 404 (Not Found)
     *
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> notFound() {
        return new Response<T>(404, null);
    }

    /**
     * Create Response with HTTP 404 (Not Found)
     *
     * @param responseBody Response body object  (may be null)
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> notFound(T responseBody) {
        return new Response<T>(404, responseBody);
    }

    /**
     * Create Response with given HTTP status code
     *
     * @param httpStatusCode HTTP status code
     * @param responseBody Response body object  (may be null)
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> fromHttpStatusCode(int httpStatusCode, T responseBody) {
        return new Response<T>(httpStatusCode, responseBody);
    }

    /**
     * Create Response with given HTTP status code
     *
     * @param httpStatusCode HTTP status code
     * @param <T> Type of Response body
     * @return The new Response object
     */
    public static <T> Response<T> fromHttpStatusCode(int httpStatusCode) {
        return new Response<T>(httpStatusCode, null);
    }

    /**
     * Create Response from a Json API error object, ensuring that the
     * HTTP status matches the status in errorResponse object.
     *
     * @param errorResponse JsonAPI error response
     * @return The new Response object
     * @see <a href="http://jsonapi.org/format/#errors">jsonapi.org</a>
     */
    public static Response<ErrorResponse> jsonAPIErrorResponse(ErrorResponse errorResponse) {
        return new Response<>(errorResponse.getHttpStatus(), errorResponse);
    }

    /**
     * Set HTTP Status Code Response
     *
     * @param httpStatusCode HTTP Status code
     * @return This response object
     */
    public Response<T> setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Set Response Body
     *
     * @param responseBody Response body object, which will be marshaled into the HTTP response body
     * @return This response object
     */
    public Response<T> setResponseBody(T responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public T getResponseBody() {
        return responseBody;
    }

    /**
     * Add a Header Value to this Response
     *
     * @param name Header Name
     * @param value Header Value
     * @return This response object
     */
    public Response<T> addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Remove all values for a header
     *
     * @param name Header Name
     * @return This response object
     */
    public Response<T> removeHeader(String name) {
        headers.removeAll(name);
        return this;
    }

    /**
     * Get all values for a header
     *
     * @return Multimap of header names to values
     */
    public Multimap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get the first value for a header
     *
     * @param headerName Header name
     * @return
     */
    public String getHeader(String headerName) {
        return Iterables.getFirst(headers.get(headerName), null);
    }
}
