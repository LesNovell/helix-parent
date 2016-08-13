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

import co.paralleluniverse.fibers.SuspendExecution;
import com.helix.feature.restservice.FakeHttpServerRequest;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.RoutingContext;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilterHandlerUnitTest {
    FilterContext filterContextBefore;
    FilterContext filterContextAfter;

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testSimpleBeforeAfterFilter() {
        FilterHandler subject = new FilterHandler(new Filter() {
            @Override
            public void beforeHandleEndpoint(FilterContext filterContext) {
                filterContextBefore = filterContext;
            }

            @Override
            public void afterHandleEndpoint(FilterContext filterContext) {
                filterContextAfter = filterContext;
            }

            @Override
            public void afterResponseSent(FilterContext filterContext) throws SuspendExecution {
            }
        });

        RoutingContext routingContext = mock(RoutingContext.class);

        MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap();
        responseHeaders.add("response-header", "output");
        MultiMap requestHeaders = MultiMap.caseInsensitiveMultiMap();
        requestHeaders.add("request-header", "input");
        MultiMap requestParams = MultiMap.caseInsensitiveMultiMap();
        requestParams.add("request-param", "param");

        setup(routingContext,
                220, responseHeaders,                         // Response HttpStatus, Http Headers
                requestHeaders,                               // Request Http Headers
                requestParams,                                // Request Http Params
                9090, "my.host.com", HttpVersion.HTTP_1_1,    // Request Sender Info
                "Request Body");                              // Request Body

        //  Call "before" filter method
        assertNull(filterContextBefore);
        assertNull(filterContextAfter);

        subject.handle(routingContext);

        assertNotNull(filterContextBefore);
        assertNull(filterContextAfter);


        // Call "after" filter method
        ArgumentCaptor<Handler> afterBodyHandler = ArgumentCaptor.forClass(Handler.class);
        verify(routingContext, times(1)).addHeadersEndHandler(afterBodyHandler.capture());
        assertEquals(1, afterBodyHandler.getAllValues().size());
        afterBodyHandler.getValue().handle(null);

        assertNotNull(filterContextBefore);
        assertNotNull(filterContextAfter);

        // Check desired after values here
        assertEquals(220, filterContextBefore.getResponse().getHttpStatusCode());
        assertEquals("output", filterContextBefore.getResponse().getHeader("response-header"));
        assertEquals("input", filterContextBefore.getRequest().getHeader("request-header", null));
        assertEquals("param", filterContextBefore.getRequest().getParam("request-param", null));
        assertEquals("Request Body", new String((byte[]) filterContextBefore.getRequest().getBody()));

        assertEquals(220, filterContextAfter.getResponse().getHttpStatusCode());
        assertEquals("output", filterContextAfter.getResponse().getHeader("response-header"));
        assertEquals("input", filterContextAfter.getRequest().getHeader("request-header", null));
        assertEquals("param", filterContextAfter.getRequest().getParam("request-param", null));
        assertEquals("Request Body", new String((byte[])filterContextAfter.getRequest().getBody()));
        assertNull(filterContextAfter.getResponse().getResponseBody());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testAfterFilterAddsHeader() {
        FilterHandler subject = new FilterHandler(new Filter() {
            @Override
            public void beforeHandleEndpoint(FilterContext filterContext) {
                filterContextBefore = filterContext;
            }

            @Override
            public void afterHandleEndpoint(FilterContext filterContext) {
                filterContext.getResponse().addHeader("new-header", "value");
                filterContextAfter = filterContext;
            }

            @Override
            public void afterResponseSent(FilterContext filterContext) throws SuspendExecution {

            }
        });

        RoutingContext routingContext = mock(RoutingContext.class);

        MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap();
        responseHeaders.add("response-header", "output");
        MultiMap requestHeaders = MultiMap.caseInsensitiveMultiMap();
        requestHeaders.add("request-header", "input");
        MultiMap requestParams = MultiMap.caseInsensitiveMultiMap();
        requestParams.add("request-param", "param");

        setup(routingContext,
                220, responseHeaders,                         // Response HttpStatus, Http Headers
                requestHeaders,                               // Request Http Headers
                requestParams,                                // Request Http Params
                9090, "my.host.com", HttpVersion.HTTP_1_1,    // Request Sender Info
                "Request Body");                              // Request Body

        //  Call "before" filter method
        assertNull(filterContextBefore);
        assertNull(filterContextAfter);

        subject.handle(routingContext);

        assertNotNull(filterContextBefore);
        assertNull(filterContextAfter);


        // Call "after" filter method
        ArgumentCaptor<Handler> afterBodyHandler = ArgumentCaptor.forClass(Handler.class);
        verify(routingContext, times(1)).addHeadersEndHandler(afterBodyHandler.capture());
        assertEquals(1, afterBodyHandler.getAllValues().size());
        afterBodyHandler.getValue().handle(null);

        assertNotNull(filterContextBefore);
        assertNotNull(filterContextAfter);

        // Check desired after values here
        assertEquals("value", responseHeaders.get("new-header"));
        assertEquals("output", responseHeaders.get("response-header"));  // original response header intact
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBeforeWritesHeaderAndResponse() {
        FilterHandler subject = new FilterHandler(new Filter() {
            @Override
            public void beforeHandleEndpoint(FilterContext filterContext) {
                filterContextBefore = filterContext;
                filterContext.getResponse().addHeader("new-header", "value");
                filterContext.endWithResponse(400, "Nobody Home");
            }

            @Override
            public void afterHandleEndpoint(FilterContext filterContext) {
                filterContextAfter = filterContext;
            }

            @Override
            public void afterResponseSent(FilterContext filterContext) throws SuspendExecution {

            }
        });

        RoutingContext routingContext = mock(RoutingContext.class);

        MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap();
        responseHeaders.add("response-header", "output");
        MultiMap requestHeaders = MultiMap.caseInsensitiveMultiMap();
        requestHeaders.add("request-header", "input");
        MultiMap requestParams = MultiMap.caseInsensitiveMultiMap();
        requestParams.add("request-param", "param");

        setup(routingContext,
                220, responseHeaders,                         // Response HttpStatus, Http Headers
                requestHeaders,                               // Request Http Headers
                requestParams,                                // Request Http Params
                9090, "my.host.com", HttpVersion.HTTP_1_1,    // Request Sender Info
                "Request Body");                              // Request Body

        //  Call "before" filter method
        assertNull(filterContextBefore);
        assertNull(filterContextAfter);

        subject.handle(routingContext);

        assertNotNull(filterContextBefore);
        assertNull(filterContextAfter);

        // Verify the "after" filter method should never be called
        ArgumentCaptor<Handler> afterBodyHandler = ArgumentCaptor.forClass(Handler.class);
        verify(routingContext, times(0)).addHeadersEndHandler(afterBodyHandler.capture());
        assertEquals(0, afterBodyHandler.getAllValues().size());

        assertNotNull(filterContextBefore);
        assertNull(filterContextAfter);

        // Check desired after values here
        ArgumentCaptor<Buffer> bufferCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(routingContext.response(), times(1)).setStatusCode(400);
        verify(routingContext.response(), times(1)).setChunked(true);
        verify(routingContext.response(), times(1)).write(bufferCaptor.capture());

        assertEquals("Nobody Home", bufferCaptor.getValue().toString("UTF-8"));
        assertEquals("value", responseHeaders.get("new-header"));
        assertEquals("output", responseHeaders.get("response-header"));  // original response header intact
    }

    private void setup(RoutingContext routingContext, int responseStatusCode, MultiMap responseHeaders, MultiMap requestHeaders,
            MultiMap requestParams, int requestRemotePort, String requestRemoteAddress, HttpVersion requestHttpVersion, String requestBody) {
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(response.getStatusCode()).thenReturn(responseStatusCode);
        when(response.headers()).thenReturn(responseHeaders);

        FakeHttpServerRequest fakeHttpServerRequest = new FakeHttpServerRequest();
        fakeHttpServerRequest.setHeaders(requestHeaders);
        fakeHttpServerRequest.setParams(requestParams);
        fakeHttpServerRequest.setRemoteAddress(new SocketAddressImpl(requestRemotePort, requestRemoteAddress));
        fakeHttpServerRequest.setVersion(requestHttpVersion);

        when(routingContext.request()).thenReturn(fakeHttpServerRequest);
        when(routingContext.response()).thenReturn(response);
        when(routingContext.getBody()).thenReturn(Buffer.buffer(requestBody));
        when(routingContext.response().write(any(Buffer.class))).thenReturn(response);
        when(routingContext.response().setStatusCode(anyInt())).thenReturn(response);
        when(routingContext.response().setChunked(anyBoolean())).thenReturn(response);

    }

}
