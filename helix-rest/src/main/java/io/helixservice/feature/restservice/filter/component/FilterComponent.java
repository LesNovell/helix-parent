
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

package io.helixservice.feature.restservice.filter.component;

import io.helixservice.core.component.Component;
import io.helixservice.feature.restservice.filter.Filter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * REST Filter Configuration Component
 * <p>
 * Each filter should have a FilterComponent that is registered
 * with the feature that created it.
 */
public class FilterComponent implements Component {
    public static final String TYPE_NAME = "Filter";
    public static final String ALL_PATHS = ".*";

    private String pathRegex;
    private Filter filter;

    /**
     * Create a filter component on all URI paths
     *
     * @param filter Filter implementation
     * @return Filter configuration that to be installed in a Feature
     */
    public static FilterComponent filterAllPaths(Filter filter) {
        return new FilterComponent(filter);
    }

    /**
     * Create a filter component on specific URI paths
     *
     * @param pathRegex Regular expression
     * @param filter Filter implementation
     * @return Filter configuration that to be installed in a Feature
     */
    public static FilterComponent filterByRegex(String pathRegex, Filter filter) {
        return new FilterComponent(pathRegex, filter);
    }

    /**
     *
     * @return Regular expression or ".*" for all paths
     */
    public String getPathRegex() {
        return pathRegex;
    }

    /**
     * @return The filter implementation
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentType() {
        return TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.print("'" + pathRegex + "' -> Filter " + filter.getClass().getSimpleName());

        return stringWriter.toString();
    }

    private FilterComponent(Filter filter) {
        this.pathRegex = ALL_PATHS;
        this.filter = filter;
    }

    private FilterComponent(String pathRegex, Filter filter) {
        this.pathRegex = pathRegex;
        this.filter = filter;
    }
}
