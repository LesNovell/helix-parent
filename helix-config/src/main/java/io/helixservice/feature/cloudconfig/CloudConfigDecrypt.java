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
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 */

package io.helixservice.feature.cloudconfig;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Calls Cloud Config decryption services to decrypt a string
 */
public class CloudConfigDecrypt {
    private static Logger LOG = LoggerFactory.getLogger(CloudConfigSecurePropertyResolver.class);

    public static final String CIPHER_PREFIX = "{cipher}";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Decrypt an encrypted string
     * <p>
     * This method blocks on a HTTP request.
     *
     * @param name property or filename for reference/logging
     * @param encryptedValue Encrypted string
     * @param cloudConfigUri URI of the Cloud Config server
     * @param httpBasicHeader HTTP Basic header containing username & password for Cloud Config server
     * @return
     */
    public static String decrypt(String name, String encryptedValue, String cloudConfigUri, String httpBasicHeader) {
        String result = encryptedValue;

        // Remove prefix if needed
        if (encryptedValue.startsWith(CIPHER_PREFIX)) {
            encryptedValue = encryptedValue.substring(CIPHER_PREFIX.length());
        }

        String decryptUrl = cloudConfigUri + "/decrypt";

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(decryptUrl).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestMethod("POST");
            connection.addRequestProperty(AUTHORIZATION_HEADER, httpBasicHeader);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("Content-Length",  Integer.toString(encryptedValue.getBytes().length));
            connection.setRequestProperty("Accept", "*/*");

            // Write body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(encryptedValue.getBytes());
            outputStream.close();

            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                result = IOUtils.toString(inputStream);
                inputStream.close();
            } else {
                LOG.error("Unable to Decrypt name=" + name + " due to httpStatusCode=" + connection.getResponseCode() + " for decryptUrl=" + decryptUrl);
            }
        } catch (IOException e) {
            LOG.error("Unable to connect to Cloud Config server at decryptUrl=" + decryptUrl, e);
        }

        return result;
    }

}
