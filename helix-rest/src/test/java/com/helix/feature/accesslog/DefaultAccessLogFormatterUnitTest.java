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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.helix.feature.restservice.controller.HttpMethod;
import com.helix.feature.restservice.controller.Request;
import com.helix.feature.restservice.controller.Response;
import com.helix.feature.restservice.filter.FilterContext;
import io.vertx.core.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAccessLogFormatterUnitTest {
    DefaultAccessLogFormatter subject;
    FilterContext filterContext = mock(FilterContext.class);
    Request request = mock(Request.class);
    Response<byte[]> response = mock(Response.class);

    @Before
    public void setUp() {
        when(filterContext.getRequest()).thenReturn(request);
        when(filterContext.getResponse()).thenReturn(response);
        subject = new DefaultAccessLogFormatter();
    }

    @Test
    public void shouldFormatAccessLogEntryCorrectly() {
        //GIVEN
        String expectedLogMessage = "req_id=XYZ status=200 http_method=GET " +
            "elapsed_millis=5 " +
            "url=/my/api/path?param1=param-value-1 " +
            "http_refer=www.referrer.com http_user_agent=my-user-agent host=test-host " +
            "http_version=HTTP/1.1 api_key=-";

        commonSetup(200, Optional.of(HttpVersion.HTTP_1_1.name()), Optional.of("172.1.1.1"), Optional.of("auth-secret"), Optional.empty());
        subject.setHostname("test-host");

        //WHEN
        String formattedLogMessage = subject.format(filterContext, 5);

        //THEN
        assertThat(formattedLogMessage, equalTo(expectedLogMessage));
    }

    @Test
    public void shouldLogHttpVersionCorrectly() {
        //GIVEN
        commonSetup(200, Optional.of(HttpVersion.HTTP_1_0.name()), Optional.of("172.1.1.1"), Optional.of("auth-secret"), Optional.empty());

        //WHEN
        String formattedLogMessage = subject.format(filterContext, 5);

        //THEN
        assertThat(formattedLogMessage, containsString("http_version=HTTP/1.0"));
    }

    @Test
    public void shouldHandleMissingHttpVersion() {
        //GIVEN
        commonSetup(200, Optional.empty(), Optional.of("172.1.1.1"), Optional.empty(), Optional.empty());

        //WHEN
        String formattedLogMessage = subject.format(filterContext, 5);

        //THEN
        assertThat(formattedLogMessage, containsString("http_version= "));
    }

    @Test
    public void shouldHandleMissingHttpStatus() {
        //GIVEN
        commonSetup(0, Optional.empty(), Optional.of("172.1.1.1"), Optional.empty(), Optional.empty());
        when(filterContext.getResponse()).thenReturn(null);

        //WHEN
        String formattedLogMessage = subject.format(filterContext, 5);

        //THEN
        assertThat(formattedLogMessage, containsString("status=500"));
    }

    private void commonSetup(int httpStatusCode, Optional<String> httpVersion, Optional<String> trueClientIp, Optional<String> authenticationSecret, Optional<String> tapAuthSecret) {
        Multimap responseHeaders = LinkedListMultimap.create();
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(response.getHttpStatusCode()).thenReturn(httpStatusCode);

        when(request.getHttpVersion()).thenReturn((httpVersion.isPresent()) ? httpVersion.get() : null);

        when(request.getHeader("True-Client-IP", null)).thenReturn((trueClientIp.isPresent()) ? trueClientIp.get() : "-");
        when(request.getParam("api_key", null)).thenReturn((tapAuthSecret.isPresent()) ? tapAuthSecret.get() : null);

        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getRequestURI()).thenReturn("/my/api/path?param1=param-value-1");
        when(request.getHeader(eq("referrer"), anyString())).thenReturn("www.referrer.com");
        when(request.getHeader(eq("user-agent"), anyString())).thenReturn("my-user-agent");
        when(request.getHeader(eq("CorrelationId"), anyString())).thenReturn("XYZ");
//        when(routingContext.get(eq("rgenID"))).thenReturn("c");
    }
}
