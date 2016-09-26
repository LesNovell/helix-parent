
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

package io.helixservice.feature.configuration.provider;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads a flattened property list from yaml
 */
public class YamlPropertiesLoader {
    private Map<String, Object> flattenedProperties;

    /**
     * @param inputStream The YAML file's input stream
     */
    public YamlPropertiesLoader(InputStream inputStream) {
        Map yamlSource = (Map) new Yaml().load(inputStream);
        flattenedProperties = getFlattenedMap(yamlSource);
    }

    /**
     * Get all the properties flattened into simple dot notation.
     * @return The map of flattened properties
     */
    public Map<String, Object> getFlattenedProperties() {
        return flattenedProperties;
    }

    private Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (source != null) {
            buildFlattenedMap(result, source, null);
        }
        return result;
    }

    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (path != null && !path.trim().isEmpty()) {
                if (key.startsWith("[")) {
                    key = path + key;
                }
                else {
                    key = path + "." + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            }
            else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            }
            else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result,
                            Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            }
            else {
                result.put(key, value == null ? "" : value);
            }
        }
    }

}
