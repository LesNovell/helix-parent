
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

/**
 * REST Endpoint Configuration Component
 * <p>
 * Endpoint components may be created by the ControllerComponent.fromAnnotationsOn() method,
 * or by calling one of the Lambda factory methods to create EndpointComponents.
 * <p>
 * Each REST Endpoint must be registered with the feature that the Controller is owned by.
 * <p>
 * Each registered EndpointComponent is a Helix component. Using the ComponentRegistry
 * it is possible to enumerate all the endpoints.
 */

public class Endpoint implements Component {
    public static final String TYPE_NAME = "Endpoint";

    private String path;
    private HttpMethod[] httpMethods;
    private Method endpointMethod;
    private EndpointHandler endpointHandler;
    private Class requestBodyType;
    private Object controller;


    /**
     * Build an EndpointComponent using a fluent builder.
     *
     * @param path URL Path for the endpoint
     * @return The fluent builder
     */
    public static EndpointComponentBuilder forPath(String path) {
        return EndpointComponentBuilder.forPath(path);
    }

    /**
     * Build an EndpointComponent using a fluent builder.
     * The request type class will be automatically determined
     * by looking at the endpoint method's parameters.
     *
     * @param path URL Path for the endpoint
     * @param httpMethod HTTP method this endpoint accepts
     * @param method Method to invoke for this URL path
     * @param controller Controller object instanceto use
     * @return The fluent builder
     */
    public static Endpoint forPath(String path, HttpMethod httpMethod, Method method, Object controller) {
        return new Endpoint(path, new HttpMethod[] {httpMethod}, method, controller);
    }

    /**
     * Build an EndpointComponent using a fluent builder.
     * The request type class will be automatically determined
     * by looking at the endpoint method's parameters.
     *
     * @param path URL Path for the endpoint
     * @param httpMethods HTTP methods this endpoint accepts
     * @param method Method to invoke for this URL path
     * @param controller Controller object instanceto use
     * @return The fluent builder
     */
    public static Endpoint forPath(String path, HttpMethod[] httpMethods,  Method method, Object controller) {
        return (new Endpoint(path, httpMethods, method, controller));
    }

    /**
     * Build an EndpointComponent for a lambda-based endpoint,
     * with a request body type of String.
     *
     * @param path URL Path for the endpoint
     * @param httpMethod HTTP method this endpoint accepts
     * @param endpointHandler Functional endpoint to invoke
     * @return The fluent builder
     */
    public static Endpoint forPath(String path, HttpMethod httpMethod, EndpointHandler endpointHandler) {
        return new Endpoint(path, new HttpMethod[] {httpMethod}, endpointHandler);
    }

    /**
     * Build an EndpointComponent for a lambda-based endpoint,
     * with a specific request body type.
     *
     * @param path URL Path for the endpoint
     * @param httpMethod HTTP method this endpoint accepts
     * @param endpointHandler Functional endpoint to invoke
     * @param requestBodyType Type to marshall the incoming request to
     * @return The fluent builder
     */
    public static Endpoint forPath(String path,  HttpMethod httpMethod, EndpointHandler endpointHandler, Class requestBodyType) {
        return (new Endpoint(path, new HttpMethod[] {httpMethod}, endpointHandler, requestBodyType));
    }

    /**
     * Build an EndpointComponent for a lambda-based endpoint,
     * with a request body type of String.
     *
     * @param path URL Path for the endpoint
     * @param httpMethods HTTP methods this endpoint accepts
     * @param endpointHandler Functional endpoint to invoke
     * @return The fluent builder
     */
    public static Endpoint forPath(String path, HttpMethod[] httpMethods, EndpointHandler endpointHandler) {
        return (new Endpoint(path, httpMethods, endpointHandler));
    }

    /**
     * Build an EndpointComponent for a lambda-based endpoint,
     * with a specific request body type.
     *
     * @param path URL Path for the endpoint
     * @param httpMethods HTTP methods this endpoint accepts
     * @param endpointHandler Functional endpoint to invoke
     * @param requestBodyType Type to marshall the incoming request to
     * @return The fluent builder
     */
    public static Endpoint forPath(String path, HttpMethod[] httpMethods, EndpointHandler endpointHandler, Class requestBodyType) {
        return (new Endpoint(path, httpMethods, endpointHandler, requestBodyType));
    }

    private Endpoint(String path, HttpMethod[] httpMethods, Method endpointMethod, Object controller) {
        this.path = path;
        this.httpMethods = httpMethods;
        this.endpointMethod = endpointMethod;
        this.controller = controller;
        this.requestBodyType = getRequestBodyTypeForMethod(endpointMethod);
    }

    private Endpoint(String path, HttpMethod[] httpMethods, EndpointHandler endpointHandler) {
        this.path = path;
        this.httpMethods = httpMethods;
        this.endpointHandler = endpointHandler;
        this.requestBodyType = String.class;
    }

    private Endpoint(String path, HttpMethod[] httpMethods, EndpointHandler endpointHandler, Class requestBodyType) {
        this.path = path;
        this.httpMethods = httpMethods;
        this.endpointHandler = endpointHandler;
        this.requestBodyType = requestBodyType;
    }

    public Object getController() {
        return controller;
    }

    public EndpointHandler getEndpointHandler() {
        return endpointHandler;
    }

    public Class getRequestBodyType() {
        return requestBodyType;
    }

    public Method getEndpointMethod() {
        return endpointMethod;
    }

    public HttpMethod[] getHttpMethods() {
        return httpMethods;
    }

    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentType() {
        return TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        if (endpointMethod != null) {
            printWriter.print("'" + path + "' -> " + endpointMethod.getDeclaringClass().getSimpleName() + "." + endpointMethod.getName() + "() ");
            printWriter.print(Arrays.toString(httpMethods));
        } else {
            printWriter.print("'" + path + "' -> Lambda");
        }

        return stringWriter.toString();
    }

    private Class<?> getRequestBodyTypeForMethod(Method method) {
        Class<?> type = String.class;

        java.lang.reflect.Type methodParameterType = method.getGenericParameterTypes()[0];
        if (methodParameterType instanceof ParameterizedType) {
            type = (Class) ((ParameterizedType) methodParameterType).getActualTypeArguments()[0];
        }

        return type;
    }
}
