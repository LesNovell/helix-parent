
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

package io.helixservice.feature.restservice.error;

import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import io.helixservice.feature.restservice.error.jsonapi.ErrorData;
import io.helixservice.feature.restservice.error.jsonapi.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Error handler, if no other error handler is found.
 * Returns an HTTP error code and logs the exception.
 */
public class DefaultErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultErrorHandler.class);

    public static Response<ErrorResponse> defaultExceptionMapper(Request request, Throwable t) {
        ErrorResponse errorResponse = new ErrorResponse(500,
                new ErrorData(t.getClass().getSimpleName(), "Request Failed", t.getMessage()));

        LOG.error(errorResponse.toString(), t);

        return Response.jsonAPIErrorResponse(errorResponse);
    }

}
