
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

package io.helixservice.feature.configuration.cloudconfig;

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.configuration.ConfigProperty;
import io.helixservice.feature.configuration.ConfigurationFeature;
import io.helixservice.feature.configuration.provider.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Optional;

/**
 * Bootstrap Feature for obtaining configuration from Spring Cloud Config
 * <p>
 * This feature supports lookup of configuration based on environment,
 * reloading of properties changed in the Spring Cloud Config, and
 * decryption of properties and files via Cloud Config decryption.
 * <p>
 * This feature speaks to Cloud Config via a REST protocol, it does not
 * have any code dependencies on Spring.
 * <p>
 * Configuration Parameters:
 * <ul>
 *     <li>service.name: Name of the service that will be looked up in Spring Cloud Config</li>
 *     <li>spring.cloud.config.uri: URI to the Cloud Config server</li>
 *     <li>spring.cloud.config.username: Cloud Config server username</li>
 *     <li>spring.cloud.config.password: Cloud Config server password</li>
 * </ul>
 *
 * System Environment Variables: (alternative to the configuration params above)
 * <ul>
 *     <li>SPRING_CLOUD_CONFIG_URI: URI to the Cloud Config server</li>
 *     <li>SPRING_CLOUD_CONFIG_USERNAME: Cloud Config server username</li>
 *     <li>SPRING_CLOUD_CONFIG_PASSWORD: Cloud Config server password</li>
 * </ul>
 *
 * <p>
 * Usage:
 * <br>
 * Installing this feature will enable Spring Cloud config
 * functionality.  The application yaml file will be loaded from
 * Spring Cloud Config, and if present will be applied on any
 * configuration property lookups automatically.
 * <p>
 * Decryption of Properties:
 * Properties that are encrypted must start with {cipher} in the
 * value or have .secret postfix. Decryption of properties named this way
 * is automatic, and the decrypted properties are captured in memory
 * and never stored on the filesystem.
 * <p>
 * Decryption of Files:
 * Files can also be decrypted, either on the classpath or filesystem.
 * The filename must have a .secret postfix to indicate that decryption is needed.
 */
public class CloudConfigFeature extends AbstractFeature {
    private static Logger LOG = LoggerFactory.getLogger(CloudConfigFeature.class);
    private ConfigProvider configProvider;

    public CloudConfigFeature(ConfigProvider configProvider) {
        this.configProvider = configProvider;
        ConfigProperty cloudConfigEnabled = new ConfigProperty(configProvider, "spring.cloud.config.enabled", "true");

        if (cloudConfigEnabled.isTrue()) {
            LOG.info("Cloud Configuration feature is enabled");

            registerCloudConfigProvider();
        } else {
            LOG.warn("Cloud Configuration feature is disabled, because spring.cloud-config.enabled=" + cloudConfigEnabled.getValue());
        }
    }

    @Override
    public boolean shouldStartDuringBootstrapPhase() {
        return true;
    }

    private void registerCloudConfigProvider() {
        String serviceName = new ConfigProperty(configProvider, "service.name").getValue();
        String cloudConfigUri = Optional.ofNullable(System.getenv("SPRING_CLOUD_CONFIG_URI"))
                .orElse(new ConfigProperty(configProvider, "spring.cloud.config.uri").getValue());
        String username = Optional.ofNullable(System.getenv("SPRING_CLOUD_CONFIG_USERNAME"))
                .orElse(new ConfigProperty(configProvider, "spring.cloud.config.username").getValue());
        String password = Optional.ofNullable(System.getenv("SPRING_CLOUD_CONFIG_PASSWORD"))
                .orElse(new ConfigProperty(configProvider, "spring.cloud.config.password").getValue());
        String httpBasicHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        String serviceUrl = cloudConfigUri + "/" + serviceName;

        CloudConfigResourceLocator cloudConfigResourceLocator =
                new CloudConfigResourceLocator(serviceUrl, cloudConfigUri, httpBasicHeader,
                        ConfigurationFeature.getConfigBaseFilePath());

        CloudConfigSecurePropertyResolver cloudConfigSecurePropertyResolver =
                new CloudConfigSecurePropertyResolver(cloudConfigUri, httpBasicHeader);

        register(cloudConfigResourceLocator);
        register(cloudConfigSecurePropertyResolver);
    }
}
