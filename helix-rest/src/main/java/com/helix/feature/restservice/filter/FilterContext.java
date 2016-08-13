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

package com.helix.feature.restservice.filter;

import com.helix.feature.restservice.controller.Request;
import com.helix.feature.restservice.controller.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Request context which is passed to the Filter implementation
 * <p>
 * Filters should not rely upon RequestContext, instead use this
 * FilterContext for keeping state.
 * <p>
 * Used for keeping request-specific values between before/after calls
 * int the filter implementation.  These variables are request and filter
 * specific, and get garbage collected after the request completes.
 *
 */
public class FilterContext {
    private Request<byte[]> request;
    private Response<byte[]> response;
    private Map<String, Object> filterVariables = new HashMap<>();
    private boolean sendResponseFromFilter = false;

    /**
     * Create filter context
     *
     * @param request Request
     * @param response Response
     * @param filterVariables Map of data the filter is tracking
     */
    public FilterContext(Request<byte[]> request, Response<byte[]> response, Map<String, Object> filterVariables) {
        this.request = request;
        this.response = response;
        this.filterVariables.putAll(filterVariables);
    }

    /**
     * Get the request object this filter is handling
     * <p>
     * Changes to request data will be sent to Controllers
     * that will be invoked after the filter.
     *
     * @return The mutable request object
     */
    public Request<?> getRequest() {
        return request;
    }

    /**
     * Get the response object, which can be modified by this filter
     * <p>
     * Changes to responses sent by controllers is allowed.
     *
     * @return The mutable response object
     */
    public Response<byte[]> getResponse() {
        return response;
    }

    /**
     * @return Mutable map of variables
     */
    public Map<String, Object> getFilterVariables() {
        return filterVariables;
    }

    /**
     * Get a filter variable value
     *
     * @param name Name of the variable
     * @param  T type of the value returned
     * @return A value, or null if one does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> T getFilterVariable(String name) {
        return (T) filterVariables.get(name);
    }

    /**
     * Set a filter variable value
     *
     * @param name Name of the variable
     * @param value Value to be set
     * @return This filter context
     */
    public FilterContext setFilterVariable(String name, Object value) {
        filterVariables.put(name, value);
        return this;
    }

    /**
     * End the request, without forwarding it to a Controller endpoint.
     *
     * @param httpStatusCode HTTP status code to send
     * @param responseBody Response body text to send
     * @return This filter context
     */
    public FilterContext endWithResponse(int httpStatusCode, String responseBody) {
        this.response.setHttpStatusCode(httpStatusCode);
        this.response.setResponseBody(responseBody.getBytes());
        this.sendResponseFromFilter = true;
        return this;
    }

    boolean isSendResponseFromFilter() {
        return sendResponseFromFilter;
    }
}
