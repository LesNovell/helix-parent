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

package io.helixservice.core.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.MultiMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VertxTypeConverterUnitTest {

    @Test
    public void testToVertxMultimap() {
        Multimap<String,String> guavaMultimap = HashMultimap.create();
        guavaMultimap.put("a", "z");
        guavaMultimap.put("a", "b");
        guavaMultimap.put("a", "c");
        guavaMultimap.put("c", "z");

        MultiMap multiMap = VertxTypeConverter.toVertxMultiMap(guavaMultimap);

        assertEquals(multiMap.names().size(),2);
        assertEquals(multiMap.getAll("a").get(0), "z");
        assertEquals(multiMap.getAll("a").get(1), "b");
        assertEquals(multiMap.getAll("a").get(2), "c");
        assertEquals(multiMap.get("c"), "z");
    }

    @Test
    public void testToGuavaMultimap() {
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("a", "z");
        multiMap.add("a", "b");
        multiMap.add("a", "c");
        multiMap.add("c", "z");

        Multimap<String, String> multimap = VertxTypeConverter.toGuavaMultimap(multiMap);

        assertEquals(multimap.keySet().size(), 2);
        assertEquals(multimap.get("a").iterator().next(), "z");
        assertEquals(multimap.get("a").iterator().next(), "b", "b");
        assertEquals(multimap.get("a").iterator().next(), "c", "c");
        assertEquals(multimap.get("c").iterator().next(), "z");
    }
}
