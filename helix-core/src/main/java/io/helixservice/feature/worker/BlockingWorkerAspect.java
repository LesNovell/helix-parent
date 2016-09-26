
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

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.helixservice.core.container.Container;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.vertx.ext.sync.Sync.awaitEvent;

/**
 * AspectJ aspect for implementing Blocking Workers feature
 */
@Aspect
@DeclarePrecedence(value = "io.helixservice.feature.worker.BlockingWorkerAspect,*")
public class BlockingWorkerAspect {
    private static Logger LOG = LoggerFactory.getLogger(BlockingWorkerAspect.class);

    private static final String VERT_X_WORKER_THREAD = "vert.x-worker-thread";

    private Vertx vertx;

    public BlockingWorkerAspect() {
    }

    /**
     * Capture Helix server reference on BlockingWorkerFeature creation
     *
     * @param container Helix server
     */
    @Before(value = "execution(public void io.helixservice.feature.worker.BlockingWorkerFeature.start(io.helixservice.core.container.Container)) "
            + "&& args(container)")
    public void beforeStartupFeature(Container container) { this.vertx = container.getVertx().get(); }

    /**
     * Adds code around all methods annotated with @BlockingWorker.
     * If the current thread is not a already worker thread, then
     * the method body is run on a worker thread.
     */
    @Suspendable
    @Around(value = "(execution(public * *(..)) && @annotation(blockingWorker)) || "
            + "(execution(public * *(..)) && within(@io.helixservice.feature.worker.BlockingWorker *) && @annotation(blockingWorker))")
    public Object around(ProceedingJoinPoint pjp, BlockingWorker blockingWorker) throws Throwable, SuspendExecution {
        Object result;

        if (onWorkerThread()) {
            result = pjp.proceed();
        } else {
            result = invokeOnWorkerThread(pjp);
        }

        return result;
    }


    public static boolean onWorkerThread() {
        return Thread.currentThread().getName().contains(VERT_X_WORKER_THREAD);
    }

    @Suspendable
    private Object invokeOnWorkerThread(ProceedingJoinPoint pjp) throws Throwable, SuspendExecution {
        AsyncResult<Object> ret =
                awaitEvent(new Consumer<Handler<AsyncResult<Object>>>() {
                    @Override
                    @Suspendable
                    public void accept(Handler<AsyncResult<Object>> awaitHandler) {
                        // Vert.x executeBlocking will run our handler on a Worker Thread
                        vertx.executeBlocking(new Handler<Future<Object>>() {
                            @Override
                            @Suspendable
                            public void handle(Future<Object> future) {
                                try {
                                    assertRunningOnVertxWorkerThread(pjp);

                                    if (LOG.isInfoEnabled()) {
                                        LOG.info("Started Blocking Worker on " + Thread.currentThread().getName());
                                    }

                                    // Run annotated method code
                                    Object result = pjp.proceed();

                                    // Return result as a completable future
                                    future.complete(result);
                                } catch (Throwable t) {
                                    // If there was an exception, pass it back on the completable future
                                    future.fail(t);
                                } finally {
                                    if (LOG.isInfoEnabled()) {
                                        LOG.info("Completed Blocking Worker on " + Thread.currentThread().getName());
                                    }
                                }
                            }
                        }, false, awaitHandler);
                    }
                });

        if (ret.failed()) {
            throw ret.cause();
        }

        return ret.result();
    }

    private void assertRunningOnVertxWorkerThread(ProceedingJoinPoint pjp) {
        if (!onWorkerThread()) {
            /**
             * This should never happen, it's a sanity check
             */
            Signature signature = pjp.getSignature();
            throw new IllegalStateException("Expected " + signature.getDeclaringTypeName()
                    + "::" + signature.getName() + " to run on a Vert.x worker thread");
        }
    }
}
