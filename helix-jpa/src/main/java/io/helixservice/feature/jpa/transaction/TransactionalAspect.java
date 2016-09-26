
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

import co.paralleluniverse.fibers.SuspendExecution;
import io.helixservice.feature.jpa.JpaHibernateFeature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.EmptyStackException;

/**
 * Aspects necessary to support @Transactional annotation
 */
@Aspect
public class TransactionalAspect {
    private static Logger LOG = LoggerFactory.getLogger(TransactionalAspect.class);

    public TransactionalAspect() {
    }

    /*
     * Instruments all public methods with @Transactional annotation
     */
    @SuppressWarnings("DuplicateThrows")
    @Around(value = "(execution(public * *(..)) && @annotation(transactional))"
            + " || (execution(public * *(..)) && within(@javax.transaction.Transactional *) && @annotation(transactional))")
    public Object around(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable, SuspendExecution {
        Object result;

        Signature method = pjp.getSignature();
        Object target = pjp.getTarget();

        LOG.info("Managing transaction for method=" + method.getName() +
                ", target=" + target.getClass().getName() + ", txType=" + transactional.value());

        validateTransactionalAnnotation(target, method, transactional);

        boolean commit = false;
        String persistenceContextName = beginTransaction(target);
        try {
            result = pjp.proceed();
            commit = true;
        } catch (Throwable t) {
            commit = shouldCommitOnThrowable(transactional, t);
            LOG.info("Caught throwable=" + t.getClass().getName() + ", which results in commit=" + commit);
            throw t;
        } finally {
            LOG.trace("Skipping non-transactional method=" + method.getName() + ", target=" + target.getClass().getName());
            endTransaction(persistenceContextName, commit);
        }

        return result;
    }

    private void validateTransactionalAnnotation(Object target, Signature method, Transactional transactional) {
        if (transactional.value() != Transactional.TxType.REQUIRED) {
            String msg = "Transaction type not supported: txType=" + transactional.value() + ", method=" + method.getName() + ", target=" + target
                    .getClass().getName();
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean shouldCommitOnThrowable(Transactional transactional, Throwable t) {
        Class[] dontRollbackClasses = transactional.dontRollbackOn();
        for (Class dontRollbackClass : dontRollbackClasses) {
            if (dontRollbackClass.isAssignableFrom(t.getClass())) {
                return true;
            }
        }

        Class[] rollbackClasses = transactional.rollbackOn();
        for (Class rollbackClass : rollbackClasses) {
            if (rollbackClass.isAssignableFrom(t.getClass())) {
                return false;
            }
        }

        return (RuntimeException.class.isAssignableFrom(t.getClass()));
    }

    private String beginTransaction(Object target) throws Throwable {
        String persistenceUnitName;

        try {
            Field entityManagerField = findEntityManagerField(target);
            persistenceUnitName = findPersistenceUnitName(target, entityManagerField);

            LOG.info("Beginning transaction on " + persistenceUnitName);
            EntityManager entityManager = createEntityManager(persistenceUnitName);
            EntityManagerStack.push(persistenceUnitName, entityManager);
            entityManagerField.set(target, new EntityManagerDelegate(persistenceUnitName));
        } catch (Throwable t) {
            String message = "Unable to begin transaction on entity manager in targetClass=" + target.getClass().getName();
            LOG.error(message, t);
            throw new IllegalStateException(message, t);
        }

        return persistenceUnitName;
    }

    private EntityManager createEntityManager(String persistenceUnitName) {
        EntityManager entityManager;

        if (EntityManagerStack.isEmpty(persistenceUnitName)) {
            EntityManagerFactory entityManagerFactory = JpaHibernateFeature.entityManagerFactoryByPersistenceUnitName(persistenceUnitName);
            entityManager = entityManagerFactory.createEntityManager();
            LOG.info("Created entityManager=" + emIdentifier(entityManager) + " on threadName=" + Thread.currentThread().getName());

            entityManager.getTransaction().begin();
            LOG.info("Started transaction on entityManager=" + emIdentifier(entityManager));
        } else {
            entityManager = EntityManagerStack.peek(persistenceUnitName);
        }

        return entityManager;
    }

    private Field findEntityManagerField(Object target) throws IllegalAccessException {
        Field result = null;

        Class<?> targetClass = target.getClass();
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.getType().equals(EntityManager.class)) {
                field.setAccessible(true);
                result = field;
            }
        }

        if (result == null) {
            String message = "EntityManager field missing on targetClass=" + targetClass.getName();
            LOG.error(message);
            throw new IllegalStateException(message);
        }

        return result;
    }

    private String findPersistenceUnitName(Object target, Field entityManagerField) {
        String persistenceUnitName = JpaHibernateFeature.DEFAULT_PERSISTENCE_UNIT_NAME;

        PersistenceContext persistenceContext = entityManagerField.getDeclaredAnnotation(PersistenceContext.class);
        if (persistenceContext == null) {
            persistenceContext = target.getClass().getAnnotation(PersistenceContext.class);
        }

        if (persistenceContext != null && persistenceContext.name() != null) {
            persistenceUnitName = persistenceContext.unitName();
        }

        return persistenceUnitName;
    }

    private void endTransaction(String persistenceUnitName, boolean commit) {

        LOG.info("Ending transaction on " + persistenceUnitName);

        EntityManager entityManager;
        try {
            entityManager = EntityManagerStack.pop(persistenceUnitName);
        } catch (EmptyStackException e) {
            throw new IllegalStateException("endTransaction() failed => EntityManagerStack is empty", e);
        }

        if (EntityManagerStack.isEmpty(persistenceUnitName)) {
            try {
                if (commit && !entityManager.getTransaction().getRollbackOnly()) {
                    entityManager.getTransaction().commit();
                    LOG.info("Transaction committed on entityManager=" + emIdentifier(entityManager));
                } else {
                    entityManager.getTransaction().rollback();
                    LOG.info("Transaction rolled back on entityManager=" + emIdentifier(entityManager));
                }
            } finally {
                safelyCloseEntityManager(entityManager);
            }
        } else {
            if (!commit) {
                LOG.info("Transaction marked for rollback only on entityManager=" + emIdentifier(entityManager));
                entityManager.getTransaction().setRollbackOnly();
            }
        }
    }

    private void safelyCloseEntityManager(EntityManager entityManager) {
        try {
            LOG.trace("Closing entityManager=" + emIdentifier(entityManager));
            try {
                EntityTransaction transaction = entityManager.getTransaction();
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
            } finally {
                entityManager.close();
            }
        } catch (Throwable unexpected) {
            // Unexpected
            LOG.error("Unexpected exception closing the entity manager", unexpected);
        }
    }

    private String emIdentifier(EntityManager entityManager) {
        return Integer.toHexString(entityManager.hashCode());
    }
}
