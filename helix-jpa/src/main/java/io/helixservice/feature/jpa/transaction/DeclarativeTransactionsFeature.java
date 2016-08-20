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

package io.helixservice.feature.jpa.transaction;

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.feature.jpa.JpaHibernateFeature;

import javax.persistence.EntityManager;
import java.util.EmptyStackException;

/**
 * Provides support for @Transactional and @PersistenceContext
 * annotations for the JPA implementation
 * <p>
 * The @Transactional annotation starts and ends a transaction by declaration.
 * <ul>
 * <li>Do NOT use @Transactional directly on Controller endpoints (A Vert.x Sync Limitation)</li>
 * <li>Do have your controller delegate to a Service object, and place @Transactional annotations there</li>
 * <li>In addition to @Transactional, you must add @BlockingWorker annotation since JPA commits are transactional</li>
 * <li>@Transactional only supports TX_REQUIRED. Other transactional propagation rules are not yet supported.</li>
 * <li>The class with @Transactional must have a field with the type EntityManager. The EntityManager will be injected.</li>
 * <li>When using multiple Persistence Units, add the @PersistenceContext annotation on the EntityManager field to specify which Persistence Unit should be used</li>
 * </ul>
 * <h2>AspectJ Configuration</h2>
 * <ul>
 * <li>AspectJ agent must be installed on the Java command line, for example: <pre>-javaagent:aspectjweaver-1.8.8.jar</pre></li>
 * <li>AspectJ aop.xml must contain <pre>&lt;aspect name="io.helixservice.feature.jpa.transaction.TransactionalAspect"\&gt;</pre></li>
 * </ul>
 */
public class DeclarativeTransactionsFeature extends AbstractFeature {
    public DeclarativeTransactionsFeature() {
    }

    /**
     * Gets the current EntityManager for the transaction
     *
     * @return The active EntityManager
     * @throws IllegalStateException When an EntityManager is not active
     */
    public static EntityManager currentEntityManager() {
        try {
            return EntityManagerStack.peek(JpaHibernateFeature.DEFAULT_PERSISTENCE_UNIT_NAME);
        } catch (EmptyStackException e) {
            throw new IllegalStateException("EntityManager not present", e);
        }
    }

    /**
     * Gets the current EntityManager for the transaction
     *
     * @param persistenceUnitName Persistence unit name associated with the EntityManager
     * @return The active EntityManager for persistenceUnitName
     * @throws IllegalStateException When an EntityManager is not active for this persistenceUnitName
     */
    public static EntityManager currentEntityManager(String persistenceUnitName) {
        try {
            return EntityManagerStack.peek(persistenceUnitName);
        } catch (EmptyStackException e) {
            throw new IllegalStateException("EntityManager not present", e);
        }
    }
}
