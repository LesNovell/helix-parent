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

package com.helix.feature.context;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.impl.HttpClientRequestImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This Aspect weaves in logic to correctly propagate the context.
 * The weaving logic here is specific to Vert.x and LogBack.
 */
@Aspect
public class RequestContextAspect {
    private static Logger LOG = LoggerFactory.getLogger(RequestContextAspect.class);
    private RequestContextFeature requestContextFeature;

    /**
     * When RequestContextFeature is created, copies a reference for later use.
     *
     * @param requestContextFeature The newly created feature
     */
    @AfterReturning(value = "execution(com.helix.feature.context.RequestContextFeature.new(..)) "
            + "&& this(requestContextFeature)", argNames = "requestContextFeature")
    public void onNewFeatureCreation(RequestContextFeature requestContextFeature) {
        this.requestContextFeature = requestContextFeature;
    }

    /**
     * Wraps Vert.x handlers and copies the context on context invocation
     */
    @Around(value = "(execution(public !static * *(.., io.vertx.core.Handler, ..)))")
    public Object aroundHandlerMethods(ProceedingJoinPoint pjp)
            throws Throwable, SuspendExecution {

        Object[] args = pjp.getArgs();

        RequestContext context = RequestContext.getContext();
        if (context != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && Handler.class.isAssignableFrom(args[i].getClass())) {
                    args[i] = new ContextCopyingHandler((Handler) args[i], context);
                }
            }
        }

        return pjp.proceed(args);
    }

    /**
     * Weave in logic before LogBack MDC property lookup, to add our context variables to log
     */
    @Around(value = "(execution(public * ch.qos.logback.classic.util.LogbackMDCAdapter.getPropertyMap()))")
    public Map<String, String> aroundLogbackGetPropertyMap(ProceedingJoinPoint pjp)
            throws Throwable, SuspendExecution {

        @SuppressWarnings("unchecked")
        Map<String, String> propertyMap = (Map<String, String>) pjp.proceed();

        RequestContext context = RequestContext.getContext();
        if (context != null && !requestContextFeature.contextNamesToLog.isEmpty()) {
            propertyMap = propertyMap == null ? new HashMap<>() : new HashMap<>(propertyMap);
            for (String contextName : requestContextFeature.contextNamesToLog) {
                propertyMap.put(contextName, context.getValue(contextName));
            }
            propertyMap.put("log.ctx", context.getValue("log.ctx"));
        }

        return propertyMap;
    }

    /**
     * Weave in logic just before sending a request to copy any headers we should forward into the outgoing request
     */
    @Around(value = "(execution(private void io.vertx.core.http.impl.HttpClientRequestImpl.prepareHeaders())) "
            + "&& this(httpClientRequestImpl)", argNames = "pjp, httpClientRequestImpl")
    public void aroundPrepareHeaders(ProceedingJoinPoint pjp, HttpClientRequestImpl httpClientRequestImpl)
            throws Throwable, SuspendExecution {

        RequestContext context = RequestContext.getContext();
        if (context != null && !requestContextFeature.propagateHeaders.isEmpty()) {
            MultiMap headers = httpClientRequestImpl.headers();

            for (Map.Entry<String, String> contextHeaderEntry : requestContextFeature.propagateHeaders.entrySet()) {
                String value = context.getValue(contextHeaderEntry.getKey());
                if (value != null) {
                    headers.set(contextHeaderEntry.getValue(), value);
                }
            }
        }

        pjp.proceed();
    }

    public static class ContextCopyingHandler implements Handler {
        private final RequestContext context;
        private final Handler handler;

        public ContextCopyingHandler(Handler handler, RequestContext context) {
            this.context = context;
            this.handler = handler;
        }

        @Override
        @Suspendable
        public void handle(Object e) {
            RequestContext.setContext(context);
            try {
                Method handle = handler.getClass().getMethod("handle", Object.class);
                handle.setAccessible(true);
                handle.invoke(handler, e);
            } catch (NoSuchMethodException | IllegalAccessException t) {
                logUnthinkableException(t);
            } catch (InvocationTargetException t) {
                if (RuntimeException.class.isAssignableFrom(t.getCause().getClass())) {
                    throw (RuntimeException) t.getCause();
                } else {
                    logUnthinkableException(t);
                }
            }
        }

        private void logUnthinkableException(ReflectiveOperationException t) {
            LOG.error("Unexpected exception from handler", t);
        }
    }
}
