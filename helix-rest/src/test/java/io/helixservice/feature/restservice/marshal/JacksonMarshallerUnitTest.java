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

package io.helixservice.feature.restservice.marshal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JacksonMarshallerUnitTest {

    @Test
    public void testNullResponseData() {
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        JacksonMarshaller subject = new JacksonMarshaller(objectMapper);
        Message message = subject.marshal(null);

        assertEquals("{}", new String(message.getBody()));
        assertEquals("application/json", message.getContentType());
    }

    @Test
    public void testResponseData() throws JsonProcessingException {
        Object marshaledObject = new Object();

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(marshaledObject)).thenReturn("{ json body }");

        JacksonMarshaller subject = new JacksonMarshaller(objectMapper);
        Message message = subject.marshal(marshaledObject);

        assertEquals("{ json body }", new String(message.getBody()));
        assertEquals("application/json", message.getContentType());
    }

    @Test(expected = MarshallerException.class)
    public void testMarshallException() throws JsonProcessingException {
        Object marshaledObject = new Object();

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(marshaledObject)).thenThrow(new NullPointerException("nope!"));

        JacksonMarshaller subject = new JacksonMarshaller(objectMapper);
        subject.marshal(marshaledObject);
    }

    @Test(expected = MarshallerException.class)
    public void testUnmarshallException() throws IOException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(any(byte[].class), eq(Object.class))).thenThrow(new NullPointerException("nope!"));

        JacksonMarshaller subject = new JacksonMarshaller(objectMapper);
        subject.unmarshal(Object.class, new Message("some data".getBytes(), "application/json"));
    }

    @Test
    public void testUnmarshalBodyToStringSimpleMapping() throws IOException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        JacksonMarshaller subject = new JacksonMarshaller(objectMapper);
        Object string = subject.unmarshal(String.class, new Message("some data".getBytes(), "application/json"));
        assertEquals(string, "some data");
    }

    @Test
    public void testUnmarshalObjectMapping() throws IOException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(any(byte[].class), eq(Object.class))).thenReturn("YES!");

        JacksonMarshaller subject = new JacksonMarshaller(objectMapper);
        Object string = subject.unmarshal(Object.class, new Message("some data".getBytes(), "application/json"));
        assertEquals(string, "YES!");
    }
}
