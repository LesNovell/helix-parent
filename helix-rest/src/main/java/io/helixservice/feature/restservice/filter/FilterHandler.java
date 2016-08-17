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

package io.helixservice.feature.restservice.filter;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Multimap;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import io.helixservice.core.util.VertxTypeConverter;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vert.x dispatch logic for Helix Filters
 */
public class FilterHandler implements Handler<RoutingContext> {
    private final static Logger LOG = LoggerFactory.getLogger(FilterHandler.class);
    public static final String FILTER_CHAIN = "FilterHandler.handleAfterChain";
    private Filter targetFilter;

    public FilterHandler(Filter targetFilter) {
        this.targetFilter = targetFilter;
    }

    @Override
    @Suspendable
    public void handle(RoutingContext routingContext) {
        try {
            FilterContext filterContext = buildFilterContext(routingContext);

            targetFilter.beforeHandleEndpoint(filterContext);

            routingContext.data().putAll(filterContext.getFilterVariables());
            copyMultiMap(filterContext.getRequest().getHeaders(), routingContext.request().headers());
            copyMultiMap(filterContext.getRequest().getParams(), routingContext.request().params());

            // Ensure beforeSendHeaders and afterResponseSent are called
            insertIntoFilterChain(routingContext);
            routingContext.response().headersEndHandler(event -> dispatchToFilterChain(routingContext, Filter::afterHandleEndpoint));
            routingContext.response().bodyEndHandler(event -> dispatchToFilterChain(routingContext, Filter::afterResponseSent));

            if (filterContext.isSendResponseFromFilter()) {
                // Filter decided to send its own response
                endWithResponse(routingContext, filterContext);
            } else {
                routingContext.next();
            }
        } catch (Throwable t) {
            LOG.error("Error occurred in handleBefore() on filterClassName=" + targetFilter.getClass().getName(), t);
            routingContext.next();
        }
    }

    private void insertIntoFilterChain(RoutingContext routingContext) {
        @SuppressWarnings("unchecked")
        List<FilterHandler> handleAfterChain = (List<FilterHandler>) routingContext.data().get(FILTER_CHAIN);

        if (handleAfterChain == null) {
            handleAfterChain = new ArrayList<>();
            routingContext.data().put(FILTER_CHAIN, handleAfterChain);
        }

        handleAfterChain.add(0, this);
    }

    @FunctionalInterface
    private interface FilterMethod {
        void dispatch(Filter filter, FilterContext filterContext) throws SuspendExecution;
    }

    @Suspendable
    private void dispatchToFilterChain(RoutingContext routingContext, FilterMethod filterMethod) {
        try {
            @SuppressWarnings("unchecked")
            List<FilterHandler> handleAfterChain = (List<FilterHandler>) routingContext.data().get(FILTER_CHAIN);
            if (handleAfterChain != null) {
                FilterContext filterContext = buildFilterContext(routingContext);

                for (FilterHandler filterHandler : handleAfterChain) {
                    filterMethod.dispatch(filterHandler.targetFilter, filterContext);
                }

                copyMultiMap(filterContext.getResponse().getHeaders(), routingContext.response().headers());
            }
        } catch (Throwable t) {
            LOG.error("Error occurred in handleAfter() filterClassName=" + targetFilter.getClass().getName(), t);
        }
    }

    private void endWithResponse(RoutingContext routingContext, FilterContext filterContext) {
        // Finish response, handler has made a decision
        copyMultiMap(filterContext.getResponse().getHeaders(), routingContext.response().headers());
        routingContext.response()
                .setChunked(true)
                .setStatusCode(filterContext.getResponse().getHttpStatusCode())
                .write(Buffer.buffer(filterContext.getResponse().getResponseBody()))
                .end();
    }

    private void copyMultiMap(Multimap<String,String> source, MultiMap destination) {
        destination.setAll(VertxTypeConverter.toVertxMultiMap(source));
    }

    private FilterContext buildFilterContext(RoutingContext routingContext) {
        return new FilterContext(buildRequest(routingContext),
                buildResponse(routingContext), routingContext.data());
    }

    private Response<byte[]> buildResponse(RoutingContext routingContext) {
        HttpServerResponse vertxResponse = routingContext.response();

        Response<byte[]> response = Response.fromHttpStatusCode(vertxResponse.getStatusCode());
        for (Map.Entry<String, String> header : vertxResponse.headers()) {
            response.addHeader(header.getKey(), header.getValue());
        }

        return response;
    }

    private Request<byte[]> buildRequest(RoutingContext routingContext) {
        HttpServerRequest vertxRequest = routingContext.request();

        return new Request<>(
                vertxRequest.method().name(), vertxRequest.uri(),
                VertxTypeConverter.toGuavaMultimap(vertxRequest.params()),
                VertxTypeConverter.toGuavaMultimap(vertxRequest.headers()),
                routingContext.getBody().getBytes(),
                vertxRequest.remoteAddress().host(),
                vertxRequest.version().name());
    }
}
