
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

package io.helixservice.feature.worker;

import io.helixservice.core.feature.AbstractFeature;
import io.helixservice.core.container.Container;
import io.vertx.core.Vertx;

/**
 * Provides a easy way to make any method execute as blocking code.
 * Annotating a method with @BlockingWorker makes the method run on a Vert.x
 * Blocking Worker thread.
 *
 * While the Blocking Worker thread is running, the Event Loop
 * thread is released to process other events.
 *
 * <b>NOTE: Controller endpoint methods cannot be annotated as blocking methods</b>
 * If you need to execute blocking code from your controller, delegate to a
 * business logic method (preferably in another class).
 *
 * <h2>Request Context Propagation</h2>
 * The RequestContext will be propagated to the Blocking Worker thread, this ensures that
 * any security, transaction, and logging related data are copied automatically.
 *
 * <h2>Nested BlockingWorkers</h2>
 * It's okay to have one method annotated with @BlockingWorker call another method
 * that is also annotated with @BlockingWorker.  If the thread is already a
 * Blocking Worker thread, then there is no attempt to execute the method on a
 * different thread. Only one Blocking Worker thread will be used.
 *
 * <h2>AspectJ Configuration</h2>
 * <ul><li>AspectJ agent must be installed on the Java command line, for example: <pre>-javaagent:aspectjweaver-1.8.8.jar</pre></li>
 * <li>AspectJ aop.xml must contain <pre>&lt;aspect name="io.helixservice.feature.worker.BlockingWorkerAspect"\&gt;</pre></li></ul>
 *
 * <h2>Worker Thread Pool Configuration</h2>
 * By default, 50 threads are created to support concurrent blocking operations.  The thread pool size can
 * be tuned by modifying the configuration property <b>vertx.server.workers</b>
 *
 * @see <a href="http://vertx.io/docs/vertx-core/java/#blocking_code">Vert.x Blocking Worker</a>
 */
public class BlockingWorkerFeature extends AbstractFeature {
    private Vertx vertx;

    /**
     * Create the blocking worker feature
     */
    public BlockingWorkerFeature() {
    }

    /**
     * Call this method to test if your code is executing
     * on a blocking worker thread.  This is more of a
     * debugging aid, as the thread will be checked before
     * the method executes to ensure the method code is
     * executing on a Vert.x Worker Thread.
     *
     * @return True if the executing code is running on a worker thread
     */
    public static boolean isOnWorkerThread() {
        return BlockingWorkerAspect.onWorkerThread();
    }

    @Override
    public void start(Container container) {
        vertx = container.getVertx().get();
    }

    public Vertx getVertx() {
        return vertx;
    }
}
