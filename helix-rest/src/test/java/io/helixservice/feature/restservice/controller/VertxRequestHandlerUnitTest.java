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

package io.helixservice.feature.restservice.controller;

import org.junit.Test;

public class VertxRequestHandlerUnitTest {

    @Test
    public void emptyTest() {

    }
//
//    private static boolean reachedHandle = false;
//    private static Object requestBody = null;
//
//    private StringEndpointTester stringEndpointTester;
//    private Method handleMethod;
//    private Marshaller marshaller;
//    private ErrorHandlerRegistry errorHandlerRegistry;
//    private RoutingContext routingContext;
//    private HttpServerResponse response;
//    private HttpServerRequest request;
//    private EndpointHandler subject;
//
//    @Before
//    public void setup() throws NoSuchMethodException {
//        stringEndpointTester = new StringEndpointTester();
//        handleMethod = StringEndpointTester.class.getMethod("handle", Request.class);
//        marshaller = mock(Marshaller.class);
//        errorHandlerRegistry = new ErrorHandlerRegistry();
//        routingContext = mock(RoutingContext.class);
//        response = mock(HttpServerResponse.class);
//        request = mock(HttpServerRequest.class);
//
//        subject = new EndpointHandler(stringEndpointTester, handleMethod, marshaller, errorHandlerRegistry);
//
//        when(routingContext.request()).thenReturn(request);
//        when(request.method()).thenReturn(HttpMethod.GET);
//        when(request.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
//        when(request.params()).thenReturn(MultiMap.caseInsensitiveMultiMap());
//        when(request.remoteAddress()).thenReturn(new SocketAddressImpl(23123, "12.12.12.12"));
//        when(request.version()).thenReturn(HttpVersion.HTTP_1_1);
//
//        when(routingContext.response()).thenReturn(response);
//        when(response.setStatusCode(anyInt())).thenReturn(response);
//        when(response.setChunked(true)).thenReturn(response);
//        when(response.write(Matchers.any(Buffer.class))).thenReturn(response);
//        when(response.putHeader("content-type", "application/json")).thenReturn(response);
//        when(response.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
//    }
//
//
//    @Test
//    public void testHandle() throws Exception {
//        when(marshaller.unmarshal(Matchers.any(), Matchers.any())).thenReturn("{ }");
//        when(marshaller.marshal("Body")).thenReturn(new Message("json output".getBytes(), new ArrayList<String>() {{ add("application/json"); }} ));
//        when(routingContext.getBody()).thenReturn(Buffer.buffer("{ }"));
//        when(routingContext.response().putHeader(anyString(), (Iterable<String>)anyObject())).thenReturn(response);
//        when(routingContext.response().write(any(Buffer.class))).thenReturn(response);
//
//        subject.handle(routingContext);
//
//        assertTrue(reachedHandle);
//        assertEquals("{ }", requestBody);
//        verify(response, times(1)).write(Matchers.any(Buffer.class));
//        verify(response, times(1)).setStatusCode(200);
//    }
//
//    @Test
//    public void testJsonObjectRequestBodyWithRealJacksonMapper() throws Exception {
//        JsonObjectEndpointTester endpointTester = new JsonObjectEndpointTester();
//        Method handleMethod = JsonObjectEndpointTester.class.getMethod("handle", Request.class);
//        ObjectMapper objectMapper = new ObjectMapper();
//        subject = new EndpointHandler(endpointTester, handleMethod, new JacksonMarshaller(objectMapper), errorHandlerRegistry);
//
//        when(routingContext.getBody()).thenReturn(Buffer.buffer("{ \"station\" : \"105.3\", \"volume\" : \"11\" }"));
//        when(routingContext.response().putHeader(anyString(), (Iterable<String>)anyObject())).thenReturn(response);
//        when(routingContext.response().write(any(Buffer.class))).thenReturn(response);
//
//        subject.handle(routingContext);
//
//        assertTrue(reachedHandle);
//        assertEquals(Radio.class, requestBody.getClass());
//        assertEquals("11", ((Radio)requestBody).getVolume());
//    }
//
//    @Test
//    public void testHandleNullResponseBody() throws Exception {
//        NullEndpointTester endpointTester = new NullEndpointTester();
//        Method handleMethod = NullEndpointTester.class.getMethod("handle", Request.class);
//        ObjectMapper objectMapper = mock(ObjectMapper.class);
//        RoutingContext routingContext = mock(RoutingContext.class);
//
//        EndpointHandler subject = new EndpointHandler(endpointTester, handleMethod, new JacksonMarshaller(objectMapper), errorHandlerRegistry);
//
//        when(routingContext.response()).thenReturn(response);
//        when(routingContext.request()).thenReturn(request);
//        when(routingContext.response().putHeader(anyString(), (Iterable<String>)anyObject())).thenReturn(response);
//        when(routingContext.response().write(any(Buffer.class))).thenReturn(response);
//
//        subject.handle(routingContext);
//
//        assertTrue(reachedHandle);
//        verify(response, times(1)).write(Matchers.any(Buffer.class));  // TODO: Test this is "{}" inside buffer!
//        verify(response, times(1)).setStatusCode(200);
//    }
//
//
//    @Test
//    public void testHandleThrowsException() throws Exception {
//        ThrowingEndpointTester endpointTester = new ThrowingEndpointTester();
//        Method handleMethod = ThrowingEndpointTester.class.getMethod("handle", Request.class);
//        ObjectMapper objectMapper = mock(ObjectMapper.class);
//        RoutingContext routingContext = mock(RoutingContext.class);
//
//        EndpointHandler subject = new EndpointHandler(endpointTester, handleMethod, new JacksonMarshaller(objectMapper), errorHandlerRegistry);
//
//        when(marshaller.unmarshal(Matchers.any(), Matchers.any())).thenReturn("{ }");
//        when(marshaller.marshal("Body")).thenReturn(new Message("json output".getBytes(), new ArrayList<String>() {{
//            add("application/json");
//        }}));
//        when(routingContext.response()).thenReturn(response);
//        when(routingContext.request()).thenReturn(request);
//        when(routingContext.getBody()).thenReturn(Buffer.buffer("{ }"));
//        when(routingContext.response().putHeader(anyString(), (Iterable<String>) anyObject())).thenReturn(response);
//        when(routingContext.response().write(any(Buffer.class))).thenReturn(response);
//
//        subject.handle(routingContext);
//
//        assertTrue(reachedHandle);
//        verify(response, times(1)).setStatusCode(500);
//        verify(response, times(1)).write(Matchers.any(Buffer.class));
//    }
//
//
//    public static class StringEndpointTester {
//
//        public Response<String> handle(Request<String> request) {
//            reachedHandle = true;
//            requestBody = request.getBody();
//            assertNotNull("Body should not be null", request.getBody());
//
//            return Response.successResponse("Body");
//        }
//    }
//
//    public static class JsonObjectEndpointTester {
//        public Response<String> handle(Request<Radio> request) {
//            reachedHandle = true;
//            requestBody = request.getBody();
//            assertNotNull(request.getBody());
//
//            return Response.successResponse("Body");
//        }
//    }
//
//
//    public static class NullEndpointTester {
//        public Response<String> handle(Request request) {
//            reachedHandle = true;
//
//            assertNull(request.getBody());
//
//            return Response.successResponse(null);
//        }
//    }
//
//    public static class ThrowingEndpointTester {
//        public Response<String> handle(Request request) {
//            reachedHandle = true;
//            assertNull(request.getBody());
//            throw new NullPointerException("oops!");
//        }
//    }
//
//
//    public static class Radio {
//        private float station;
//        private String volume;
//
//        public float getStation() {
//            return station;
//        }
//
//        public void setStation(float station) {
//            this.station = station;
//        }
//
//        public String getVolume() {
//            return volume;
//        }
//
//        public void setVolume(String volume) {
//            this.volume = volume;
//        }
//    }
}
