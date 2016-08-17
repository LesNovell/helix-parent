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

package io.helixservice.feature.restservice.controller;

import co.paralleluniverse.fibers.Suspendable;
import io.helixservice.core.util.VertxTypeConverter;
import io.helixservice.feature.restservice.controller.component.EndpointComponent;
import io.helixservice.feature.restservice.controller.metrics.RequestMetricsPublisher;
import io.helixservice.feature.restservice.error.ErrorHandler;
import io.helixservice.feature.restservice.error.ErrorHandlerRegistry;
import io.helixservice.feature.restservice.marshal.Marshaller;
import io.helixservice.feature.restservice.marshal.Message;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Accepts incoming HTTP requests from Vert.x and routes them
 * to the appropriate REST controller.  Marshals request and
 * response bodies to the correct types.  Handles exceptions thrown
 * by Controller methods and dispatches to the correct ErrorHandler.
 * <p>
 * The EndpointHandler publishes metrics on each REST request
 * to the Vert.x EventBus.
 */
public class VertxRequestHandler implements Handler<RoutingContext> {
    public static final String CONTENT_TYPE = "content-type";
    private static final Logger LOG = LoggerFactory.getLogger(VertxRequestHandler.class);
    private EndpointComponent endpointComponent;

    private String path;
    private EventBus eventBus;

    private Marshaller marshaller;
    private ErrorHandlerRegistry errorHandlerRegistry;

    /**
     * Create an EndpointHandler
     *
     * @param endpointComponent Endpoint definition
     * @param marshaller Marshaller to be used for this endpoint handler
     * @param errorHandlerRegistry Registry of error handlers
     * @param eventBus Event bus for publishing controller metrics
     */
    public VertxRequestHandler(EndpointComponent endpointComponent,
            Marshaller marshaller, ErrorHandlerRegistry errorHandlerRegistry, EventBus eventBus) {
        this.endpointComponent = endpointComponent;
        this.marshaller = marshaller;
        this.errorHandlerRegistry = errorHandlerRegistry;
        this.path = endpointComponent.getPath();
        this.eventBus = eventBus;
    }

    /**
     * Handles incoming Vert.x request, forwarding it to the appropriate controller.
     *
     * @param event Vert.x Web request context
     */
    @Suspendable
    public void handle(RoutingContext event) {
        Request<?> request = null;

        RequestMetricsPublisher requestMetricsPublisher = new RequestMetricsPublisher(eventBus, path);

        try {
            HttpServerRequest vertxRequest = event.request();
            requestMetricsPublisher.setHttpMethod(event.request().method().name());

            request = new Request<>(
                    vertxRequest.method().name(), vertxRequest.uri(),
                    VertxTypeConverter.toGuavaMultimap(vertxRequest.params()),
                    VertxTypeConverter.toGuavaMultimap(vertxRequest.headers()),
                    unmarshalRequestBody(event), vertxRequest.remoteAddress().host(),
                    vertxRequest.version().name());

            Response response;
            Method method = endpointComponent.getEndpointMethod();
            if (method != null) {
                response = (Response) method.invoke(endpointComponent.getController(), request);
            } else {
                Endpoint endpoint = endpointComponent.getEndpoint();
                response = endpoint.handle(request);
            }

            //noinspection unchecked
            event.response().headers().addAll(VertxTypeConverter.toVertxMultiMap(response.getHeaders()));
            Message responseMessage = marshalResponseBody(response);

            Buffer responseBuffer = Buffer.buffer(responseMessage.getBody());
            event.response()
                    .setChunked(true)
                    .setStatusCode(response.getHttpStatusCode())
                    .putHeader(CONTENT_TYPE, responseMessage.getContentTypes())
                    .write(responseBuffer)
                    .end();

            requestMetricsPublisher.setResponseSize(responseBuffer.length());
            requestMetricsPublisher.setSuccess(response.getHttpStatusCode() >= 200 && response.getHttpStatusCode() <= 299);
        } catch (InvocationTargetException t) {
            int responseSize = handleErrorResponse(event, request, t.getCause());
            requestMetricsPublisher.setResponseSize(responseSize);
        } catch (Throwable t) {
            int responseSize = handleErrorResponse(event, request, t);
            requestMetricsPublisher.setResponseSize(responseSize);
        } finally {
            requestMetricsPublisher.publish();
        }
    }

    private Object unmarshalRequestBody(RoutingContext event) throws java.io.IOException {
        Object result;

        byte[] body = event.getBody().getBytes();
        List<String> contentTypeHeaders = event.request().headers().getAll(CONTENT_TYPE);
        result = marshaller.unmarshal(endpointComponent.getRequestBodyType(), new Message(body, contentTypeHeaders));

        return result;
    }

    private Message marshalResponseBody(Response response) {
        return marshaller.marshal(response.getResponseBody());
    }

    private int handleErrorResponse(RoutingContext event, Request request, Throwable t) {
        ErrorHandler<Throwable> errorHandler = errorHandlerRegistry.errorHandlerFor(t);

        Response<?> errorResponse = errorHandler.mapToErrorResponse(request, t);
        Object responseBody = errorResponse.getResponseBody();

        byte[] marshaledResponseBody;
        try {
            marshaledResponseBody = marshaller.marshal(responseBody).getBody();
        } catch (Throwable tMarshaller) {
            String marshallerErrorResponse = "Unable to marshal response object";
            LOG.error(marshallerErrorResponse, tMarshaller);
            marshaledResponseBody = marshallerErrorResponse.getBytes();
        }

        Buffer buffer = Buffer.buffer(marshaledResponseBody);

        event.response().headers().addAll(VertxTypeConverter.toVertxMultiMap(errorResponse.getHeaders()));
        event.response()
                .setStatusCode(errorResponse.getHttpStatusCode())
                .setChunked(true)
                .write(buffer)
                .end();

        return buffer.length();
    }
}
