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

package io.helixservice.feature.configuration.cloudconfig;

import io.helixservice.feature.configuration.resolver.PropertyResolver;

/**
 * Property resolver, which is able to resolve and decrypt properties
 * via the Cloud Config server.
 * <p>
 * There are two ways to indicate a property should be decrypted:
 * <ol>
 *     <li>Property name ends with .secret</li>
 *     <li>and/or Property value format should be prepended with {cipher}</li>
 * </ol>
 * <p>
 * This property resolver automatically gets loaded by the Configuration Feature.
 * It is possible to decrypt values coming from both any resource provider including
 * filesystem and classpath.
 */
public class CloudConfigSecurePropertyResolver implements PropertyResolver {
    public static final String SECRET_POSTFIX = ".secret";

    private final String cloudConfigUri;
    private final String httpBasicHeader;

    public CloudConfigSecurePropertyResolver(String cloudConfigUri, String httpBasicHeader) {
        this.cloudConfigUri = cloudConfigUri;
        this.httpBasicHeader = httpBasicHeader;
    }

    /**
     * Resolve a property, decrypting if possible
     *
     * @param name property name
     * @param value property value
     * @return decrypted property value, or just value if no decryption was performed
     */
    @Override
    public String resolve(String name, String value) {
        String result = value;

        if (sensitive(name, value)) {
            result = CloudConfigDecrypt.decrypt(name, value, cloudConfigUri, httpBasicHeader);
        }

        return result;
    }

    /**
     * Returns true when the property appears to be a encrypted property
     *
     * @param name property name
     * @param value property value
     * @return true if this property appears to be encrypted
     */
    @Override
    public boolean sensitive(String name, String value) {
        boolean sensitive = false;

        if (name != null && value != null) {
            sensitive = name.endsWith(SECRET_POSTFIX) || value.startsWith(CloudConfigDecrypt.CIPHER_PREFIX);
        }

        return sensitive;
    }

    @Override
    public String getComponentDescription() {
        return "Cloud Config Secure Property Resolver";
    }
}
