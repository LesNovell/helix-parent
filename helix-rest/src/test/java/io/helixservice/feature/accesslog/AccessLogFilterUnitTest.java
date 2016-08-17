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

package io.helixservice.feature.accesslog;

import co.paralleluniverse.fibers.SuspendExecution;
import io.helixservice.feature.restservice.controller.Response;
import io.helixservice.feature.restservice.filter.FilterContext;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class AccessLogFilterUnitTest {
    FilterContext filterContext = mock(FilterContext.class);
    AccessLogFormatter accessLogFormatter = mock(AccessLogFormatter.class);
    Logger accessLogger = mock(Logger.class);
    AccessLogFilter subject = new AccessLogFilter(accessLogger, accessLogFormatter);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
    }

    @Test
    public void shouldSetTimestampInBeforeMethod() throws SuspendExecution {
        //WHEN
        subject.beforeHandleEndpoint(filterContext);

        verify(filterContext, times(1)).setFilterVariable(eq("startTimestamp"), anyLong());
    }

    @Test
    public void shouldThrowExceptionIfRequestStartTimeInvalid() throws SuspendExecution {
        //GIVEN
        expectedException.expect(IllegalArgumentException.class);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(0L);

        //WHEN
        subject.afterHandleEndpoint(filterContext);
    }

    @Test
    public void shouldThrowExceptionIfRequestStartTimeIsNull() throws SuspendExecution {
        //GIVEN
        expectedException.expect(IllegalArgumentException.class);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(null);

        //WHEN
        subject.afterHandleEndpoint(filterContext);
    }


    @Test
    public void shouldCallAccessLogFormatterWithCalculatedTimeElapsed() throws SuspendExecution {
        //GIVEN
        Response<byte[]> response = mock(Response.class);
        when(filterContext.getResponse()).thenReturn(response);
        when(response.getHttpStatusCode()).thenReturn(99);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(System.currentTimeMillis() - 1000);

        //WHEN
        subject.afterHandleEndpoint(filterContext);

        //THEN
        verify(accessLogFormatter, times(1)).format(same(filterContext), longThat(Matchers.greaterThan(999L)));
    }

    @Test
    public void shouldLogAtErrorLevelWhenResponseStatusCodeIs5xx() throws SuspendExecution {
        //GIVEN
        Response<byte[]> response = mock(Response.class);
        when(filterContext.getResponse()).thenReturn(response);
        when(response.getHttpStatusCode()).thenReturn(504);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(System.currentTimeMillis() - 1000);

        //WHEN
        subject.afterHandleEndpoint(filterContext);

        //THEN
        verify(accessLogger, times(1)).error(anyString());
        verifyNoMoreInteractions(accessLogger);
    }

    @Test
    public void shouldLogAtErrorLevelWhenResponseStatusCodeIs4xx() throws SuspendExecution {
        //GIVEN
        Response<byte[]> response = mock(Response.class);
        when(filterContext.getResponse()).thenReturn(response);
        when(response.getHttpStatusCode()).thenReturn(420);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(System.currentTimeMillis() - 1000);

        //WHEN
        subject.afterHandleEndpoint(filterContext);

        //THEN
        verify(accessLogger, times(1)).error(anyString());
        verifyNoMoreInteractions(accessLogger);
    }

    @Test
    public void shouldLogAtInfoLevelWhenResponseStatusCodeIs2xx() throws SuspendExecution {
        //GIVEN
        Response<byte[]> response = mock(Response.class);
        when(filterContext.getResponse()).thenReturn(response);
        when(response.getHttpStatusCode()).thenReturn(222);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(System.currentTimeMillis() - 1000);

        //WHEN
        subject.afterHandleEndpoint(filterContext);

        //THEN
        verify(accessLogger, times(1)).info(anyString());
        verifyNoMoreInteractions(accessLogger);
    }

    @Test
    public void shouldLogAtWarnLevelWhenResponseStatusCodeIs3xx() throws SuspendExecution {
        //GIVEN
        Response<byte[]> response = mock(Response.class);
        when(filterContext.getResponse()).thenReturn(response);
        when(response.getHttpStatusCode()).thenReturn(363);
        when(filterContext.getFilterVariable("startTimestamp")).thenReturn(System.currentTimeMillis() - 1000);

        //WHEN
        subject.afterHandleEndpoint(filterContext);

        //THEN
        verify(accessLogger, times(1)).warn(anyString());
        verifyNoMoreInteractions(accessLogger);
    }
}
