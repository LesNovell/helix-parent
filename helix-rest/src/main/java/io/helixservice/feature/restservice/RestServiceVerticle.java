
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

package io.helixservice.feature.restservice;

import io.helixservice.core.feature.Feature;
import io.helixservice.core.container.Container;
import io.helixservice.core.container.ContainerState;
import io.helixservice.feature.configuration.ConfigProperty;
import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.restservice.controller.VertxRequestHandler;
import io.helixservice.feature.restservice.controller.HttpMethod;
import io.helixservice.feature.restservice.controller.component.Endpoint;
import io.helixservice.feature.restservice.error.ErrorHandlerFunction;
import io.helixservice.feature.restservice.error.ErrorHandlerRegistry;
import io.helixservice.feature.restservice.filter.FilterHandler;
import io.helixservice.feature.restservice.filter.component.FilterComponent;
import io.helixservice.feature.restservice.marshal.Marshaller;
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

    private ConfigProvider configProvider;
    private final Container container;
    private final Router router;

    public RestServiceVerticle(ConfigProvider configProvider, Container container, Router router) {
        this.configProvider = configProvider;
        this.container = container;
        this.router = router;
    }

    @Override
    public void start() throws Exception {
        try {
            ConfigProperty port = new ConfigProperty(configProvider, "vertx.server.port");

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
            if (container.getContainerState() == ContainerState.FINISHING) {
                routingContext.response().setStatusCode(FINISHING_STATUS_CODE).end();
            } else {
                bodyHandler.handle(routingContext);
            }
        };
    }

    private void configureFeatures() {
        for (Feature feature : container.getFeatures()) {
            configureFilters(feature);
            configureEndpoints(feature);
        }
    }

    public Handler<HttpServerRequest> getHandler(Router router) {
        return fiberHandler(router::accept);
    }

    private void configureFilters(Feature feature) {
        Iterable<FilterComponent> filters = feature.findComponentByType(FilterComponent.TYPE_NAME);
        for (FilterComponent filter : filters) {
            router.routeWithRegex(filter.getPathRegex()).handler(fiberHandler(new FilterHandler(filter.getFilter())));
        }
    }

    private void configureEndpoints(Feature feature) {
        Collection<Endpoint> endpoints = feature.findComponentByType(Endpoint.TYPE_NAME);

        for (Endpoint endpoint : endpoints) {
            registerEndpoint(feature, endpoint);
        }
    }

    private void registerEndpoint(Feature feature, Endpoint endpoint) {
        Marshaller marshaller = feature.findComponentByType(Marshaller.TYPE_NAME, Marshaller.DEFAULT);
        Collection<ErrorHandlerFunction> errorHandlers = feature.findComponentByType(ErrorHandlerFunction.TYPE_NAME);

        ErrorHandlerRegistry errorHandlerRegistry = new ErrorHandlerRegistry();
        errorHandlerRegistry.addErrorHandlers(errorHandlers);

        VertxRequestHandler handler = new VertxRequestHandler(endpoint,
                marshaller, errorHandlerRegistry,
                vertx.eventBus());

        for (HttpMethod supportedMethod : endpoint.getHttpMethods()) {
            router.route(toVertxHttpMethod(supportedMethod), endpoint.getPath())
                    .handler(fiberHandler(handler));
        }
    }

    private static io.vertx.core.http.HttpMethod toVertxHttpMethod(HttpMethod supportedMethod) {
        return io.vertx.core.http.HttpMethod.valueOf(supportedMethod.name());
    }
}
