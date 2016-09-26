
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

package io.helixservice.feature.health;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.helixservice.feature.configuration.locator.ClasspathResourceLocator;
import io.helixservice.feature.configuration.locator.ResourceLocator;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Clock;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HealthControllerUnitTest {

    private static final String VERSION_FILE = "version.json";

    private HealthController subject;
    private OfflineProcessor<Multimap<String, String>> offlineProcessor = mock(OfflineProcessor.class);
    private ResourceLocator resourceLocator = mock(ResourceLocator.class);
    private Request<String> request = mock(Request.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SuspendExecution {
        when(resourceLocator.getJsonObject(anyString())).thenReturn(Optional.empty());
        subject = new HealthController(offlineProcessor, resourceLocator, Clock.systemDefaultZone());
        Status.INSTANCE.setOnLine();
    }

    @Test
    public void shouldMemoizeTheResult() throws SuspendExecution {
        subject.heartbeat(request);
        subject.heartbeat(request);

        verify(resourceLocator, times(1)).getJsonObject(VERSION_FILE);
    }

    @Test
    public void shouldCallOfflineProcessorWithParametersWhenProcessingRequest() {
        //GIVEN
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("my-param", "my-param-value");
        when(request.getParams()).thenReturn(parameters);
        when(request.getMethod()).thenReturn("GET");

        //WHEN
        subject.heartbeat(request);

        //THEN
        verify(offlineProcessor, times(1)).processInstruction(parameters);
    }



    @Test
    public void shouldReturnVersionInformationInResponseBody() throws SuspendExecution {

        when(resourceLocator.getJsonObject(anyString())).thenReturn(ClasspathResourceLocator.INSTANCE.getJsonObject(VERSION_FILE));
        subject = new HealthController(offlineProcessor, resourceLocator, Clock.systemDefaultZone());
        Status.INSTANCE.setOnLine();

        //WHEN
        Response<String> response = subject.heartbeat(request);

        //THEN
        assertEquals(200, response.getHttpStatusCode());
        System.out.println("response.getResponseBody() = " + response.getResponseBody());
        assertTrue(response.getResponseBody().startsWith(
                "{\"gitCommitId\":\"commitId\",\"gitRemoteOriginUrl\":\"originUrl\",\"appVersion\":\"version\",\"upSince\""));
    }

    @Test
    public void shouldThrowExceptionIfResourceLocatoarIsNull() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage(containsString("resourceLocator cannot be null"));
        new HealthController(offlineProcessor, null, Clock.systemDefaultZone());
    }

    @Test
    public void shouldReturnOnlineHealthCheckStatusCodeIfServerOnline() {
        //WHEN
        Response<String> response = subject.healthCheck(request);

        //THEN
        assertEquals(200, response.getHttpStatusCode());
        assertNull(response.getResponseBody());
    }

    @Test
    public void shouldReturnOnlineHeartBeatStatusCodeIfServerOnline() {
        //WHEN
        Response<String> response = subject.heartbeatStatus(request);

        //THEN
        assertEquals(200, response.getHttpStatusCode());
        assertNull(response.getResponseBody());
    }

    @Test
    public void shouldReturnOfflineStatusCodeIfServerOffline() {
        //GIVEN
        Status.INSTANCE.setOffLine();

        //WHEN
        Response<String> response = subject.healthCheck(request);

        //THEN
        assertEquals(Status.FORCED_DOWN, response.getHttpStatusCode());
        assertNull(response.getResponseBody());
    }
}
