
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

package io.helixservice.feature.context;

import co.paralleluniverse.fibers.SuspendExecution;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.filter.Filter;
import io.helixservice.feature.restservice.filter.FilterContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * This filter adds, initializes, and then finally
 * removes the RequestContext for each HTTP request
 */
public class RequestContextFilter implements Filter {
    private RequestContextFeature feature;

    public RequestContextFilter(RequestContextFeature requestContextFeature) {
        this.feature = requestContextFeature;
    }

    /**
     * Before any controller endpoints get a chance to run,
     * capture headers and params then setup a new RequestContext.
     *
     * @param filterContext Filter context
     * @throws SuspendExecution For Vert.x Sync
     */
    @Override
    public void beforeHandleEndpoint(FilterContext filterContext) throws SuspendExecution {
        RequestContext newContext = RequestContext.createEmptyContext();

        setLoggedContextVars(newContext);
        captureHeaders(newContext, filterContext.getRequest());
        captureParams(newContext, filterContext.getRequest());
        feature.contextTransformer.accept(newContext);
    }

    /**
     * After controller endpoint has run, set any response headers
     * before the response has been written.
     *
     * @param filterContext Filter context
     * @throws SuspendExecution For Vert.x Sync
     */
    @Override
    public void afterHandleEndpoint(FilterContext filterContext) throws SuspendExecution {
        setResponseHeaders(filterContext);
    }

    /**
     * After the response has been written, clear our context
     *
     * @param filterContext Filter context
     * @throws SuspendExecution For Vert.x Sync
     */
    @Override
    public void afterResponseSent(FilterContext filterContext) throws SuspendExecution {
        RequestContext.clearContext();
    }

    private void setResponseHeaders(FilterContext filterContext) {
        RequestContext context = RequestContext.getContext();

        for (Map.Entry<String, String> entry : feature.responseHeaders.entrySet()) {
            String value = context.getValue(entry.getKey());
            if (value != null) {
                filterContext.getResponse().addHeader(entry.getValue(), value);
            }
        }
    }

    private void captureHeaders(RequestContext context, Request request) {
        for (String headerName : feature.captureHeaders) {
            String value = request.getHeader(headerName, null);
            context.setValue(runRename(headerName), runTransform(headerName, value));
        }
    }

    private void captureParams(RequestContext context, Request request) {
        for (String paramName : feature.captureParams) {
            String value = request.getParam(paramName, null);
            context.setValue(runRename(paramName), runTransform(paramName, value));
        }
    }

    private String runRename(String contextName) {
        return Optional.of(contextName).map(feature.renameFn).get();
    }

    private String runTransform(String contextName, String value) {
        String result = value;

        Collection<Function<String, String>> transformers = feature.transformerMap.get(contextName);
        for (Function<String, String> transformer : transformers) {
            result = transformer.apply(result);
        }

        return result;
    }

    private void setLoggedContextVars(RequestContext context) {
        Set<String> loggedContextVars = new HashSet<>();
        loggedContextVars.addAll(feature.contextNamesToLog);
        context.setLoggedContextVars(loggedContextVars);
    }
}
