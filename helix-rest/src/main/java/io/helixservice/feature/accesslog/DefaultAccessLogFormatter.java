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

package io.helixservice.feature.accesslog;

import com.google.common.base.MoreObjects;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.filter.FilterContext;

/**
 * The default access log formatter
 */
public class DefaultAccessLogFormatter extends AbstractAccessLogFormatter {
    private static final String CORRELATION_ID_HEADER = "CorrelationId";
    private static final String API_KEY_PARAMETER = "api_key";
    private static final String REFERRER_HEADER = "referrer";
    private static final String USER_AGENT_HEADER = "user-agent";

    public DefaultAccessLogFormatter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(FilterContext filterContext, long elapsedTimeMillis) {
        int responseStatusCode = filterContext.getResponse() == null ?
                500 : filterContext.getResponse().getHttpStatusCode();
        Request<?> request = filterContext.getRequest();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String version = getVersionString(request.getHttpVersion());
        String api_key = MoreObjects.firstNonNull(request.getParam(API_KEY_PARAMETER, null), "-");
        Object referrer = request.getHeader(REFERRER_HEADER, null);
        Object userAgent = request.getHeader(USER_AGENT_HEADER, null);
        Object uniqueRequestID = request.getHeader(CORRELATION_ID_HEADER, null);


        return "req_id=" + uniqueRequestID +
                " status=" + responseStatusCode +
                " http_method=" + method +
                " elapsed_millis=" + elapsedTimeMillis +
                " url=" + uri +
                " http_refer=" + referrer +
                " http_user_agent=" + userAgent +
                " host=" + getHostname() +
                " http_version=" + version +
                " api_key=" + api_key;
    }

    private String getVersionString(String version) {
        if (version == null) {
            version = "";
        } else if ("HTTP_1_1".equals(version)) {
            version = "HTTP/1.1";
        } else if ("HTTP_1_0".equals(version)) {
            version = "HTTP/1.0";
        }
        return version;
    }
}

