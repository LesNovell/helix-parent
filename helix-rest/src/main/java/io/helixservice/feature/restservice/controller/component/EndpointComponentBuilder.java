
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

package io.helixservice.feature.restservice.controller.component;

import io.helixservice.core.component.Component;
import io.helixservice.feature.restservice.controller.EndpointHandler;
import io.helixservice.feature.restservice.controller.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder interface for creating EndpointComponents
 */
public class EndpointComponentBuilder implements Component {
    private String path;
    private List<Endpoint> endpoints = new ArrayList<>();

    private EndpointComponentBuilder(String path) {
        this.path = path;
    }

    /**
     * Create a new EndpointBuilder for an endpoint path
     *
     * @param path URI path
     * @return The fluent builder
     */
    public static EndpointComponentBuilder forPath(String path) {
        return new EndpointComponentBuilder(path);
    }

    /**
     * Build an new EndpointComponent for a lambda-based endpoint,
     * with a request body type of String.
     *
     * @param httpMethod HTTP method this endpoint accepts
     * @param functionalEndpointHandler Functional endpoint to invoke
     * @return The fluent builder
     */
    public EndpointComponentBuilder handle(HttpMethod httpMethod, EndpointHandler functionalEndpointHandler) {
        endpoints.add(Endpoint.forPath(path, new HttpMethod[] {httpMethod}, functionalEndpointHandler));
        return this;
    }

    /**
     * Build an EndpointComponent for a lambda-based endpoint,
     * with a specific request body type.
     *
     * @param httpMethod HTTP method this endpoint accepts
     * @param functionalEndpointHandler Functional endpoint to invoke
     * @param requestBodyType Type to marshall the incoming request to
     * @return The fluent builder
     */
    public EndpointComponentBuilder handle(HttpMethod httpMethod, EndpointHandler functionalEndpointHandler, Class requestBodyType) {
        endpoints.add(Endpoint.forPath(path, new HttpMethod[] {httpMethod}, functionalEndpointHandler, requestBodyType));
        return this;
    }

    /**
     * Build an new EndpointComponent for a lambda-based endpoint,
     * with a request body type of String.
     *
     * @param httpMethods HTTP methods this endpoint accepts
     * @param functionalEndpointHandler Functional endpoint to invoke
     * @return The fluent builder
     */
    public EndpointComponentBuilder handle(HttpMethod[] httpMethods, EndpointHandler functionalEndpointHandler) {
        for (HttpMethod method : httpMethods) {
            handle(method, functionalEndpointHandler);
        }
        return this;
    }

    /**
     * Build an EndpointComponent for a lambda-based endpoint,
     * with a specific request body type.
     *
     * @param httpMethods HTTP methods this endpoint accepts
     * @param functionalEndpointHandler Functional endpoint to invoke
     * @param requestBodyType Type to marshall the incoming request to
     * @return The fluent builder
     */
    public EndpointComponentBuilder handle(HttpMethod[] httpMethods, EndpointHandler functionalEndpointHandler, Class requestBodyType) {
        for (HttpMethod method : httpMethods) {
            handle(method, functionalEndpointHandler, requestBodyType);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentType() {
        return "EndpointBuilder";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription() {
        return null;
    }

    /**
     * Returns a array of all EndpointComponents created by this EndpointBuilder
     * @return Array of EndpointComponents
     */
    @Override
    public Component[] getContainedComponents() {
        return endpoints.toArray(new Component[endpoints.size()]);
    }
}
