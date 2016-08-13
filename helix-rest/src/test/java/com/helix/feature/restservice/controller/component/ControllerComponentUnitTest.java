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

package com.helix.feature.restservice.controller.component;

import com.helix.feature.restservice.controller.HttpMethod;
import com.helix.feature.restservice.controller.Request;
import com.helix.feature.restservice.controller.Response;
import com.helix.feature.restservice.controller.annotation.Controller;
import com.helix.feature.restservice.controller.annotation.Endpoint;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ControllerComponentUnitTest {

    @Test
    public void testSimpleConstruction() throws Exception {
        TestController testController = new TestController();
        List<EndpointComponent> endpointComponents = new ArrayList<>();
        Method endpointMethod = TestController.class.getMethod("endpointMethod", Request.class);
        EndpointComponent endpointComponent = EndpointComponent.forPath("path", new HttpMethod[] {HttpMethod.GET}, endpointMethod, testController);
        endpointComponents.add(endpointComponent);

        ControllerComponent subject = new ControllerComponent(testController, endpointComponents);

        assertEquals(testController, subject.getController());
        assertEquals(1, subject.getEndpointComponentList().size());
        assertEquals("endpointMethod", endpointComponent.getEndpointMethod().getName());
        assertEquals(1, endpointComponent.getHttpMethods().length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoControllerAnnotation() throws Exception {
        ControllerComponent.fromAnnotationsOn(new TestController());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnnotation() throws Exception {
        ControllerComponent.fromAnnotationsOn(new TestController());
    }

    @Test
    @Ignore
    public void testFromAnnotations() throws Exception {
        TestAnnotatedController testController = new TestAnnotatedController();
        ControllerComponent subject = ControllerComponent.fromAnnotationsOn(testController);

        assertEquals(testController, subject.getController());
        assertEquals(2, subject.getEndpointComponentList().size());

        EndpointComponent endpointComponent1 = subject.getEndpointComponentList().get(0);
        assertEquals("endpointMethod", endpointComponent1.getEndpointMethod().getName());
        assertEquals("path", endpointComponent1.getPath());
        assertEquals(1, endpointComponent1.getHttpMethods().length);

        EndpointComponent endpointComponent2 = subject.getEndpointComponentList().get(1);
        assertEquals("endpointMethod2", endpointComponent2.getEndpointMethod().getName());
        assertEquals("path2", endpointComponent2.getPath());
        assertEquals(1, endpointComponent2.getHttpMethods().length);
        assertEquals(HttpMethod.POST, endpointComponent2.getHttpMethods()[0]);

        String toString = subject.toString();
        assertTrue(toString.contains("'path' -> TestAnnotatedController.endpointMethod() [GET]"));
        assertTrue(toString.contains("'path2' -> TestAnnotatedController.endpointMethod2() [POST]"));

    }

    public static class TestController {
        public Response endpointMethod(Request request) {
            return null;
        }
    }

    @Controller
    public static class TestAnnotatedController {
        @Endpoint(value = "path", methods = HttpMethod.GET)
        public Response endpointMethod(Request request) {
            return null;
        }

        @Endpoint(value = "path2", methods = HttpMethod.POST)
        public Response endpointMethod2(Request request) {
            return null;
        }
    }
}
