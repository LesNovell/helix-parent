
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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class QueryParameterOfflineProcessorUnitTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    final String forcedDownPassword = "passw0rd";

    QueryParameterOfflineProcessor subject;

    @Before
    public void setUp() {
        Status.INSTANCE.setOnLine();
        subject = new QueryParameterOfflineProcessor(forcedDownPassword);
    }

    @Test
    public void shouldThrowExceptionIfForcedDownPasswordNull() {
        //GIVEN
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("Forced down password cannot be null"));

        //WHEN
        new QueryParameterOfflineProcessor(null);
    }

    @Test
    public void shouldThrowExceptionIfForcedDownPasswordHasLessThanThreeCharacters() {
        //GIVEN
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("forced down password must contain at least 3 characters"));

        //WHEN
        new QueryParameterOfflineProcessor("ab");
    }

    @Test
    public void shouldPermitForcedDownPasswordOfThreeCharactersOrMore() {
        //GIVEN

        //WHEN
        QueryParameterOfflineProcessor instance = new QueryParameterOfflineProcessor("abc");

        //THEN
        assertNotNull(instance);
    }

    @Test
    public void shouldProcessHeartbeatRequestWithNoParametersWhenOnline() {
        //GIVEN
        Multimap parameters = LinkedListMultimap.create();

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(true));
    }

    @Test
    public void shouldProcessHeartbeatRequestWithNoParametersWhenOffline() {
        //GIVEN
        Status.INSTANCE.setOffLine();
        Multimap parameters = LinkedListMultimap.create();

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(false));
    }


    @Test
    public void shouldIgnoreUnrecognisedQueryParameters() {
        //GIVEN
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("unrecognised-param", "unrecognised-param-value");

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(true));
    }

    @Test
    public void shouldIgnoreUnrecognisedOfflineParameterValue() {
        //GIVEN
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("offline", "unrecognised-param-value");
        parameters.put("password", forcedDownPassword);

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(true));
    }

    @Test
    public void shouldPutServerInOfflineModeIfRequestIsMadeWithCorrectPassword() {
        //GIVEN
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("offline", "true");
        parameters.put("password", forcedDownPassword);

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(false));
    }

    @Test
    public void shouldPutServerInOnlineModeIfRequestIsMadeWithCorrectPassword() {
        //GIVEN
        Status.INSTANCE.setOffLine();
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("offline", "false");
        parameters.put("password", forcedDownPassword);

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(true));
    }

    @Test
    public void shouldNotChangeServerStatusIfOfflineRequestIsMadeWithIncorrectPasswordWhenServerIsOnline() {
        //GIVEN
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("offline", "true");
        parameters.put("password", "incorrect");

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(true));
    }

    @Test
    public void shouldNotChangeServerStatusIfOfflineRequestIsMadeWithIncorrectPasswordWhenServerIsOffline() {
        //GIVEN
        Status.INSTANCE.setOffLine();
        Multimap parameters = LinkedListMultimap.create();
        parameters.put("offline", "false");
        parameters.put("password", "incorrect");

        //WHEN
        subject.processInstruction(parameters);

        //THEN
        assertThat(Status.INSTANCE.isOnline(), equalTo(false));
    }
}
