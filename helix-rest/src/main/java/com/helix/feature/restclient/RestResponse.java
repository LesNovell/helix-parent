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

package com.helix.feature.restclient;

import com.google.common.collect.Multimap;
import com.helix.feature.restservice.error.jsonapi.ErrorResponse;
import com.helix.core.util.VertxTypeConverter;
import com.helix.feature.restservice.marshal.Marshaller;
import com.helix.feature.restservice.marshal.Message;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;

import java.util.function.Supplier;

/**
 * Represents an HTTP response. The response is created by the RestRequest
 * class after it executes the HTTP request.
 *
 * @param <T> Type of object expected in the HTTP response (in success cases)
 */
public class RestResponse<T> {
    private final HttpClientResponse response;
    private final Buffer body;
    private final Supplier<Marshaller> marshallerSupplier;
    private final Class<T> successType;

    /**
     * Use RestRequest as*() methods to create a RestRequest
     */
    RestResponse(HttpClientResponse response, Buffer body, Supplier<Marshaller> marshallerSupplier, Class<T> successType) {
        this.response = response;
        this.body = body;
        this.marshallerSupplier = marshallerSupplier;
        this.successType = successType;
    }

    /**
     * Get the HTTP status code as an integer
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return response.statusCode();
    }

    /**
     * Get the HTTP status code message
     *
     * @return HTTP status code, in a displayable message
     */
    public String getStatusText() {
        return response.statusMessage();
    }

    /**
     * Checks if the request is successful (HTTP 200)
     *
     * @return true if the HTTP response is 200
     */
    public boolean is200Successful() { return response.statusCode() == 200; }

    /**
     * Checks if the request is successful [200..299]
     *
     * @return true if the HTTP response is in the range of [200..299]
     */

    public boolean is2xxSuccessful() { return response.statusCode() >= 200 && response.statusCode() <=299; }

    /**
     * Get the response body, and marshal it to the expected response time.
     * <p>
     * It is recommended to check for a success message before calling this method.
     * In the case of an error, many servers return a different error type or even a simple String response.
     *
     * @return The marshaled response
     */
    @SuppressWarnings("unchecked")
    public T getBody() {
        T result;

        if (String.class.equals(successType)) {
            result = (T) getBodyAsString("UTF-8");
        } else if (byte[].class.equals(successType)) {
            result = (T) getBodyAsBytes();
        } else {
            Message message = new Message(body.getBytes(), response.getHeader("Content-Type"));
            result = (T) marshallerSupplier.get().unmarshal(successType, message);
        }

        return result;
    }

    /**
     * Marshals the response body into a standard
     * jsonapi.org ErrorResponse object
     *
     * @see <a href="http://jsonapi.org/format/#errors">jsonapi.org</a>
     * @return JSON API ErrorResponse object
     */
    public ErrorResponse getErrorResponse() {
        return getBodyAs(ErrorResponse.class);
    }

    /**
     * Marshals the response body into a requested object type.
     * <p>
     * Use this method if the server is returning a different type than you
     * would normally expect.  This is very useful for handling error conditions,
     * where the server may return a special error object type.
     *
     * @param clazz Type to marshal the response to
     * @param <T> Type to marshal the response to
     * @return The marshaled response
     */
    @SuppressWarnings("unchecked")
    public <T> T getBodyAs(Class<T> clazz) {
        T result;

        if (String.class.equals(clazz)) {
            result = (T) getBodyAsString("UTF-8");
        } else if (byte[].class.equals(clazz)) {
            result = (T) getBodyAsBytes();
        } else {
            Message message = new Message(body.getBytes(), response.getHeader("Content-Type"));
            result = (T) marshallerSupplier.get().unmarshal(clazz, message);
        }

        return result;
    }

    /**
     * Marshals the response body into a string.
     * <p>
     * Use this method if the server is returning a different type than you
     * would normally expect.  This is very useful for handling error conditions,
     * where the server may return a error string.
     *
     * @param encoding String encoding (ex, utf-8)
     * @return The response body represented as a string
     */
    public String getBodyAsString(String encoding) {
        return body.toString(encoding);
    }

    /**
     * Marshals the response body into a byte array.
     * <p>
     * Use this method if the server is returning binary data.
     * You might want to use this method if you have custom marshalling
     * for error responses.
     *
     * @param encoding String encoding (ex, utf-8)
     * @return The response body represented as a byte array
     */
    public byte[] getBodyAsBytes() {
        return body.getBytes();
    }

    /**
     * Get the response headers
     *
     * @return Map of response headers returned by the server
     */
    public Multimap<String, String> getHeaders() {
        return VertxTypeConverter.toGuavaMultimap(response.headers());
    }
}
