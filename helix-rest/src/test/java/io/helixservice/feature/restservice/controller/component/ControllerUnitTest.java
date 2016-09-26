
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

package io.helixservice.feature.restservice.controller.component;

import io.helixservice.feature.restservice.controller.HttpMethod;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ControllerUnitTest {

    @Test
    public void testSimpleConstruction() throws Exception {
        TestController testController = new TestController();
        List<Endpoint> endpoints = new ArrayList<>();
        Method endpointMethod = TestController.class.getMethod("endpointMethod", Request.class);
        Endpoint endpoint = Endpoint.forPath("path", new HttpMethod[] {HttpMethod.GET}, endpointMethod, testController);
        endpoints.add(endpoint);

        Controller subject = new Controller(testController, endpoints);

        assertEquals(testController, subject.getController());
        assertEquals(1, subject.getEndpointList().size());
        assertEquals("endpointMethod", endpoint.getEndpointMethod().getName());
        assertEquals(1, endpoint.getHttpMethods().length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoControllerAnnotation() throws Exception {
        Controller.fromAnnotationsOn(new TestController());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnnotation() throws Exception {
        Controller.fromAnnotationsOn(new TestController());
    }

    @Test
    @Ignore
    public void testFromAnnotations() throws Exception {
        TestAnnotatedController testController = new TestAnnotatedController();
        Controller subject = Controller.fromAnnotationsOn(testController);

        assertEquals(testController, subject.getController());
        assertEquals(2, subject.getEndpointList().size());

        Endpoint endpoint1 = subject.getEndpointList().get(0);
        assertEquals("endpointMethod", endpoint1.getEndpointMethod().getName());
        assertEquals("path", endpoint1.getPath());
        assertEquals(1, endpoint1.getHttpMethods().length);

        Endpoint endpoint2 = subject.getEndpointList().get(1);
        assertEquals("endpointMethod2", endpoint2.getEndpointMethod().getName());
        assertEquals("path2", endpoint2.getPath());
        assertEquals(1, endpoint2.getHttpMethods().length);
        assertEquals(HttpMethod.POST, endpoint2.getHttpMethods()[0]);

        String toString = subject.toString();
        assertTrue(toString.contains("'path' -> TestAnnotatedController.endpointMethod() [GET]"));
        assertTrue(toString.contains("'path2' -> TestAnnotatedController.endpointMethod2() [POST]"));

    }

    public static class TestController {
        public Response endpointMethod(Request request) {
            return null;
        }
    }

    @io.helixservice.feature.restservice.controller.annotation.Controller
    public static class TestAnnotatedController {
        @io.helixservice.feature.restservice.controller.annotation.Endpoint(value = "path", methods = HttpMethod.GET)
        public Response endpointMethod(Request request) {
            return null;
        }

        @io.helixservice.feature.restservice.controller.annotation.Endpoint(value = "path2", methods = HttpMethod.POST)
        public Response endpointMethod2(Request request) {
            return null;
        }
    }
}
