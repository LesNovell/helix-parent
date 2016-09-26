
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

package io.helixservice.feature.jpa;

import io.helixservice.core.container.Container;
import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.configuration.ConfigProperties;
import io.helixservice.feature.configuration.ConfigProperty;
import io.helixservice.feature.configuration.provider.ConfigProvider;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides JPA support via Hibernate implementation.
 * <p>
 * This JPA Feature will scan for entities in the packages
 * specified by the <b>jpa.entity-packages</b> configuration value.
 * <p>
 * Hibernate properties can be provided by passing Hibernate configuration
 * parameters as a under <b>jpa.provider</b> configuration tree.
 * <p>
 * Multiple persistence units are supported, by registering this feature
 * more than once.  When defining multiple persistence units, you must
 * provide a separate name and configuration for each.
 */
public class JpaHibernateFeature extends AbstractFeature {
    public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";

    private static Logger LOG = LoggerFactory.getLogger(JpaHibernateFeature.class);
    private static Map<String, EntityManagerFactory> EM_FACTORY_MAP = new HashMap<>();

    private String persistenceUnitName;
    private Map persistenceProviderProperties;
    private String packagesToScanForEntities;

    /**
     * Create a new JPA Hibernate feature as a default persistence unit
     */
    public JpaHibernateFeature(ConfigProvider configProvider) {
        this.persistenceUnitName = DEFAULT_PERSISTENCE_UNIT_NAME;
        this.persistenceProviderProperties = new ConfigProperties(configProvider, "jpa.provider").toMapOfProperties(true);
        this.packagesToScanForEntities = new ConfigProperty(configProvider, "jpa.entity-packages").getValue();
//        this(DEFAULT_PERSISTENCE_UNIT_NAME,
//                new ConfigProperties(configProvider, "jpa.provider").toMapOfProperties(true),
//                new ConfigProperty(configProvider, "jpa.entity-packages").getValue());
    }

//    /**
//     * Create a new JPA Hibernate feature, with custom persistence unit name.
//     *
//     * @param persistenceUnitName Unique persistence unit name
//     * @param persistenceProviderProperties Map of properties that will be passed to Hibernate
//     * @param packagesToScanForEntities List of packages to scan for entities
//     */
//    private JpaHibernateFeature(String persistenceUnitName, Map persistenceProviderProperties, String packagesToScanForEntities) {
//        this.persistenceUnitName = persistenceUnitName;
//        this.persistenceProviderProperties = persistenceProviderProperties;
//        this.packagesToScanForEntities = packagesToScanForEntities;
//    }

    /**
     * Lookup the default entity manager factory, if only one persistence unit is defined.
     *
     * @return The default entity manager factory or NULL if it does not exist.
     */
    public static EntityManagerFactory defaultEntityManagerFactory() {
        return entityManagerFactoryByPersistenceUnitName(DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Lookup the default entity manager factory by it's persistence unit name
     *
     * @param persistenceUnitName Persistence unit name
     * @return The entity manager factory, or NULL if it does not exist.
     */
    public static EntityManagerFactory entityManagerFactoryByPersistenceUnitName(String persistenceUnitName) {
        return EM_FACTORY_MAP.get(persistenceUnitName);
    }


    /**
     * Start the Hibernate JPA Persistence unit
     *
     * @param container Helix server
     */
    @Override
    public void start(Container container) {
        HibernatePersistenceUnitInfo persistenceUnit = new HibernatePersistenceUnitInfo(persistenceUnitName);
        setupProperties(persistenceUnit, persistenceProviderProperties);
        setupPackagesToScan(persistenceUnit, packagesToScanForEntities);

        EM_FACTORY_MAP.put(persistenceUnitName, createEntityManagerFactory(persistenceProviderProperties, persistenceUnit));
    }

    /**
     * Stop the Hibernate JPA Persistence unit,
     * releasing resources and connections
     *
     * @param container Helix server
     */
    @Override
    public void stop(Container container) {
        EntityManagerFactory entityManagerFactory = EM_FACTORY_MAP.get(persistenceUnitName);
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            EM_FACTORY_MAP.remove(persistenceUnitName);
        }
    }

    private EntityManagerFactory createEntityManagerFactory(Map persistenceProviderProperties, HibernatePersistenceUnitInfo persistenceUnit) {
        HibernatePersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        return persistenceProvider.createContainerEntityManagerFactory(persistenceUnit, persistenceProviderProperties);
    }

    private void setupProperties(HibernatePersistenceUnitInfo persistenceUnit, Map persistenceProviderProperties) {
        persistenceUnit.getProperties().putAll(persistenceProviderProperties);
    }

    private void setupPackagesToScan(HibernatePersistenceUnitInfo persistenceUnit, String packagesToScanForEntities) {
        String[] packages = packagesToScanForEntities.replace(".", "/").split(",");
        for (String aPackage : packages) {
            URL resource = persistenceUnit.getClassLoader().getResource(aPackage);
            if (resource != null) {
                persistenceUnit.getJarFileUrls().add(resource);
                LOG.info("Scanning package for JPA entities: " + aPackage);
            } else {
                throw new IllegalArgumentException("Package not found for entity scanning. packageName=" + aPackage
                        + ", packagesToScanForEntities=" + packagesToScanForEntities);
            }
        }
    }
}
