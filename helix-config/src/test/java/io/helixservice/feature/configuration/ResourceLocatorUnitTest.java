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

package io.helixservice.feature.configuration;

import co.paralleluniverse.fibers.SuspendExecution;
import io.helixservice.feature.configuration.locator.ClasspathResourceLocator;
import io.vertx.core.json.JsonObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class ResourceLocatorUnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfJsonFileNotFoundOnClasspath() throws SuspendExecution {
        Optional<JsonObject> jsonObject = ClasspathResourceLocator.INSTANCE.getJsonObject("path/to/nowhere.json");
        assertFalse(jsonObject.isPresent());
    }

    @Test
    public void shouldProcessValidJsonFileOnClasspath() throws SuspendExecution {
        //GIVEN - test.json on test classpath

        //WHEN
        Optional<JsonObject> json = ClasspathResourceLocator.INSTANCE.getJsonObject("test.json");

        //THEN
        assertThat(json.get().getString("key1"), equalTo("value1"));
        assertThat(json.get().getString("key2"), equalTo("value2"));
    }

    @Test
    public void shouldFindInputStreamFromClasspath() throws Exception {
        // GIVEN
        // member-keystore.jceks on test classpath

        // WHEN
        Optional<InputStream> inputStream = ClasspathResourceLocator.INSTANCE.getStream("member-keystore.jceks");

        // THEN
        assertEquals(true, inputStream.isPresent());
    }

    @Test
    public void shouldNotFindInputStreamFromClasspathIfDoesNotExist() throws Exception {
        // GIVEN
        // gibberish.log does NOT exist on test classpath

        // WHEN
        Optional<InputStream> inputStream = ClasspathResourceLocator.INSTANCE.getStream("gibberish.log");

        // THEN
        assertEquals(false, inputStream.isPresent());


    }
}
