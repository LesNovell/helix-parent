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

package io.helixservice.feature.health;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Processes offline requests.
 * @see HealthCheckFeature for information on the query parameters to enable and disable offline mode
 */
public class QueryParameterOfflineProcessor implements OfflineProcessor<Multimap<String,String>> {

    private static final Logger LOG = LoggerFactory.getLogger(QueryParameterOfflineProcessor.class);

    private static final String OFFLINE_PARAMETER_KEY = "offline";
    private static final String SET_OFFLINE_VAL = "true";
    private static final String SET_ONLINE_VAL = "false";

    private static final String OFFLINE_PASSWORD_KEY = "password";
    private static final int FORCED_DOWN_PASSWORD_MIN_LENGTH = 3;
    private String forcedDownPassword;

    public QueryParameterOfflineProcessor(String forcedDownPassword) {
        if (forcedDownPassword == null) throw new IllegalArgumentException("Forced down password cannot be null");

        this.forcedDownPassword = forcedDownPassword;
        checkArgument(forcedDownPassword.length() >= FORCED_DOWN_PASSWORD_MIN_LENGTH,
                String.format("forced down password must contain at least %d characters", FORCED_DOWN_PASSWORD_MIN_LENGTH));
    }

    public void processInstruction(Multimap<String, String> queryParameters) {
        Stream<Map.Entry<String, String>> stream = queryParameters.entries().stream();
        boolean passwordMatch = stream.filter(queryParam ->
                        queryParam.getKey().equals(OFFLINE_PASSWORD_KEY)
                                && queryParam.getValue().equals(forcedDownPassword)
        ).count() == 1;

        if (passwordMatch) {
            queryParameters.entries().stream().filter(queryParam ->
                            queryParam.getKey().equals(OFFLINE_PARAMETER_KEY)
                                    && queryParam.getValue().equals(SET_OFFLINE_VAL)
            ).forEach(offlineInstruction -> {
                LOG.info("Setting server offline");
                Status.INSTANCE.setOffLine();
            });

            queryParameters.entries().stream().filter(queryParam ->
                            queryParam.getKey().equals(OFFLINE_PARAMETER_KEY)
                                    && queryParam.getValue().equals(SET_ONLINE_VAL)
            ).forEach(offlineInstruction -> {
                LOG.info("Setting server online");
                Status.INSTANCE.setOnLine();
            });
        }
    }
}
