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

package io.helixservice.feature.restservice.error.jsonapi;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

/**
 * For creating error responses in the standard JsonAPI message format
 * This should be inside an ErrorResponse object.
 *
 *  @see <a href="http://jsonapi.org/format/#errors">jsonapi.org</a>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ErrorData {
    private String id;
    private String aboutLink;
    private String status;
    private String code;
    private String title;
    private String detail;
    private String sourcePointer;
    private String sourceParameter;
    private Map<String, Object> meta;

    /**
     * Create an empty ErrorData
     */
    public ErrorData() {
        this.id = UUID.randomUUID().toString();
    }

    public ErrorData(String code, String title) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.title = title;
    }

    /**
     * Create an ErrorData with details
     *
     * @param code Service error code
     * @param title Error message title
     * @param detail Error message detail
     */
    public ErrorData(String code, String title, String detail) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    /**
     * Create an ErrorData with details
     *
     * @param code Service error code
     * @param title Error message title
     * @param detail A throwable which will be used for building a stack trace
     */
    public ErrorData(String code, String title, Throwable detail) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.title = title;

        StringWriter stringWriter = new StringWriter();
        detail.printStackTrace(new PrintWriter(stringWriter));
        this.detail = stringWriter.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAboutLink() {
        return aboutLink;
    }

    public void setAboutLink(String aboutLink) {
        this.aboutLink = aboutLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getSourcePointer() {
        return sourcePointer;
    }

    public void setSourcePointer(String sourcePointer) {
        this.sourcePointer = sourcePointer;
    }

    public String getSourceParameter() {
        return sourceParameter;
    }

    public void setSourceParameter(String sourceParameter) {
        this.sourceParameter = sourceParameter;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "ErrorData {" +
                "id='" + id + '\'' +
                ", aboutLink='" + aboutLink + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", sourcePointer='" + sourcePointer + '\'' +
                ", sourceParameter='" + sourceParameter + '\'' +
                ", meta=" + meta +
                '}';
    }
}
