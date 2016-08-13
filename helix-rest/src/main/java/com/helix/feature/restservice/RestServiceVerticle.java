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

package com.helix.feature.restservice;

import com.helix.core.feature.Feature;
import com.helix.core.server.Server;
import com.helix.core.server.ServerState;
import com.helix.feature.configuration.ConfigProperty;
import com.helix.feature.restservice.controller.VertxRequestHandler;
import com.helix.feature.restservice.controller.HttpMethod;
import com.helix.feature.restservice.controller.component.EndpointComponent;
import com.helix.feature.restservice.error.ErrorHandler;
import com.helix.feature.restservice.error.ErrorHandlerRegistry;
import com.helix.feature.restservice.filter.FilterHandler;
import com.helix.feature.restservice.filter.component.FilterComponent;
import com.helix.feature.restservice.marshal.Marshaller;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static io.vertx.ext.sync.Sync.fiberHandler;

/**
 * Primary Verticle for the Helix REST Service
 */
@SuppressWarnings("unused")
public class RestServiceVerticle extends SyncVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(RestServiceVerticle.class);
    private static final int FINISHING_STATUS_CODE = 599;

    private final Server server;
    private final Router router;

    public RestServiceVerticle(Server server, Router router) {
        this.server = server;
        this.router = router;
    }

    @Override
    public void start() throws Exception {
        try {
            ConfigProperty port = new ConfigProperty("vertx.server.port");

            HttpServerOptions serverOptions = new HttpServerOptions().setPort(port.asInt());
            HttpServer httpServer = vertx.createHttpServer(serverOptions);

            router.route().handler(fiberHandler(finisher(BodyHandler.create())));
            configureFeatures();

            httpServer.requestHandler(getHandler(router)).listen();
        } catch (Throwable t) {
            LOG.error("Unable to start RestServiceVerticle", t);
            throw t;
        }
    }

    private Handler<RoutingContext> finisher(BodyHandler bodyHandler) {
        return routingContext -> {
            if (server.getServerState() == ServerState.FINISHING) {
                routingContext.response().setStatusCode(FINISHING_STATUS_CODE).end();
            } else {
                bodyHandler.handle(routingContext);
            }
        };
    }

    private void configureFeatures() {
        for (Feature feature : server.getFeatures()) {
            configureFilters(feature);
            configureEndpoints(feature);
        }
    }

    public Handler<HttpServerRequest> getHandler(Router router) {
        return fiberHandler(router::accept);
    }

    private void configureFilters(Feature feature) {
        Iterable<FilterComponent> filters = feature.findByType(FilterComponent.TYPE_NAME);
        for (FilterComponent filter : filters) {
            router.routeWithRegex(filter.getPathRegex()).handler(fiberHandler(new FilterHandler(filter.getFilter())));
        }
    }

    private void configureEndpoints(Feature feature) {
        Collection<EndpointComponent> endpoints = feature.findByType(EndpointComponent.TYPE_NAME);

        for (EndpointComponent endpointComponent : endpoints) {
            registerEndpoint(feature, endpointComponent);
        }
    }

    private void registerEndpoint(Feature feature, EndpointComponent endpointComponent) {
        Marshaller marshaller = feature.findByType(Marshaller.TYPE_NAME, Marshaller.DEFAULT);
        Collection<ErrorHandler> errorHandlers = feature.findByType(ErrorHandler.TYPE_NAME);

        ErrorHandlerRegistry errorHandlerRegistry = new ErrorHandlerRegistry();
        errorHandlerRegistry.addErrorHandlers(errorHandlers);

        VertxRequestHandler handler = new VertxRequestHandler(endpointComponent,
                marshaller, errorHandlerRegistry,
                vertx.eventBus());

        for (HttpMethod supportedMethod : endpointComponent.getHttpMethods()) {
            router.route(toVertxHttpMethod(supportedMethod), endpointComponent.getPath())
                    .handler(fiberHandler(handler));
        }
    }

    private static io.vertx.core.http.HttpMethod toVertxHttpMethod(HttpMethod supportedMethod) {
        return io.vertx.core.http.HttpMethod.valueOf(supportedMethod.name());
    }
}
