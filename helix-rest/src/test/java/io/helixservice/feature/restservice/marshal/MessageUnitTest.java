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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MessageUnitTest {

    @Test
    public void testMessage() {
        byte[] body = "123".getBytes();
        Message message = new Message(body, "soap1.0");
        assertEquals("123", new String(message.getBody()));
        assertEquals("soap1.0", message.getContentType());
        assertEquals(1, message.getContentTypes().size());
        assertEquals("soap1.0", message.getContentTypes().get(0));
    }

    @Test
    public void testMessageConstructor2() {
        byte[] body = "123".getBytes();
        List<String> contentTypes = new ArrayList<>();
        Message message = new Message(body, contentTypes);
        assertEquals("123", new String(message.getBody()));
        assertEquals("", message.getContentType());
        assertEquals(0, message.getContentTypes().size());
    }

    @Test
    public void testMessageConstructor2WithValues() {
        byte[] body = "123".getBytes();
        List<String> contentTypes = new ArrayList<>();
        contentTypes.add("soap1.0");
        contentTypes.add("xmlrpc");

        Message message = new Message(body, contentTypes);
        assertEquals("123", new String(message.getBody()));
        assertEquals("soap1.0", message.getContentType());
        assertEquals(2, message.getContentTypes().size());
    }


}
