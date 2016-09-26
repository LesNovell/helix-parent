
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

package io.helixservice.feature.accesslog;

import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.restservice.filter.FilterContext;

/**
 * Implement this interface to provide a custom access log format
 */
public interface AccessLogFormatter {
    /**
     * Sets the configuration provider
     *
     * @param configProvider Configuration provider to use
     */
    void setConfigProvider(ConfigProvider configProvider);

    /**
     * Format the access log entry, returning a string that will be logged.
     * This method is called after the request completes.
     *
     * @param filterContext Context containing request, response, and other data that might be useful to log
     * @param elapsedTimeMillis Elapsed time in milliseconds of the request
     * @return The formatted string that will be logged to the access log
     */
    String format(FilterContext filterContext, long elapsedTimeMillis);
}
