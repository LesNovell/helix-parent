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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.util.StringInputStream;
import com.amazonaws.util.StringUtils;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.helixservice.feature.configuration.locator.AbstractResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class DynamoConfigResourceLocator extends AbstractResourceLocator {
    private static Logger LOG = LoggerFactory.getLogger(DynamoConfigResourceLocator.class);

    private static final String APPLICATION_YAML_FILE = "/application.yml";

    private final String clientEndpoint;
    private final String accessKey;
    private final String secretKey;
    private final String tableName;
    private final String serviceName;

    private AmazonDynamoDBClient client;
    private Table configTable;

    public DynamoConfigResourceLocator(String clientEndpoint, String accessKey, String secretKey,
            String tableName, String serviceName, boolean createTable) {
        Objects.requireNonNull(clientEndpoint, "clientEndpoint cannot be null");
        Objects.requireNonNull(tableName, "tableName cannot be null");
        Objects.requireNonNull(serviceName, "serviceName cannot be null");

        this.clientEndpoint = clientEndpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.tableName = tableName;
        this.serviceName = serviceName;

        configureDynamoConnection();
        if (createTable) {
            createTable();
        }
    }

    private void configureDynamoConnection() {
        if (!StringUtils.isNullOrEmpty(accessKey) && !StringUtils.isNullOrEmpty(secretKey)) {
            //set the credentials as system properties
            System.setProperty("aws.accessKeyId", accessKey);
            System.setProperty("aws.secretKey", secretKey);
            LOG.info("accessKeyId and secretKey passed successfully via configuration");
        } else {
            LOG.error("accessKeyId or secretKey not set");
        }

        client = new AmazonDynamoDBClient().withEndpoint(clientEndpoint);
        configTable = new DynamoDB(client).getTable(tableName);
    }

    private void createTable() {
        try {
            client.describeTable(tableName);
        } catch (ResourceNotFoundException e) {
            ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName("environment")
                    .withAttributeType(ScalarAttributeType.S));
            attributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName("service")
                    .withAttributeType(ScalarAttributeType.S));

            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement().withAttributeName("environment").withKeyType(KeyType.HASH));
            keySchema.add(new KeySchemaElement().withAttributeName("service").withKeyType(KeyType.RANGE));

            ProvisionedThroughput provisioning = new ProvisionedThroughput(8L, 4L);
            CreateTableRequest createTableRequest = new CreateTableRequest(attributeDefinitions, tableName, keySchema, provisioning);
            client.createTable(createTableRequest);
        }
    }

    @Override
    public Optional<InputStream> getStream(String resourcePath) {
        Optional<InputStream> result = Optional.empty();

        if (resourcePath.endsWith(APPLICATION_YAML_FILE)) {
            result = getPropertiesFromDynamoDB(resourcePath, result);
        }

        return result;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<InputStream> getPropertiesFromDynamoDB(String resourcePath, Optional<InputStream> pResult) {
        Optional<InputStream> result = pResult;

        String environmentName = resourcePath.substring(0, resourcePath.lastIndexOf(APPLICATION_YAML_FILE));
        try {
            Item item = configTable.getItem("environment", environmentName, "service", serviceName);
            if (item == null) {
                LOG.info("No configuration found for environment=" + environmentName + "; creating default empty configuration");
                createEmptyConfiguration(environmentName);
            } else {
                String json = item.toJSON();
                JSONObject jsonObject = new JSONObject(json);
                JSONObject config = jsonObject.getJSONObject("config");

                ObjectMapper objectMapper = new ObjectMapper();
                Map mapOfValues = objectMapper.readValue(config.toString(), Map.class);

                ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
                String yamlResult = yamlObjectMapper.writeValueAsString(mapOfValues);

                result = Optional.of(new StringInputStream(yamlResult));
            }
        } catch (Throwable t) {
            LOG.error("Unable to load configuration from DynamoDB for environment=" + environmentName, t);
        }

        return result;
    }

    private void createEmptyConfiguration(String environmentName) {
        try {
            Item item = new Item();
            item.withString("environment", environmentName);
            item.withString("service", serviceName);
            item.withJSON("config", "{}");
            configTable.putItem(item);
            LOG.info("Created default configuration in DynamoDB for environment=" + environmentName);
        } catch (Throwable t) {
            LOG.error("Unable to create default configuration in DynamoDB for environment=" + environmentName, t);
        }
    }

    @Override
    public String getBasePath() {
        return "dynamodbconfig://";
    }

    @Override
    public String getComponentDescription() {
        return "DynamoDB Config Resource Locator";
    }
}
