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

package com.helix.core.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.helix.core.component.Component;
import com.helix.core.feature.Feature;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Helix Server
 * <p>
 * The Helix Server controls the lifecycle of the features
 * and components which comprise the service.
 */
public class HelixServer implements Server {
    private static final Logger LOG = LoggerFactory.getLogger(HelixServer.class);
    private final Supplier<Feature[]> bootstrapFeaturesSupplier;
    private final Supplier<Feature[]> coreFeaturesSupplier;

    private Multimap<String, Component> registrationMap = ArrayListMultimap.create();

    private ServerState serverState = ServerState.STOPPED;

    private boolean bootstrapFeaturesStarted = false;
    private List<Feature> bootstrapFeatures;
    private List<Feature> coreFeatures;
    private List<Feature> features;

    private Vertx vertx;

    /**
     * Create a Helix Server
     * <p>
     * The server expects a lambda or class that can will instantiate features to be installed.
     *
     * @param bootstrapFeaturesSupplier Bootstrap features to be started before others. Primarily, these are configuration related features.
     * @param coreFeaturesSupplier Core features that will be started after the bootstrap features successfully start.
     */
    public HelixServer(Supplier<Feature[]> bootstrapFeaturesSupplier, Supplier<Feature[]> coreFeaturesSupplier) {
        this.bootstrapFeaturesSupplier = bootstrapFeaturesSupplier;
        this.coreFeaturesSupplier = coreFeaturesSupplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Vertx> getVertx() {
        return Optional.ofNullable(vertx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerState getServerState() {
        return serverState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread start() {
        if (serverState != ServerState.STOPPED) {
            throw new IllegalStateException("Server state must be STOPPED in order to start the server");
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                serverState = ServerState.STARTING;
                LOG.info("Starting Helix Server");

                HelixServer.this.instantiateFeatures();

                for (Feature feature : features) {
                    feature.logFeatureDetails(LOG);

                    Multimap<String, Component> registrationMap = feature.getRegistrationMap();
                    HelixServer.this.registrationMap.putAll(registrationMap);
                }

                // Start bootstrap features
                if (!bootstrapFeaturesStarted) {
                    for (Feature bootstrapFeature : bootstrapFeatures) {
                        bootstrapFeature.start(HelixServer.this);
                    }
                    bootstrapFeaturesStarted = true;
                }

                // Initialize Vertx
                String numWorkers = System.getProperty("vertx.server.workers", "50");
                VertxOptions options = new VertxOptions().setWorkerPoolSize(Integer.parseInt(numWorkers));
                vertx = Vertx.vertx(options);

                // Start core features
                for (Feature coreFeature : coreFeatures) {
                    coreFeature.start(HelixServer.this);
                }

                serverState = ServerState.STARTED;
                LOG.info("Helix Server started in " + (System.currentTimeMillis() - start) + " ms");
            }
        });
        thread.start();

        return thread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread restart() {
        if (serverState != ServerState.STARTED) {
            throw new IllegalStateException("Server state must be STARTED in order to restart the server");
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Restarting Helix Server");

                try {
                    HelixServer.this.stop(false).join();
                    HelixServer.this.start().join();
                } catch (InterruptedException e) {
                    LOG.error("Restart HelixServer interrupted ", e);
                }
            }
        });
        thread.start();

        return thread;
    }

    private void instantiateFeatures() {
        features = new ArrayList<>();

        if (this.bootstrapFeatures == null) {
            this.bootstrapFeatures = Arrays.asList(bootstrapFeaturesSupplier.get());
            features.addAll(this.bootstrapFeatures);
        }

        this.coreFeatures = Arrays.asList(coreFeaturesSupplier.get());
        features.addAll(this.coreFeatures);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread stop(boolean immediate) {
        if (serverState != ServerState.STARTED) {
            throw new IllegalStateException("Server state must be STARTED in order to stop the server");
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Feature> reverseStartOrder = new ArrayList<>(features);
                Collections.reverse(reverseStartOrder);

                serverState = ServerState.FINISHING;
                LOG.info("Finishing Helix Server");

                for (Feature feature : reverseStartOrder) {
                    feature.finish(HelixServer.this);
                }

                if (!immediate) {
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                LOG.info("Stopping Helix Server");
                serverState = ServerState.STOPPING;
                vertx.close((r) -> {
                    for (Feature feature : reverseStartOrder) {
                        feature.stop(HelixServer.this);
                    }

                    registrationMap.clear();
                    vertx = null;
                    serverState = ServerState.STOPPED;
                    features = null;
                    bootstrapFeatures = null;
                    coreFeatures = null;

                    LOG.info("Stopped Helix Server");
                });
            }
        });
        thread.start();

        return thread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multimap<String, Component> getRegistrationMap() {
        return ArrayListMultimap.create(registrationMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Component> Collection<T> findByType(String componentType) {
        @SuppressWarnings("unchecked")
        Collection<T> result = (Collection<T>) registrationMap.get(componentType);

        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Component> T findByType(String componentType, T defaultValue) {
        //noinspection unchecked
        return Iterables.getFirst((Collection<T>) registrationMap.get(componentType), defaultValue);
    }
}
