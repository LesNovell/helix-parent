
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

import static org.junit.Assert.assertEquals;

public class MarshallerExceptionUnitTest {

    @Test
    public void testExceptionConstruction() {
        IllegalArgumentException cause = new IllegalArgumentException();
        String msg = "unknown error";
        MarshallerException marshallerException = new MarshallerException(msg, cause);
        assertEquals(msg, marshallerException.getMessage());
        assertEquals(cause, marshallerException.getCause());
    }
}
