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

package com.helix.feature.restservice.controller.component;

import com.helix.core.component.Component;
import com.helix.feature.restservice.error.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Error Handler Configuration Component
 */
public class ErrorHandlerComponent implements Component {
    List<ErrorHandler> errorHandlers = new ArrayList<>();

    private ErrorHandlerComponent() {
    }

    /**
     * Create an error handler for all REST paths
     * @return Error handler component
     */
    public static ErrorHandlerComponent forAllPaths() {
        return new ErrorHandlerComponent();
    }

    /**
     * Add an error handler function, which will handle
     * any thrown exception of type T or subclasses of T.
     *
     * @param errorHandler Error handler function
     * @param <T> Exception type to handle
     * @return The ErrorHandlerComponent for this function
     */
    public <T> ErrorHandlerComponent handle(ErrorHandler<T> errorHandler) {
        errorHandlers.add(errorHandler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentType() {
        return "ErrorHandlerBuilder";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription() {
        return null;
    }

    /**
     * Returns all the ErrorHandlerDefinition components created
     * @return ErrorHandlerDefinition array
     */
    @Override
    public Component[] getContainedComponents() {
        return errorHandlers.toArray(new Component[errorHandlers.size()]);
    }
}
