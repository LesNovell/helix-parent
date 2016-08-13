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

package com.helix.feature.health;

import java.util.concurrent.atomic.AtomicBoolean;

//Use an enum type to enforce a singleton property. Effective Java: Item 3.
public enum Status {
    INSTANCE;

    public static final int FORCED_DOWN = 599;
    public static final int SUCCESS = 200;

    private final AtomicBoolean onLine = new AtomicBoolean(true);

    public void setOnLine() {
        onLine.set(true);
    }

    public void setOffLine() {
        onLine.set(false);
    }

    public boolean isOnline() {
        return onLine.get();
    }
}
