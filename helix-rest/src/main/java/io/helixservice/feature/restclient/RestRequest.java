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

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.helixservice.core.util.VertxTypeConverter;
import io.helixservice.feature.restservice.marshal.Marshaller;
import io.helixservice.feature.restservice.marshal.Message;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.vertx.ext.sync.Sync.awaitEvent;

/**
 * Fluent interface for a pending REST request
 */
public class RestRequest {
    private final Supplier<Marshaller> marshallerSupplier;
    private final Supplier<HttpClient> httpClient;
    private final HttpMethod method;
    private final String urlPath;
    private final Optional<Object> requestBody;

    private Multimap<String, String> headers = HashMultimap.create();
    private Map<String, String> pathVariables = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private Long timeoutInMs;
    private boolean useDefaultHostAndPort = false;


    /**
     * Use RestClient factory methods to create the initial RestRequest
     */
    RestRequest(Supplier<Marshaller> marshallerSupplier, Supplier<HttpClient> httpClient,
            HttpMethod method, String urlPath, Optional<Object> requestBody) {
        this.marshallerSupplier = marshallerSupplier;
        this.httpClient = httpClient;
        this.method = method;
        this.urlPath = urlPath;
        this.requestBody = requestBody;
    }

    /**
     * Set a timeout for this request. Overrides the default HTTP timeout defined in VertxHTTPOptions.
     *
     * @param timeoutInMs Timeout in milliseconds
     */
    public void setTimeout(long timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
    }

    /**
     * Clear any headers that might be inserted automatically before the request.
     * Provides full control over headers sent.
     *
     * @return This fluent interface
     */
    public RestRequest clearDefaultHeaders() {
        this.headers.clear();
        return this;
    }

    /**
     * If true, then this request will use the default
     * host and port configured originally in the VertxHTTPOptions.
     * <p>
     * Host and Port can be omitted, as they will be replaced
     * if this parameter is true.
     *
     * @return This fluent interface
     */
    public RestRequest useDefaultHostAndPort() {
        this.useDefaultHostAndPort = true;
        return this;
    }

    /**
     * Add a header to this request
     *
     * @param name Name of the header
     * @param value Value of the header
     * @return This fluent interface
     */
    public RestRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Add a multimap of headers to this request
     *
     * @param headers Map of headers to add to this request
     * @return This fluent interface
     */
    public RestRequest headers(Multimap<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Replace REST path variable inside the provided URI.
     * <p>
     * Path variables can be defined in the URI by surrounding
     * the variable name with curly braces. For Example,
     * <pre>
     *     http://localhost:8080/document/{documentId}
     * </pre>
     *
     * @param variable Variable name
     * @param value Variable value
     * @return This fluent interface
     */
    public RestRequest pathVariable(String variable, String value) {
        this.pathVariables.put(variable, value);
        return this;
    }

    /**
     * Replace REST path variables inside the provided URI.
     * <p>
     * Path variables can be defined in the URI by surrounding
     * the variable name with curly braces. For Example,
     * <pre>
     *     http://localhost:8080/document/{documentId}
     * </pre>
     *
     * @param pathVariables Map of path variables to add to this request
     * @return This fluent interface
     */
    public RestRequest pathVariables(Map<String, String> pathVariables) {
        this.pathVariables.putAll(pathVariables);
        return this;
    }


    /**
     * Add URI parameters to the request
     *
     * @param parameters Map of parameters
     * @return This fluent interface
     */
    public RestRequest parameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    /**
     * Add URI a parameter to the request
     *
     * @param parameter Name of the parameter
     * @param value Value of the parameter
     * @return This fluent interface
     */
    public RestRequest parameter(String parameter, String value) {
        this.parameters.put(parameter, value);
        return this;
    }

    /**
     * Execute the request, with the expected response body marshaled as a String
     *
     * @return RestResponse fluent interface
     * @throws SuspendExecution For Vert.x Sync
     */
    public RestResponse<String> asString() throws SuspendExecution {
        return asObject(String.class);
    }

    /**
     * Execute the request, with the expected response body marshaled as a byte array
     *
     * @return RestResponse fluent interface
     * @throws SuspendExecution For Vert.x Sync
     */
    public RestResponse<byte[]> asByteArray() throws SuspendExecution {
        return asObject(byte[].class);
    }

    /**
     * Execute the request, with the expected response body marshaled
     * to a specific object type.
     *
     * @param responseType Type we expect the response to be marshaled to
     * @return RestResponse fluent interface
     * @throws SuspendExecution For Vert.x Sync
     */
    public <T> RestResponse<T> asObject(Class<T> responseType) throws SuspendExecution {
        try {
            // Apply Params & Url Vars
            String modifiedUrlPath = addParameters(replaceUrlVars(urlPath));

            // Do request
            HttpClientRequest request;
            if (useDefaultHostAndPort) {
                request = httpClient.get().request(
                        io.vertx.core.http.HttpMethod.valueOf(method.name()),
                        modifiedUrlPath);
            } else {
                request = httpClient.get().requestAbs(
                        io.vertx.core.http.HttpMethod.valueOf(method.name()),
                        modifiedUrlPath);
            }

            // Set timeout, if requested
            if (timeoutInMs != null) {
                request.setTimeout(timeoutInMs);
            }

            // With headers
            request.headers().addAll(VertxTypeConverter.toVertxMultiMap(headers));

            // Write body if we need to
            Buffer body = Buffer.buffer();
            if (requestBody.isPresent()) {
                request.setChunked(true);
                Message message = marshallerSupplier.get().marshal(requestBody);

                List<String> contentTypes = message.getContentTypes();
                if (contentTypes != null && contentTypes.size() > 0) {
                    request.putHeader("Content-Type", contentTypes);
                }

                body = body.appendBytes(message.getBody());
            }

            // Wait for response with Vert.x Sync
            HttpClientResponse httpClientResponse = getHttpClientResponse(request, body);
            Buffer bodyBuffer = getBuffer(httpClientResponse);

            return new RestResponse<>(httpClientResponse, bodyBuffer, marshallerSupplier, responseType);
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to parse urlPath=" + urlPath, e);
        }
    }

    private Buffer getBuffer(HttpClientResponse httpClientResponse) throws SuspendExecution {
        return awaitEvent(httpClientResponse::bodyHandler);
    }

    private HttpClientResponse getHttpClientResponse(HttpClientRequest request, Buffer body) throws SuspendExecution {
        return awaitEvent(h -> { request.handler(h); request.write(body); request.end(); });
    }

    private String addParameters(String urlPath) throws URISyntaxException, UnsupportedEncodingException {
        StringBuffer result = new StringBuffer(urlPath);

        if (parameters.size() > 0) {
            boolean first = true;
            result.append("?");
            for (Map.Entry<String, String> paramEntry : parameters.entrySet()) {
                if (!first) {
                    result.append("&");
                }
                result.append(URLEncoder.encode(paramEntry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(paramEntry.getValue(), "UTF-8"));
                first = false;
            }
        }

        return result.toString();
    }

    private String replaceUrlVars(String urlPath) {
        for (Map.Entry<String, String> pathVarEntry : pathVariables.entrySet()) {
            urlPath = urlPath.replace("{" + pathVarEntry.getKey() + "}", pathVarEntry.getValue());
        }
        return urlPath;
    }
}
