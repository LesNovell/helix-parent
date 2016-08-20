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

package io.helixservice.feature.restservice;

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.core.server.Server;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import java.util.function.Supplier;

/**
 * Rest Service Feature provides non-blocking HTTP Server support
 * <p>
 * Features include:
 * <ul>
 * <li>Vert.x-based async HTTP Server</li>
 * <li>Controllers objects that define 1 or more REST endpoints</li>
 * <li>Configuration of Controllers via annotation or by Lambda definition</li>
 * <li>Filter support for intercepting REST requests</li>
 * <li>Error handling support, </li>
 * </ul>
 */
public class RestServiceFeature extends AbstractFeature {
    private Router router;

    @Override
    public void start(Server server) {
        Vertx vertx = server.getVertx().get();
        router = Router.router(vertx);

        RestServiceVerticle restServiceVerticle = new RestServiceVerticle(server, router);
        vertx.deployVerticle(restServiceVerticle);
    }

    public Supplier<Router> getRouter() {
        return () -> router;
    }
}
