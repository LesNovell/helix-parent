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

package io.helixservice.feature.restservice.filter;

import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FilterContextUnitTest {
    @Test
    public void testFilterContext() {

        Request<byte[]> request = mock(Request.class);
        Response<byte[]> response = Response.successResponse(null);
        Map<String,Object> filterVariables = new HashMap<String,Object>();

        filterVariables.put("a", "b");
        FilterContext filterContext = new FilterContext(request, response, filterVariables);
        assertEquals("b", filterContext.getFilterVariables().get("a"));


        filterContext.setFilterVariable("startTime", -1L);
        long startTime = filterContext.getFilterVariable("startTime");
        assertEquals(-1L, startTime);

        assertFalse(filterContext.isSendResponseFromFilter());
        filterContext.endWithResponse(444, "error");
        assertTrue(filterContext.isSendResponseFromFilter());
        Assert.assertEquals(444, filterContext.getResponse().getHttpStatusCode());
        assertEquals("error", new String(filterContext.getResponse().getResponseBody()));
    }
}
