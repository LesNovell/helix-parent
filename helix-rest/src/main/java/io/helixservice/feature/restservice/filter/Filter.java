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

package io.helixservice.feature.restservice.filter;

import co.paralleluniverse.fibers.SuspendExecution;

/**
 * All filters must implement this interface
 */
public interface Filter {
    /**
     * Called before a Controller endpoint is invoked
     * <p>
     * This method may decide to handle the request,
     * and return a response without the request being
     * forwarded on to the controller endpoint.
     *
     * @param filterContext Request Context
     * @throws SuspendExecution Required by Vert.x Sync
     */
    void beforeHandleEndpoint(FilterContext filterContext) throws SuspendExecution;

    /**
     * Called after a Controller endpoint or its error handler has been called
     * <p>
     * The response headers may be modified
     *
     * @param filterContext Request Context
     * @throws SuspendExecution Required by Vert.x Sync
     */
    void afterHandleEndpoint(FilterContext filterContext) throws SuspendExecution;


    /**
     * Called after the response has been sent and all other processing is completed
     * <p>
     * No response may be modified at this point
     *
     * @param filterContext Request Context
     * @throws SuspendExecution Required by Vert.x Sync
     */
    void afterResponseSent(FilterContext filterContext) throws SuspendExecution;
}
