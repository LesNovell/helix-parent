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

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * RequestContext holds the current variables in the scope of the Controller's request
 * <p>
 * RequestContext supports simple name/value pairs as strings.  This is not the right
 * place to store a lot of state, but is a good place to store values that are cross-cutting
 * that cannot be passed as parameters such as transaction or security related values.
 */
public class RequestContext {
    private static final Map<Long, RequestContext> threadIdToRequestContext = new HashMap<>();

    private Map<String, String> contextVarMap;
    private Set<String> loggedContextVars;

    private RequestContext() {
        loggedContextVars = new HashSet<>();
        contextVarMap = new HashMap<>();
    }

    /**
     * Get the current request context
     *
     * @return The current request context
     */
    public static RequestContext getContext() {
        return threadIdToRequestContext.get(Thread.currentThread().getId());
    }

    /**
     * Set a value in the request context
     *
     * @param name Name of the value
     * @param value Value as a String to add to the request context
     */
    public void setValue(String name, String value) {
        contextVarMap.put(name, value);
        updateLoggedContextVar();
    }

    /**
     * Get a value from the request context
     *
     * @param name Name of the value
     * @return The value from the current request context
     */
    public String getValue(String name) {
        return contextVarMap.get(name);
    }

    /**
     * Removes a value from the request context
     *
     * @param name Name of the value
     */
    public void clearValue(String name) {
        contextVarMap.remove(name);
        updateLoggedContextVar();
    }

    /**
     * Gets the current request context as a Map
     *
     * @return Immutable Map containing the request context's contained name and values
     */
    public Map<String, String> getContextVarMap() {
        return Collections.unmodifiableMap(contextVarMap);
    }

    /**
     * Create a new empty context, called right before Controllers or Filters are invoked
     * @return The newly created request context
     */
    static RequestContext createEmptyContext() {
        RequestContext requestContext = new RequestContext();
        setContext(requestContext);
        return requestContext;
    }

    /**
     * Clear the current context. Called after a request has been handled.
     */
    static void clearContext() {
        threadIdToRequestContext.remove(Thread.currentThread().getId());
    }

    /**
     * Set a request context to be associated with the current thread.
     * Used to propagate the context across thread boundaries.
     *
     * @param context The context to associate with this thread.
     */
    static void setContext(RequestContext context) {
        threadIdToRequestContext.put(Thread.currentThread().getId(), context);
    }

    /**
     * Set a list of context variables that we will always log if present
     * @param loggedContextVars List of context variable names to log
     */
    void setLoggedContextVars(Set<String> loggedContextVars) {
        this.loggedContextVars = loggedContextVars;
    }

    /**
     * Should be called after any changes to a context, this
     * regenerates the context values that will be logged
     */
    private void updateLoggedContextVar() {
        if (loggedContextVars != null) {
            StringWriter logContextSW = new StringWriter();

            for (String varName : loggedContextVars) {
                String value = contextVarMap.get(varName);
                if (value != null) {
                    logContextSW.append(varName + "=" + value + " ");
                }
            }

            String logContext = logContextSW.toString().trim();
            if (logContext.isEmpty()) {
                contextVarMap.remove("log.ctx");
            } else {
                contextVarMap.put("log.ctx", logContext);
            }
        }
    }
}
