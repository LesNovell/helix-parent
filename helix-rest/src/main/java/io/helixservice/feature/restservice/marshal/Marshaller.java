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

package io.helixservice.feature.restservice.marshal;

import io.helixservice.core.component.Component;

/**
 * Interface that all message marshallers must implement
 */
public interface Marshaller extends Component {
    String TYPE_NAME = "Marshaller";
    Marshaller DEFAULT = new JacksonMarshaller();

    /**
     * Convert a Message to an Object
     *
     * @param targetType Class type the message should be converted to, if possible
     * @param message Message to be converted to an object
     * @return The object, otherwise throw a MarshallerException if the unmarshalling fails
     */
    Object unmarshal(Class targetType, Message message);

    /**
     * Convert a Object to a Message
     *
     * @param object Object that should be converted to a message
     * @return Message form of the object, otherwise throw a MarshallerException if the marshalling fails
     */
    Message marshal(Object object);

    /**
     * {@inheritDoc}
     */
    @Override
    default String getComponentType() {
        return TYPE_NAME;
    }
}
