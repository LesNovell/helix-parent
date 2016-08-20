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

package io.helixservice.feature.configuration.dynamo;

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.configuration.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Bootstrap Feature for obtaining configuration from AWS DynamoDB
 * <p>
 * This feature supports lookup of configuration based on environment,
 * reloading of properties changed in the DynamoDB
 *
 * Configuration Parameters:
 * <ul>
 *     <li>service.name: Name of the service that will be looked up in Spring Cloud Config</li>
 *     <li>dynamo.config.client.endpoint: URI to AWS DynamoDB service</li>
 *     <li>dynamo.config.access.key: AWS Access Key (optional)</li>
 *     <li>dynamo.config.secret.key: AWS Secret Key (optional)</li>
 *     <li>dynamo.config.service.name: Name of the service (optional)</li>
 *     <li>dynamo.config.table.name: DynamoDB Table Name (defaults to "ServiceConfiguration")</li>
 *     <li>dynamo.config.create.table: If 'true', will create the config table if it does not exist (defaults to true)</li>
 * </ul>
 *
 * System Environment Variables: (alternative to the configuration params above)
 * <ul>
 *     <li>AWS_ACCESS_KEY_ID</li>
 *     <li>AWS_SECRET_ACCESS_KEY</li>
 * </ul>
 */
public class DynamoConfigFeature extends AbstractFeature {
    private static Logger LOG = LoggerFactory.getLogger(DynamoConfigFeature.class);

    public DynamoConfigFeature() {
        ConfigProperty dynamoConfigEnabled = new ConfigProperty("dynamo.config.enabled", "true");

        if (dynamoConfigEnabled.isTrue()) {
            LOG.info("DynamoDB Configuration feature is enabled");

            String clientEndpoint = new ConfigProperty("dynamo.config.client.endpoint").getValue();
            String accessKey = Optional.ofNullable(System.getenv("AWS_ACCESS_KEY_ID"))
                    .orElse(new ConfigProperty("dynamo.config.access.key", "").getValue());
            String secretKey = Optional.ofNullable(System.getenv("AWS_SECRET_ACCESS_KEY"))
                    .orElse(new ConfigProperty("dynamo.config.secret.key", "").getValue());

            String tableName = new ConfigProperty("dynamo.config.table.name", "ServiceConfiguration").getValue();
            String serviceName = new ConfigProperty("dynamo.config.service.name", "default").getValue();
            boolean createTable = new ConfigProperty("dynamo.config.create.table", "true").isTrue();

            register(new DynamoConfigResourceLocator(clientEndpoint, accessKey, secretKey, tableName, serviceName, createTable));
        } else {
            LOG.warn("DynamoDB Configuration feature is disabled, because dynamo.config.enabled=" + dynamoConfigEnabled.getValue());
        }
    }

    public DynamoConfigFeature(String clientEndpoint, String accessKey, String secretKey,
            String tableName, String serviceName, boolean createTable) {
        register(new DynamoConfigResourceLocator(clientEndpoint, accessKey, secretKey, tableName, serviceName, createTable));
    }
}
