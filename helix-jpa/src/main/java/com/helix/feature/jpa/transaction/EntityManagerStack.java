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

package com.helix.feature.jpa.transaction;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Tracks the current entity manager for a thread.
 * A stack is used to support nested @Transactional method calls with different Persistence Units.
 */
public class EntityManagerStack {
    // Persistence Unit Name -> Thread Local Stack of Entity Managers
    private static Map<String, ThreadLocal<Stack<EntityManager>>> PU2STACK = new HashMap<>();

    public static void push(String persistenceUnitName, EntityManager entityManager) {
        getEntityManagersStack(persistenceUnitName).push(entityManager);
    }

    public static EntityManager pop(String persistenceUnitName) {
        return getEntityManagersStack(persistenceUnitName).pop();
    }

    public static boolean isEmpty(String persistenceUnitName) {
        return getEntityManagersStack(persistenceUnitName).isEmpty();
    }

    public static EntityManager peek(String persistenceUnitName) {
        return getEntityManagersStack(persistenceUnitName).peek();
    }

    private static Stack<EntityManager> getEntityManagersStack(String persistenceUnitName) {
        ThreadLocal<Stack<EntityManager>> threadLocalStack = PU2STACK.get(persistenceUnitName);

        if (threadLocalStack == null || threadLocalStack.get() == null) {
            threadLocalStack = new ThreadLocal<>();
            threadLocalStack.set(new Stack<>());
            PU2STACK.put(persistenceUnitName, threadLocalStack);
        }

        return threadLocalStack.get();
    }
}
