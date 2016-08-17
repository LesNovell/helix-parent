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

package io.helixservice.feature.context;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.restservice.filter.component.FilterComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * RequestContextFeature provides a context for storing information specific
 * to a request.  The RequestContext is available during the processing of
 * a Controller's endpoint.
 * <p>
 * RequestContext should be used for cross-cutting concerns such as transaction
 * identifiers and security parameters that are not naturally passed via method
 * parameters.  It's strongly discouraged to use the RequestContext for storing
 * general request related data.
 * <p>
 * In an async framework such as Vert.x, your handlers are not guaranteed to
 * be called by any particular thread. This means, using of ThreadLocal for
 * storage of security or transaction context information is impossible.
 * <p>
 * RequestContextFeature uses AspectJ to weave in the automatic propagation
 * of the RequestContext.  This approach is able to propagate the context
 * for any Vert.x Handler callback including execution of Blocking Worker code.
 * <p>
 * RequestContextFeature provides some very common operations that are done
 * with a micro-service environment, hopefully saving some additional work.
 *
 * <h2>Capturing Headers and URI Params</h2>
 * Headers and URI parameters to incoming requests can be captured
 * and recorded in the RequestContext automatically.  This is very useful
 * for security or tracing values which need to be propagated.
 *
 * <h2>Forwarding Headers to HTTP Client</h2>
 * Another common need is to ensure certain headers are always
 * copied from the incoming request to outgoing requests.  For example,
 * tracing or transaction identifiers may need to be added to each outgoing
 * HTTP request. This feature allows the automatic forwarding of
 * headers you specify.
 *
 * <h2>Logging Context Variables</h2>
 * Logging of important values is a common concern.  For example, it's
 * common to log the userId, transactionId, and other values on every
 * log line relating to the request.  Normally, this is achieved using
 * the log provider's MDC context which copies values into a ThreadLocal.
 * Unfortunately, ThreadLocals do not work in Vert.x -- making consistent
 * logging of tracing values tedious.  This feature provides an automatic
 * way to specify which context variables should always be logged.
 *
 * <h2>AspectJ Configuration</h2>
 * <li>AspectJ agent must be installed on the Java command line, for example: <pre>-javaagent:aspectjweaver-1.8.8.jar</pre></li>
 * <li>AspectJ aop.xml must contain <pre>&lt;aspect name="io.helixservice.feature.context.RequestContextAspect"\&gt;</pre></li>
 *
 */
public class RequestContextFeature extends AbstractFeature {
    List<String> captureHeaders = new ArrayList<>();
    List<String> captureParams = new ArrayList<>();

    List<String> contextNamesToLog = new ArrayList<>();

    Map<String, String> propagateHeaders = new HashMap<>();
    Map<String, String> responseHeaders = new HashMap<>();

    Multimap<String, Function<String,String>> transformerMap = ArrayListMultimap.create();
    Function<String,String> renameFn = Function.identity();

    Consumer<RequestContext> contextTransformer = rc -> {};

    /**
     * Create RequestContextFeature, which will register a filter that
     * creates a context associated with every HTTP request
     */
    public RequestContextFeature() {
        register(FilterComponent.filterAllPaths(new RequestContextFilter(this)));
    }

    /**
     * @return Returns the current request context
     */
    public static RequestContext getRequestContext() { return RequestContext.getContext(); }

    /**
     * Configure that the header should be captured into the RequestContext automatically.
     *
     * @param headerName Header name to find and add to the RequestContext
     * @return A fluent interface for further configuring the header capture process
     */
    public HeaderCapture captureHeader(String headerName) {
        return new HeaderCapture(headerName);
    }

    /**
     * Configure that the URI param that should be captured into the RequestContext automatically.
     *
     * @param paramName URI param name to find and add to the RequestContext
     * @return A fluent interface for further configuring the param capture process
     */
    public UriParamCapture captureUriParam(String paramName) {
        return new UriParamCapture(paramName);
    }

    /**
     * Add a custom transformer, which enables arbitrary logic that can
     * modify context variables as needed.
     *
     * @param contextTransformer Context transformer to add
     */
    public void addContextTransformer(Consumer<RequestContext> contextTransformer) {
        this.contextTransformer = this.contextTransformer.andThen(contextTransformer);
    }

    /**
     * Fluent interface for configuring header capture options
     */
    public class HeaderCapture {
        private String headerName;

        HeaderCapture(String headerName) {
            this.headerName = headerName;
            captureHeaders.add(headerName);
        }

        /**
         * Send the header out in all outgoing HTTP Requests
         * @return this fluent interface
         */
        public HeaderCapture propagateToOutgoingRequests() {
            return propagateToOutgoingRequests(headerName);
        }

        /**
         * Send this header out in all outgoing HTTP requests, as a different header name
         * @param outgoingHeaderName Outgoing request header name
         * @return this fluent interface
         */
        public HeaderCapture propagateToOutgoingRequests(String outgoingHeaderName) {
            propagateHeaders.put(headerName, outgoingHeaderName);
            return this;
        }

        /**
         * Causes the header to be sent back in the response  (echo back)
         * @return this fluent interface
         */
        public HeaderCapture copyAsResponseHeader() {
            return copyAsResponseHeader(headerName);
        }

        /**
         * Causes the header to be sent back in the response  (echo back) as a different header name
         * @param newHeaderName Header name to send back in the response
         * @return this fluent interface
         */
        public HeaderCapture copyAsResponseHeader(String newHeaderName) {
            responseHeaders.put(headerName, newHeaderName);
            return this;
        }

        /**
         * Causes the header to be added to all log messages
         * @return this fluent interface
         */
        public HeaderCapture addToLogMessages() {
            contextNamesToLog.add(headerName);
            return this;
        }

        /**
         * Checks if the header is empty, if so calls a function to initialize the header value in the request context
         * @param initializer Initialization function
         * @return this fluent interface
         */
        public HeaderCapture ifEmptyThenInitializeTo(Supplier<String> initializer) {
            transform(t -> t == null || t.isEmpty() ? initializer.get() : t);
            return this;
        }

        /**
         * Register a transformer that allows arbitrary modifications to header values
         * @param valueTransformer Value transformer function, with headerName and headerValue as input
         * @return this fluent interface
         */
        public HeaderCapture transform(Function<String,String> valueTransformer) {
            transformerMap.put(headerName, valueTransformer);
            return this;
        }

        /**
         * Rename the header as it's copied into the RequestContext
         * @param newHeaderName Name this header should have in the RequestContext
         * @return this fluent interface
         */
        public HeaderCapture renameTo(String newHeaderName) {
            rename(s -> headerName.equals(s) ? newHeaderName : headerName);
            return this;
        }

        /**
         * Provides an arbitrary renaming function for headers copied into the RequestContext
         * @param pRenameFn Header renaming function, with headerName and headerValue as input
         * @return this fluent interface
         */
        public HeaderCapture rename(Function<String,String> pRenameFn) {
            renameFn = renameFn.andThen(pRenameFn);
            return this;
        }
    }


    /**
     * Fluent interface for configuring uri parameter capture options
     */
    public class UriParamCapture {
        private String paramName;

        UriParamCapture(String paramName) {
            this.paramName = paramName;
            captureParams.add(paramName);
        }

        /**
         * Causes the uri param to be added to all log messages
         * @return this fluent interface
         */
        public UriParamCapture addToLogMessages() {
            contextNamesToLog.add(paramName);
            return this;
        }

        /**
         * Causes the uri param to be echoed as a specific response header back to the client
         * @return this fluent interface
         */
        public UriParamCapture copyAsResponseHeader(String copyToHeaderName) {
            responseHeaders.put(paramName, copyToHeaderName);
            return this;
        }

        /**
         * Checks if the uri parameter is empty, if so calls a function to initialize the value in the request context
         * @param initializer Initialization function
         * @return this fluent interface
         */
        public UriParamCapture ifEmptyThenInitializeTo(Supplier<String> initializer) {
            transform(t -> t == null || t.isEmpty() ? initializer.get() : t);
            return this;
        }

        /**
         * Register a transformer that allows arbitrary modifications to uri param values
         * @param valueTransformer Value transformer function, with paramName and paramValue as input
         * @return this fluent interface
         */
        public UriParamCapture transform(Function<String,String> valueTransformer) {
            transformerMap.put(paramName, valueTransformer);
            return this;
        }

        /**
         * Rename the uri param as it's copied into the RequestContext
         * @param newParamName Name this param should have in the RequestContext
         * @return this fluent interface
         */
        public UriParamCapture renameTo(String newParamName) {
            rename(s -> paramName.equals(s) ? newParamName : paramName);
            return this;
        }

        /**
         * Provides an arbitrary renaming function for params copied into the RequestContext
         * @param pRenameFn Renaming function, with paramName and paramValue as input
         * @return this fluent interface
         */
        public UriParamCapture rename(Function<String,String> pRenameFn) {
            renameFn = renameFn.andThen(pRenameFn);
            return this;
        }
    }
}
