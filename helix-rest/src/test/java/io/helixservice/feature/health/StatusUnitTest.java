
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

package io.helixservice.feature.health;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StatusUnitTest {

    @Before
    public void setUp() {
        Status.INSTANCE.setOnLine();
    }

    @Test
    public void shouldReflectOnlineOfflineStatusWhenToggled() {
        assertThat(Status.INSTANCE.isOnline(), is(true));

        Status.INSTANCE.setOffLine();

        assertThat(Status.INSTANCE.isOnline(), is(false));

        Status.INSTANCE.setOnLine();

        assertThat(Status.INSTANCE.isOnline(), is(true));
    }
}
