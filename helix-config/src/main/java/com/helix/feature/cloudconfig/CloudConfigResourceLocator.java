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

package com.helix.feature.cloudconfig;

import com.helix.feature.configuration.locator.AbstractResourceLocator;
import com.helix.feature.configuration.provider.YamlPropertiesLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Finds YAML property files from the Cloud Config server
 * or, if a file on the filesystem decrypted with Cloud Config Server
 */
public class CloudConfigResourceLocator extends AbstractResourceLocator {
    private static Logger LOG = LoggerFactory.getLogger(CloudConfigResourceLocator.class);

    private static final String APPLICATION_YAML_FILE = "/application.yml";
    private static final String SECRET_FILE = ".secret";
    private static final String PROPERTY_SOURCES_YAML_PREFIX = "propertySources[0].source.";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private String serviceUri;
    private String cloudConfigUri;
    private String httpBasicHeader;
    private String basePath;

    public CloudConfigResourceLocator(String serviceUri, String cloudConfigUri, String httpBasicHeader, String basePath) {
        this.serviceUri = serviceUri;
        this.cloudConfigUri = cloudConfigUri;
        this.httpBasicHeader = httpBasicHeader;
        this.basePath = basePath;
    }

    /**
     * Gets a stream for either a ".yaml" on Cloud Config Server
     * or ".secret" file decrypted from the local filesystem
     * <p>
     * This operation does block on HTTP request.
     * <p>
     * @param resourcePath Resource path, may be ".yaml" or ".secret" file.
     * @return An optional, containing an InputStream if the resource could be found
     */
    @Override
    public Optional<InputStream> getStream(String resourcePath) {
        Optional<InputStream> result = Optional.empty();

        if (resourcePath.endsWith(APPLICATION_YAML_FILE)) {
            result = requestCloudConfigProperties(resourcePath, result);
        } else if (resourcePath.endsWith(SECRET_FILE)) {
            result = decryptSecretFile(resourcePath);
        }

        return result;
    }

    private Optional<InputStream> requestCloudConfigProperties(String resourcePath, Optional<InputStream> result) {
        String cloudConfigResource = resourcePath.substring(0, resourcePath.lastIndexOf(APPLICATION_YAML_FILE)).replace("default", "default");
        String url = serviceUri + "/" + cloudConfigResource;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty(AUTHORIZATION_HEADER, httpBasicHeader);

            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                InputStream extractedPropertiesInputStream = extractProperties(inputStream);
                result = Optional.of(extractedPropertiesInputStream);
                inputStream.close();
            } else {
                LOG.error("Unable to get Cloud Config due to httpStatusCode=" + connection.getResponseCode() + " for cloudConfigUrl=" + url);
            }
        } catch (IOException e) {
            LOG.error("Unable to connect to Cloud Config server at cloudConfigUrl=" + url + " exception=" + e.getClass().getSimpleName() + " exceptionMessage=" + e.getMessage());
        }
        return result;
    }

    private InputStream extractProperties(InputStream inputStream) throws IOException {
        YamlPropertiesLoader yamlPropertiesLoader = new YamlPropertiesLoader(inputStream);
        Map<String, Object> flattenedProperties = yamlPropertiesLoader.getFlattenedProperties();

        Map<String, Object> filteredProperties = flattenedProperties.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(PROPERTY_SOURCES_YAML_PREFIX))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String yamlDocument = new Yaml().dumpAsMap(filteredProperties);

        return new ByteArrayInputStream(yamlDocument.getBytes());
    }

    private Optional<InputStream> decryptSecretFile(String resourcePath) {
        Optional<InputStream> result = Optional.empty();

        LOG.debug("Looking for encrypted file:" + resourcePath);

        File file = new File(basePath + File.separator + resourcePath);
        if (file.exists()) {
            try {
                FileInputStream encryptedFileInputStream = new FileInputStream(file);
                String encryptedData = IOUtils.toString(encryptedFileInputStream);
                String decryptedData = CloudConfigDecrypt.decrypt(resourcePath, encryptedData, cloudConfigUri, httpBasicHeader);

                result = Optional.of(new ByteArrayInputStream(decryptedData.getBytes()));
                encryptedFileInputStream.close();
            } catch (IOException e) {
                LOG.error("Unexpected file not found for encrypted file=" + file.getAbsolutePath(), e);
            }
        }

        return result;
    }

    @Override
    public String getBasePath() {
        return "cloudconfig://";
    }

    @Override
    public String getComponentDescription() {
        return "Cloud Config Resource Locator";
    }
}
