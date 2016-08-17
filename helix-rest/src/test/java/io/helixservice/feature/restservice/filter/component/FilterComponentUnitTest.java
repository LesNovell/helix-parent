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

package io.helixservice.feature.restservice.filter.component;

import io.helixservice.feature.restservice.filter.Filter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FilterComponentUnitTest {
    @Test
    public void testAllPathsConstructor() {
        Filter filter = mock(Filter.class);
        FilterComponent filterComponent = FilterComponent.filterAllPaths(filter);
        assertEquals(".*", filterComponent.getPathRegex());
        assertEquals(filter, filterComponent.getFilter());
    }

    @Test
    @Ignore
    public void testExplicitPathConstructor() {
        Filter filter = mock(Filter.class);
        FilterComponent filterComponent = FilterComponent.filterByRegex("/foo/.*", filter);
        assertEquals("/foo/.*", filterComponent.getPathRegex());
        assertEquals(filter, filterComponent.getFilter());

        String output = filterComponent.toString();
        assertTrue(output.startsWith("'/foo/.*' -> Filter Filter$"));
    }

}
