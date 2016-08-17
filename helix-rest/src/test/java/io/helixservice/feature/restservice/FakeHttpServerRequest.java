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


import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

public class FakeHttpServerRequest implements HttpServerRequest {

    Handler<Buffer> dataHandler;
    Handler<Void> endHandler;
    private String uri;
    private MultiMap params;
    private MultiMap headers;
    private SocketAddress remoteAddress;
    private HttpVersion version;
    private HttpServerResponse response;

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        return this;
    }

    @Override
    public HttpServerRequest handler(Handler<Buffer> handler) {
        this.dataHandler = handler;
        return this;
    }

    @Override
    public HttpServerRequest pause() {
        return null;
    }

    @Override
    public HttpServerRequest resume() {
        return null;
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    @Override
    public HttpVersion version() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public String uri() {
        return this.uri;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String query() {
        return null;
    }

    @Override
    public HttpServerResponse response() {
        return response;
    }

    public void setResponse(HttpServerResponse response) {
        this.response = response;
    }

    @Override
    public MultiMap headers() {
        return headers;
    }

    @Override
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    @Override
    public String getHeader(CharSequence charSequence) {
        return null;
    }

    @Override
    public MultiMap params() {
        return this.params;
    }

    @Override
    public String getParam(String paramName) {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public SocketAddress localAddress() {
        return null;
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return new X509Certificate[0];
    }

    @Override
    public String absoluteURI() {
        return null;
    }

    @Override
    public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
        return null;
    }

    @Override
    public NetSocket netSocket() {
        return null;
    }

    @Override
    public HttpServerRequest setExpectMultipart(boolean expect) {
        return null;
    }

    @Override
    public boolean isExpectMultipart() {
        return false;
    }

    @Override
    public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        return null;
    }

    @Override
    public MultiMap formAttributes() {
        return null;
    }

    @Override
    public String getFormAttribute(String attributeName) {
        return null;
    }

    @Override
    public ServerWebSocket upgrade() {
        return null;
    }

    @Override
    public boolean isEnded() {
        return false;
    }


    public void processDataHandler(Buffer data) {
        this.dataHandler.handle(data);
    }

    public void processEndHandler() {
        this.endHandler.handle(null);
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setParams(MultiMap params) {
        this.params = params;
    }

    public void setHeaders(MultiMap headers) {
        this.headers = headers;
    }
}
