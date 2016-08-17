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

package io.helixservice.feature.restservice.error.jsonapi;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;

/**
 * Conforms to the JSON API spec for sending error responses
 *
 * @see <a href="http://jsonapi.org/format/#errors">jsonapi.org</a>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ErrorResponse {
    private int httpStatus;
    private Iterable<ErrorData> errors;

    /**
     * For serialization only
     */
    public ErrorResponse() {
    }

    /**
     * Create a new JSONAPI Error Response
     *
     * @param httpStatus httpStatus
     * @param errors JsonAPI ErrorData objects
     */
    public ErrorResponse(int httpStatus, Iterable<ErrorData> errors) {
        this.httpStatus = httpStatus;
        this.errors = errors;
    }

    /**
     * Create a new JSONAPI Error Response
     *
     * @param httpStatus httpStatus
     * @param errors JsonAPI ErrorData objects
     */
    public ErrorResponse(int httpStatus, ErrorData... errors) {
        this.httpStatus = httpStatus;
        this.errors = Arrays.asList(errors);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Iterable<ErrorData> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ErrorResponse {" +
                "httpStatus=" + httpStatus +
                ", errors=" + errors +
                '}';
    }
}
