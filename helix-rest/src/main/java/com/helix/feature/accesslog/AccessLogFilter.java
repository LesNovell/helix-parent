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

package com.helix.feature.accesslog;

import co.paralleluniverse.fibers.SuspendExecution;
import com.helix.feature.restservice.filter.Filter;
import com.helix.feature.restservice.filter.FilterContext;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This filter gets called before and after each request, enabling the logging of each request.
 */
public class AccessLogFilter implements Filter {
    private static final String X_RESPONSE_TIME = "x-response-time";
    private static final String START_TIMESTAMP_VAR = "startTimestamp";

    private final Logger logger;
    private final AccessLogFormatter accessLogFormatter;

    /**
     * Create AccessLogFilter
     *
     * @param logger Logger to send the access log to
     * @param accessLogFormatter Formatter for generating the string to be logged
     */
    public AccessLogFilter(Logger logger, AccessLogFormatter accessLogFormatter) {
        this.logger = checkNotNull(logger);
        this.accessLogFormatter = checkNotNull(accessLogFormatter);
    }

    @Override
    public void beforeHandleEndpoint(FilterContext filterContext) throws SuspendExecution {
        filterContext.setFilterVariable(START_TIMESTAMP_VAR, System.currentTimeMillis());
    }

    @Override
    public void afterHandleEndpoint(FilterContext filterContext) throws SuspendExecution {
        long startTimestamp = lookupStartTimeVariable(filterContext);
        long responseTimeMillis = System.currentTimeMillis() - startTimestamp;

        String message = accessLogFormatter.format(filterContext, responseTimeMillis);
        logMessage(filterContext, message);

        filterContext.getResponse().addHeader(X_RESPONSE_TIME, String.valueOf(responseTimeMillis));
    }

    @Override
    public void afterResponseSent(FilterContext filterContext) throws SuspendExecution {
    }

    private Long lookupStartTimeVariable(FilterContext filterContext) {
        Long startTime = filterContext.getFilterVariable(START_TIMESTAMP_VAR);
        if (startTime == null) {
            throw new IllegalArgumentException("Start Timestamp should not be null");
        }
        if (startTime <= 0) {
            throw new IllegalArgumentException("Start Timestamp should be a positive number");
        }
        return startTime;
    }

    private void logMessage(FilterContext filterContext, String message) {
        int status = filterContext.getResponse() == null ? 500 : filterContext.getResponse().getHttpStatusCode();

        if (status >= 500 || status >= 404) {
            logger.error(message);
        } else if (status >= 300) {
            logger.warn(message);
        } else {
            logger.info(message);
        }
    }
}
