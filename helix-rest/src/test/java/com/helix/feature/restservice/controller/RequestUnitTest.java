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

package com.helix.feature.restservice.controller;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.helix.feature.restservice.controller.Request;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RequestUnitTest {
    @Test
    public void testRequestObject() {
        Multimap<String,String> params = LinkedListMultimap.create();
        Multimap<String,String> headers = LinkedListMultimap.create();

        params.put("Foo", "Bar");
        headers.put("Head", "Out");

        Request<String> request = new Request<>("method", "uri", params, headers, "body", "ipRemoteAddress", "http 1.0");

        assertEquals("body", request.getBody());
        assertEquals("uri", request.getRequestURI());
        assertEquals("method", request.getMethod());
        assertEquals("ipRemoteAddress", request.getRemoteAddr());
        assertEquals("http 1.0", request.getHttpVersion());

        assertSame(request.getHeader("Head", null), "Out");
        assertSame(request.getHeader("NotExisting", null), null);
        assertSame(request.getParam("Foo", null), "Bar");
        assertSame(request.getParams().size(), 1);
        assertSame(request.getHeaders().size(), 1);
    }

    @Test
    public void testRequestObjectSetters() {
        Multimap<String,String> params = LinkedListMultimap.create();
        Multimap<String,String> headers = LinkedListMultimap.create();

        params.put("Foo", "Bar");
        headers.put("Head", "Out");

        Request<String> request = new Request<>("method", "uri", params, headers, "body", "ipRemoteAddress", "http 1.0");

        request.setHttpVersion("httpVersion");
        request.addHeader("header1", "hvalue");
        request.addParam("param1", "pvalue");

        assertEquals("httpVersion", request.getHttpVersion());
        assertEquals("hvalue", request.getHeader("header1", null));
        assertEquals("pvalue", request.getParam("param1", null));
        assertSame(request.getHeader("Head", null), "Out");
        assertSame(request.getHeader("NotExisting", null), null);
        assertSame(request.getParam("Foo", null), "Bar");
        assertSame(request.getParams().size(), 2);
        assertSame(request.getHeaders().size(), 2);
    }

}
