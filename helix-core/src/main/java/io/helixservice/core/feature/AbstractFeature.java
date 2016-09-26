
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

package io.helixservice.core.feature;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import io.helixservice.core.component.Component;
import io.helixservice.core.component.ComponentRegistry;
import io.helixservice.core.container.Container;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class from which all features should derive,
 * as it provides common functionality and default implementations.
 */
public abstract class AbstractFeature implements Feature {
    private Multimap<String, Component> componentsMap = ArrayListMultimap.create();
    private String featureName;

    /**
     * Construct the feature, setting the feature name based on the Java class name.
     */
    public AbstractFeature() {
        this.featureName = getClass().getSimpleName();
        register(this); // Register this feature component, enables finding features dependency resolution
    }

    /**
     * Construct this feature, with the provided feature name
     *
     * @param featureName Feature name
     */
    public AbstractFeature(String featureName) {
        this.featureName = featureName;
    }

    /**
     * Register component(s) created and owned by this feature
     * <p>
     * Components registered here are in a registry local to this feature.
     * These components will also be automatically aggregated
     * and registered with the HelixServer top-level registry.
     * <p>
     * If a component contains other components, then the entire
     * tree of components will be registered.
     *
     * @param components Components to register
     */
    public Feature register(Component... components) {
        for (Component component : components) {
            componentsMap.put(component.getComponentType(), component);
            register(component.getContainedComponents());
        }

        return this;
    }

    @Override
    public ComponentRegistry registerAllFrom(ComponentRegistry registry) {
        componentsMap.putAll(registry.getComponentMap());
        return this;
    }

    /**
     * Get this feature's name for debugging and logging
     *
     * @return The feature's name
     */
    @Override
    public String getFeatureName() {
        return featureName;
    }

    /**
     * Returns a map of registered components owned by this feature
     *
     * @return The map of components, where key is the component type name.
     */
    public Multimap<String, Component> getComponentMap() {
        return ArrayListMultimap.create(componentsMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Component> findAllComponents() {
        return componentsMap.values();
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
        return Iterables.getLast((Collection<T>) componentsMap.get(componentType), defaultValue);
    }

    /**
     * Logs the feature configuration to the provided logger.
     * <p>
     * This implements a generic way of logging features, so
     * subclasses should not need to implement their own logging code.
     *
     * @param logger Logger to write the feature details to
     */
    @Override
    public void logFeatureDetails(Logger logger) {
        logger.info("Feature " + getFeatureName() + " components:");
        logFactories(logger);
        logComponents(logger);
    }

    private void logComponents(Logger logger) {
        List<String> componentDescriptions = new ArrayList<>();

        for (String registrableTypeName : componentsMap.keySet()) {
            for (Component component : componentsMap.get(registrableTypeName)) {
                String componentDescription = component.getComponentDescription();
                if (componentDescription != null && !componentDescription.isEmpty()) {
                    componentDescriptions.add("   " + String.format("%1$-15s", registrableTypeName) + " " + componentDescription);
                }
            }
        }

        Collections.sort(componentDescriptions);
        componentDescriptions.forEach(logger::info);
    }

    private void logFactories(Logger logger) {
        Method[] declaredMethods = getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!"void".equals(method.getGenericReturnType().getTypeName()) && (method.getModifiers() & Modifier.PUBLIC) != 0) {
                logger.info("   {}  {}", String.format("%1$-15s", "FactoryMethod"), method.getName() + "() => " + method.getGenericReturnType().getTypeName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Container container) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish(Container container) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(Container container) {

    }
}
