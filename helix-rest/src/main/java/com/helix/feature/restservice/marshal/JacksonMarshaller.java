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

package com.helix.feature.restservice.marshal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JacksonMarshaller implements Marshaller {
    private static final List<String> JSON_CONTENT_TYPE = new ArrayList<>();
    private static final List<String> TEXT_PLAIN_CONTENT_TYPE = new ArrayList<>();
    private static final List<String> APPLICATION_OCTET_STREAM_TYPE = new ArrayList<>();

    static {
        JSON_CONTENT_TYPE.add("application/json");
        TEXT_PLAIN_CONTENT_TYPE.add("text/plain");
        APPLICATION_OCTET_STREAM_TYPE.add("application/octet-stream");
    }

    private ObjectMapper objectMapper;

    public JacksonMarshaller() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper = objectMapper;
    }

    public JacksonMarshaller(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getComponentDescription() {
        return "Jackson JSON ObjectMapper";
    }

    @Override
    public Object unmarshal(Class targetType, Message message) {
        Object result;

        try {
            // Add other special cases here, not directly supported by Jackson
            if (String.class.equals(targetType)) {
                result = message.getBody() == null ? "" : new String(message.getBody());
            } else {
                result = objectMapper.readValue(message.getBody(), targetType);
            }
        } catch (Exception e) {
            throw new MarshallerException("Unable to unmarshal the request", e);
        }

        return result;
    }

    @Override
    public Message marshal(Object object) {
        Message message;

        try {
            if (object == null) {
                message = new Message("{}".getBytes(), JSON_CONTENT_TYPE);
            } else if (String.class.equals(object.getClass())) {
                message = new Message(((String) object).getBytes(), TEXT_PLAIN_CONTENT_TYPE);
            } else if (byte[].class.equals(object.getClass())) {
                message = new Message(((byte[]) object), APPLICATION_OCTET_STREAM_TYPE);
            } else {
                String stringResponse = objectMapper.writeValueAsString(object);
                message = new Message(stringResponse.getBytes(), JSON_CONTENT_TYPE);
            }
        } catch (Exception e) {
            throw new MarshallerException("Unable to unmarshal the request", e);
        }

        return message;
    }
}
