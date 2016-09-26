
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

package io.helixservice.feature.restservice.error;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for exception handlers
 */
public class ErrorHandlerRegistry {
    private Map<Class, ErrorHandlerFunction> exceptionHandlersMap = new HashMap<>();

    /**
     * Register an error handler
     *
     * @param errorHandler Error handler function
     */
    public void addErrorHandler(ErrorHandlerFunction errorHandler) {
        exceptionHandlersMap.put(errorHandler.exceptionType(), errorHandler);
    }

    /**
     * Register a collection of handlers
     *
     * @param errorHandlers Error handler functions
     */
    public void addErrorHandlers(Collection<ErrorHandlerFunction> errorHandlers) {
        errorHandlers.forEach(this::addErrorHandler);
    }

    /**
     * Finds the best error handler for the given exception instance
     *
     * @param exceptionToMap Exception that occurred
     * @param <T> Exception type returned
     * @return The error handler, or the default error handler if none is registered
     */
    public <T extends Throwable> ErrorHandlerFunction<T> errorHandlerFor(T exceptionToMap) {
        return errorHandlerFor(exceptionToMap.getClass());
    }

    /**
     * Finds the best error handler for the given exception class
     *
     * @param exceptionClassToMap Exception type to find
     * @param <T> Exception type returned
     * @return The error handler, or the default error handler if none is registered
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> ErrorHandlerFunction<T> errorHandlerFor(Class exceptionClassToMap) {
        ErrorHandlerFunction<T> result = exceptionHandlersMap.get(exceptionClassToMap);

        if (result == null && exceptionClassToMap.getSuperclass() != null) {
            result = errorHandlerFor(exceptionClassToMap.getSuperclass());
        }

        if (result == null) {
            return DefaultErrorHandler::defaultExceptionMapper;
        }

        return result;
    }
}
