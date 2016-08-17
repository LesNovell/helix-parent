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

package io.helixservice.feature.jpa;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Configuration for JPA to setup Hibernate as a provider
 */
class HibernatePersistenceUnitInfo implements javax.persistence.spi.PersistenceUnitInfo {
    private String persistenceUnitName;
    private String persistenceProviderClassName = "org.hibernate.jpa.HibernatePersistenceProvider";
    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
    private DataSource jtaDataSource;
    private DataSource nonJtaDataSource;
    private List<String> mappingFileNames = new ArrayList<>();
    private List<URL> jarFileUrls = new ArrayList<>();
    private URL persistenceUnitRootUrl;
    private List<String> managedClassNames = new ArrayList<>();
    private boolean excludeUnlistedClasses = false;
    private SharedCacheMode sharedCacheMode = SharedCacheMode.ENABLE_SELECTIVE;
    private ValidationMode validationMode = ValidationMode.AUTO;
    private Properties properties = new Properties();
    private String persistenceXMLSchemaVersion = "2.1";
    private ClassLoader classLoader = HibernatePersistenceUnitInfo.class.getClassLoader();
    private List<ClassTransformer> classTransformers = new ArrayList<>();
    private ClassLoader newTempClassLoader = HibernatePersistenceUnitInfo.class.getClassLoader();


    public HibernatePersistenceUnitInfo(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName + "_" + UUID.randomUUID().toString();
    }

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    @Override
    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    @Override
    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    @Override
    public ValidationMode getValidationMode() {
        return validationMode;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return persistenceXMLSchemaVersion;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        classTransformers.add(transformer);
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return newTempClassLoader;
    }

    public void setNewTempClassLoader(ClassLoader newTempClassLoader) {
        this.newTempClassLoader = newTempClassLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    public void setManagedClassNames(List<String> managedClassNames) {
        this.managedClassNames = managedClassNames;
    }

    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    public void setJarFileUrls(List<URL> jarFileUrls) {
        this.jarFileUrls = jarFileUrls;
    }

    public void setMappingFileNames(List<String> mappingFileNames) {
        this.mappingFileNames = mappingFileNames;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    public void setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    public void setTransactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public List<ClassTransformer> getClassTransformers() {
        return classTransformers;
    }

    public void setClassTransformers(List<ClassTransformer> classTransformers) {
        this.classTransformers = classTransformers;
    }
}
