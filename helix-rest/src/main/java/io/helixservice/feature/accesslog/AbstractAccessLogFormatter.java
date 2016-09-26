
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
import io.helixservice.feature.configuration.provider.ConfigProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Extend this class to reuse common access log formatter code
 */
public abstract class AbstractAccessLogFormatter implements AccessLogFormatter {
    private static Logger LOG = LoggerFactory.getLogger(AbstractAccessLogFormatter.class);
    private String hostname = null;
    private ConfigProvider configProvider;

    @Override
    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Resolve and get the current host name.
     * Host name resolution will only occur once, thereafter it's cached.
     *
     * @return The hostname
     */
    public String getHostname() {
        if (hostname == null)
            resolveHostname();

        return hostname;
    }

    void setHostname(String hostname) {
        this.hostname = hostname;
    }

    private synchronized String resolveHostname() {
        try {
            hostname = configProvider.propertyByName("access-log.hostname").getValue();
        } catch (ConfigProviderException e) {
            hostname = null;
        }

        if (hostname == null || hostname.isEmpty() || hostname.equals("resolve")) {
            hostname = resolveServerHost();
        }

        return hostname;
    }

    private String resolveServerHost() {
        String serverHostName = "unknown";

        try {
            // May need additional methods to resolve hostname
            // Works on Linux, MacOS X
            Process proc = Runtime.getRuntime().exec("hostname");
            try (InputStream stream = proc.getInputStream()) {
                try (Scanner s = new Scanner(stream).useDelimiter("\\A")) {
                    serverHostName = s.hasNext() ? s.next() : "";
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to determine host name via exec", e);
        }

        return serverHostName;
    }
}
