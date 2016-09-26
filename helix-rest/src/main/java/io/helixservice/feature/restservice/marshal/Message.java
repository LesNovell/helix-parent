
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

package io.helixservice.feature.restservice.marshal;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a message body and its type
 */
public class Message {
    private byte[] body;
    private List<String> contentTypes;

    /**
     * Creates a message object
     *
     * @param body Message body, as a byte array
     * @param contentTypes List of HTTP mime content types this message is encoded as
     */
    public Message(byte[] body, List<String> contentTypes) {
        this.body = body;
        this.contentTypes = contentTypes;
    }

    /**
     * Creates a message object
     *
     * @param body Message body, as a byte array
     * @param contentType HTTP mime content type this message is encoded as
     */
    public Message(byte[] body, String contentType) {
        this.body = body;
        this.contentTypes = new ArrayList<>();
        this.contentTypes.add(contentType);
    }

    /**
     * Returns the body of this message
     *
     * @return Body as a byte array
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Gets a list of content types for this message
     *
     * @return List of HTTP mime content types
     */
    public List<String> getContentTypes() {
        return contentTypes;
    }

    /**
     * Gets the first content type for this message
     *
     * @return A singular HTTP mim content type, or empty string if none has been specified
     */
    public String getContentType() {
        String contentType = "";

        if (contentTypes.size() > 0) {
            contentType = contentTypes.get(0);
        }

        return contentType;
    }
}
