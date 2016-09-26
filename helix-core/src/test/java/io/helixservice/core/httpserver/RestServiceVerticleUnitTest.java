
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

package io.helixservice.core.httpserver;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class RestServiceVerticleUnitTest {
    @Test
    public void testStart() throws Exception {
//        Vertx vertx = mock(Vertx.class);
//        Context context = mock(Context.class);
//
//        HttpServer httpServer = mock(HttpServer.class);
//        Supplier<HttpServer> httpServerSupplier = mock(Supplier.class);
//
//        Supplier<Router> routeSupplier = mock(Supplier.class);
//        Router router = mock(Router.class);
//
//        when(httpServerSupplier.get()).thenReturn(httpServer);
//        when(httpServer.requestHandler(Matchers.<Handler<HttpServerRequest>>any())).thenReturn(httpServer);
//        when(routeSupplier.get()).thenReturn(router);
//
//        RestServiceVerticle subject = new RestServiceVerticle(httpServerSupplier, routeSupplier) {
//            @Override
//            public Handler<HttpServerRequest> getHandler(Router router) {
//                return router::accept;
//            }
//        };
//        subject.init(vertx, context);
//
//        subject.start();
//
//        verify(httpServer, times(1)).listen();
    }
}
