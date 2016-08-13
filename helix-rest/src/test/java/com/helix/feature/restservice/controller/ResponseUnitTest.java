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

import org.junit.Test;

import static org.junit.Assert.*;

public class ResponseUnitTest {

    @Test
    public void testSuccessResponse() throws Exception {
        Response<String> response = Response.successResponse("Yes!");
        assertEquals(200, response.getHttpStatusCode());
        assertEquals("Yes!", response.getResponseBody());
        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    public void testInternalError() throws Exception {
        Response<String> response = Response.internalError("No!");
        assertEquals(500, response.getHttpStatusCode());
        assertEquals("No!", response.getResponseBody());
        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    public void testNotFound() throws Exception {
        Response<String> response = Response.notFound("Go Home!");
        assertEquals(404, response.getHttpStatusCode());
        assertEquals("Go Home!", response.getResponseBody());
        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    public void testInternalErrorNullBody() throws Exception {
        Response<String> response = Response.internalError();
        assertEquals(500, response.getHttpStatusCode());
        assertNull(response.getResponseBody());
        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    public void testNotFoundNullBody() throws Exception {
        Response<String> response = Response.notFound();
        assertEquals(404, response.getHttpStatusCode());
        assertNull(response.getResponseBody());
        assertTrue(response.getHeaders().isEmpty());
    }


    @Test
    public void testFromHttpStatusCode() throws Exception {
        Response<String> response = Response.fromHttpStatusCode(203, "Interesting");
        response.getHeaders().put("header", "value");
        assertEquals(203, response.getHttpStatusCode());
        assertEquals("Interesting", response.getResponseBody());
        assertEquals(1, response.getHeaders().size());
        assertEquals("value", response.getHeaders().get("header").iterator().next());
    }

    @Test
    public void testFromHttpStatusCodeNullBody() throws Exception {
        Response<String> response = Response.fromHttpStatusCode(203);
        response.getHeaders().put("header", "value");
        assertEquals(203, response.getHttpStatusCode());
        assertNull(response.getResponseBody());
        assertEquals(1, response.getHeaders().size());
        assertEquals("value", response.getHeaders().get("header").iterator().next());
    }

    @Test
    public void testMutateResponse() throws Exception {
        Response<String> response = Response.fromHttpStatusCode(203);
        response.setHttpStatusCode(100);
        response.setResponseBody("Headless");
        response.addHeader("h1", "v1");
        response.addHeader("h2", "v2");

        assertEquals(2, response.getHeaders().size());
        assertEquals("v1", response.getHeaders().get("h1").iterator().next());
        assertEquals("v2", response.getHeaders().get("h2").iterator().next());
        assertEquals("v2", response.getHeader("h2"));
        assertEquals("Headless", response.getResponseBody());
        assertEquals(100, response.getHttpStatusCode());

        response.removeHeader("foo");
        assertEquals(2, response.getHeaders().size());
        response.removeHeader("h2");
        assertEquals(1, response.getHeaders().size());
        assertEquals(null, response.getHeader("h2"));
    }
}
