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

package io.helixservice.feature.restservice.controller;

import co.paralleluniverse.fibers.SuspendExecution;

/**
 * Functional interface for handling REST requests
 */
@FunctionalInterface
public interface Endpoint {
    /**
     * Handle a REST request
     *
     * @param request Request information
     * @return The Response object containing response body and headers
     * @throws Throwable If any exception occurs, the registered ErrorHandler for that exception will be invoked
     * @throws SuspendExecution For Vert.x Sync
     */
    Response handle(Request request) throws Throwable, SuspendExecution;
}
