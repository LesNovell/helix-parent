
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

package io.helixservice.core.container;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import io.helixservice.core.component.Component;
import io.helixservice.core.component.ComponentRegistry;
import io.helixservice.core.feature.Feature;
import io.helixservice.core.feature.FeatureBuilder;
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
import java.util.stream.Collectors;

/**
 * Helix Container Class
 * <p>
 * The Helix Container controls the lifecycle of the features
 * and components which comprise the service.
 */
public class Helix implements Container {
    private static final Logger LOG = LoggerFactory.getLogger(Helix.class);
    private static final long HELIX_START_TIMEOUT_MILLIS = 60 * 2 * 1000;

    // Container Related
    private Multimap<String, Component> componentsMap = ArrayListMultimap.create();
    private ContainerState containerState = ContainerState.STOPPED;

    // Features Installed
    private List<Class<?>> featureClasses;
    private List<Feature> features;
    private boolean bootstrapFeaturesStarted = false;

    private Vertx vertx;


    private Helix(List<Class<?>> featureClasses) {
        this.featureClasses = featureClasses;
    }

    /**
     * Create and Start the Helix Container
     *
     * @param featureClasses List of feature classes to instantiate and start
     * @throws InterruptedException If the start operation times out or is unexpectedly canceled
     */
    public static Helix start(Class<?>... featureClasses) throws InterruptedException {
        return start(true, HELIX_START_TIMEOUT_MILLIS, featureClasses);
    }

    /**
     * Create and Optionally Start the Helix Container
     *
     * @param startImmediately If true, the container is started. This method blocks until the container has completed startup.
     * @param timeoutInMillis  Timeout waiting for the container to be started.
     * @param featureClasses   Array of feature classes to install.
     * @throws InterruptedException If the start operation times out or is unexpectedly canceled
     */
    public static Helix start(boolean startImmediately, long timeoutInMillis,
            Class<?>... featureClasses) throws InterruptedException {

        Helix helix = new Helix(Arrays.asList(featureClasses));
        if (startImmediately) {
            helix.start().join(timeoutInMillis);
        }
        return helix;
    }

    private List<Feature> getCoreFeatures() {
        return features.stream().filter(f -> !f.shouldStartDuringBootstrapPhase()).collect(Collectors.toList());
    }

    private List<Feature> getBootstrapFeatures() {
        return features.stream().filter(Feature::shouldStartDuringBootstrapPhase).collect(Collectors.toList());
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
    public ContainerState getContainerState() {
        return containerState;
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
    public synchronized Thread start() {
        if (containerState != ContainerState.STOPPED) {
            throw new IllegalStateException("State must be STOPPED in order to start the container");
        }

        Thread thread = new Thread(() -> {
            long start = System.currentTimeMillis();
            containerState = ContainerState.STARTING;
            LOG.info("Starting Helix");

            LOG.info("Building Features");
            FeatureBuilder featureBuilder = new FeatureBuilder(this);
            features = featureBuilder.buildAndRegisterFeatures(featureClasses);

            startFeatures(bootstrapFeaturesStarted ? Collections.emptyList() : getBootstrapFeatures());
            bootstrapFeaturesStarted = true;

            // Initialize Vertx
            String numWorkers = System.getProperty("vertx.server.workers", "50");
            VertxOptions options = new VertxOptions().setWorkerPoolSize(Integer.parseInt(numWorkers));
            vertx = Vertx.vertx(options);

            startFeatures(getCoreFeatures());

            containerState = ContainerState.STARTED;
            LOG.info("Helix started in " + (System.currentTimeMillis() - start) + " ms");
        });
        thread.start();

        return thread;
    }

    private void startFeatures(List<Feature> features) {
        features.stream().forEach(feature ->
        {
            feature.start(Helix.this);
            feature.logFeatureDetails(LOG);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Thread restart() {
        if (containerState != ContainerState.STARTED) {
            throw new IllegalStateException("State must be STARTED in order to restart the container");
        }

        Thread thread = new Thread(() -> {
            LOG.info("Restarting Helix Container");

            try {
                Helix.this.stop(false).join();
                Helix.this.start().join();
            } catch (InterruptedException e) {
                LOG.error("Restart of Helix was interrupted ", e);
            }
        });
        thread.start();

        return thread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Thread stop(boolean immediate) {
        if (containerState != ContainerState.STARTED) {
            throw new IllegalStateException("State must be STARTED in order to stop the container");
        }

        Thread thread = new Thread(() -> {
            List<Feature> reverseStartOrder = new ArrayList<>(features);
            Collections.reverse(reverseStartOrder);

            containerState = ContainerState.FINISHING;
            LOG.info("Finishing Helix In-Flight Requests");

            for (Feature feature : reverseStartOrder) {
                feature.finish(Helix.this);
            }

            if (!immediate) {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            LOG.info("Stopping Helix");
            containerState = ContainerState.STOPPING;
            vertx.close((r) -> {
                for (Feature feature : reverseStartOrder) {
                    feature.stop(Helix.this);
                }

                componentsMap.clear();
                vertx = null;
                containerState = ContainerState.STOPPED;
                features = null;

                LOG.info("Stopped Helix");
            });
        });
        thread.start();

        return thread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multimap<String, Component> getComponentMap() {
        return ArrayListMultimap.create(componentsMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentRegistry register(Component... components) {
        for (Component component : components) {
            componentsMap.put(component.getComponentType(), component);
            register(component.getContainedComponents());
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentRegistry registerAllFrom(ComponentRegistry registry) {
        componentsMap.putAll(registry.getComponentMap());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Component> findAllComponents() {
        return Collections.unmodifiableCollection(componentsMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Component> Collection<T> findComponentByType(String componentType) {
        @SuppressWarnings("unchecked")
        Collection<T> result = (Collection<T>) componentsMap.get(componentType);

        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Component> T findComponentByType(String componentType, T defaultValue) {
        //noinspection unchecked
        return Iterables.getFirst((Collection<T>) componentsMap.get(componentType), defaultValue);
    }
}
