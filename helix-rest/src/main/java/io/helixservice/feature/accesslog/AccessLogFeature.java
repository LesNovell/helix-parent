
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

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.restservice.filter.component.FilterComponent;
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
    public AccessLogFeature(ConfigProvider configProvider) {
        String loggerName = configProvider.propertyByName("access-log.loggerName").getValue();
        String accessLogFormatterClassName = configProvider.propertyByName("access-log.formatter").getValue();

        try {
            AccessLogFormatter accessLogFormatter = (AccessLogFormatter)
                    Class.forName(accessLogFormatterClassName).newInstance();
            accessLogFormatter.setConfigProvider(configProvider);

            Logger logger = LoggerFactory.getLogger(loggerName);
            AccessLogFilter accessLogFilter = new AccessLogFilter(logger, accessLogFormatter);
            register(FilterComponent.filterAllPaths(accessLogFilter));
        } catch (Throwable t) {
            throw new IllegalArgumentException("Unable to create AccessLogFormatter className=" + accessLogFormatterClassName, t);
        }
    }
}
