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

package io.helixservice.feature.restservice.error;

import co.paralleluniverse.asm.Type;
import io.helixservice.core.component.Component;
import io.helixservice.feature.restservice.controller.Request;
import io.helixservice.feature.restservice.controller.Response;
import sun.reflect.ConstantPool;

import java.lang.reflect.Method;

/**
 * Implement this functional interface to provide error handling logic
 * for a particular exception type or its subclasses.
 * <p>
 * @param <T> The exception type this error handler will handle
 */
@FunctionalInterface
public interface ErrorHandler<T> extends Component {
    String TYPE_NAME = "ErrorHandler";

    /**
     * Handle an exception, providing the response that should be
     * returned to the client when an error happens.
     *
     * @param request The request details
     * @param t The exception that needs to be handled
     * @return A response that will be sent back to the client
     */
    Response mapToErrorResponse(Request request, T t);

    /**
     * {@inheritDoc}
     */
    @Override
    default String getComponentType() {
        return TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default String getComponentDescription() {
        return "for " + exceptionType().getSimpleName();
    }

    /**
     * Uses some (tricky) introspection to determine the exception type
     * this class will handle.
     *
     * @return The parametrized exception type (T)
     */
     default Class exceptionType() {
        Class<?> type;

        try {
            // Adapted from http://dan.bodar.com/2014/09/01/getting-the-generic-signature-of-a-java-8-lambda/
            Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
            getConstantPool.setAccessible(true);
            ConstantPool constantPool = (ConstantPool) getConstantPool.invoke(this.getClass());
            String[] methodRefInfo = constantPool.getMemberRefInfoAt(constantPool.getSize() - 2);

            int argumentIndex = 1;
            String argumentType = Type.getArgumentTypes(methodRefInfo[2])[argumentIndex].getClassName();
            type = Class.forName(argumentType);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to access generic type of ExceptionMapper class. Please check your JDK version and distribution.", t);
        }

        return type;
    }
}
