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

package com.helix.core.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.MultiMap;

/**
 * Utility for type conversions related to Vert.x data structures
 */
public interface VertxTypeConverter {
    /**
     * Vert.x MultiMap to Guava Multimap.
     * Primarily used for request and response headers.
     *
     * @param multiMap Vert.x multimap
     * @return Multimap converted to Guava Multimap
     */
    static Multimap<String, String> toGuavaMultimap(MultiMap multiMap) {
        LinkedHashMultimap<String, String> result = LinkedHashMultimap.create();

        for (String key : multiMap.names()) {
            result.replaceValues(key, multiMap.getAll(key));
        }

        return result;
    }

    /**
     * Guava MultiMap to Vert.x  Multimap.
     * Primarily used for request and response headers.
     *
     * @param multimap Guava multimap
     * @return Multimap converted to Vert.x MultiMap
     */
    static MultiMap toVertxMultiMap(Multimap<String, String> multimap) {
        MultiMap result = MultiMap.caseInsensitiveMultiMap();

        for (String key : multimap.keys()) {
            result.set(key, multimap.get(key));
        }

        return result;
    }
}
