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

import com.helix.core.feature.AbstractFeature;
import com.helix.feature.configuration.ConfigProperty;
import com.helix.feature.restservice.filter.component.FilterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access Logging Feature logs details about each HTTP request
 * <p>
 * Configuration Properties:
 * <ul>
 *     <li>access-log.loggerName: Name of logger category to use</li>
 *     <li>access-log.formatter: Class name of a AccessLogFormatter</li>
 * </ul>
 */
public class AccessLogFeature extends AbstractFeature {
    public AccessLogFeature() {
        String loggerName = new ConfigProperty("access-log.loggerName").getValue();
        String accessLogFormatterClassName = new ConfigProperty("access-log.formatter").getValue();

        try {
            Logger logger = LoggerFactory.getLogger(loggerName);

            AccessLogFormatter accessLogFormatter =
                    (AccessLogFormatter) Class.forName(accessLogFormatterClassName).newInstance();

            AccessLogFilter accessLogFilter = new AccessLogFilter(logger, accessLogFormatter);
            register(FilterComponent.filterAllPaths(accessLogFilter));
        } catch (Throwable t) {
            throw new IllegalArgumentException("Unable to create AccessLogFormatter className=" + accessLogFormatterClassName, t);
        }
    }
}
