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

package io.helixservice.core.feature;

import io.helixservice.core.component.Component;
import io.helixservice.core.component.ComponentRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Instantiates and resolves dependencies for a feature.
 * Dependencies are resolved by referencing components registered in the ComponentRegistry.
 */
public class FeatureBuilder {
    private final ComponentRegistry parentRegistry;

    public FeatureBuilder(ComponentRegistry parentRegistry) {
        this.parentRegistry = parentRegistry;
    }

    public Feature buildAndRegisterFeature(Class<?> featureClass) {
        return buildAndRegisterFeatures(Collections.singletonList(featureClass)).get(0);
    }

    public List<Feature> buildAndRegisterFeatures(List<Class<?>> featureClasses) {
        return featureClasses.stream()
                .map((featureClass) -> firstAndOnlyConstructor(featureClass.getDeclaredConstructors()))
                .map(this::instantiateFeature)
                .map(this::registerFeatureAllComponents)
                .collect(Collectors.toList());
    }

    private Feature registerFeatureAllComponents(Feature feature) {
        parentRegistry.registerAllFrom(feature);
        return feature;
    }

    private Constructor<Feature> firstAndOnlyConstructor(Constructor<?>[] constructor) {
        if (constructor.length != 1) {
            throw new FeatureBuilderException("Error creating featureClass=" + constructor[0].getDeclaringClass().getName() + ". "
                    + "Features must have only one constructor.  The constructor should accept dependencies as parameters.");
        }

        //noinspection unchecked
        return (Constructor<Feature>) constructor[0];
    }

    private Feature instantiateFeature(Constructor<Feature> constructor) {
        try {
            return constructor.newInstance(resolveComponentDependencies(constructor.getParameterTypes()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new FeatureBuilderException("Error creating featureClass=" + constructor.getDeclaringClass().getName() + ".", e);
        }
    }

    private Object[] resolveComponentDependencies(Class<?>[] parameterTypes) {

        return Arrays.asList(parameterTypes).stream()
                .map(this::findComponentByType)
                .collect(Collectors.toList())
                .toArray(new Object[0]);
    }

    private Component findComponentByType(Class<?> type) {
        Component result = null;

        for (Component component : parentRegistry.getComponentMap().values()) {
            if (component.getClass().equals(type)) {
                result = component;
            }
        }

        if (result == null) {
            for (Component component : parentRegistry.getComponentMap().values()) {
                if (type.isAssignableFrom(component.getClass())) {
                    result = component;
                }
            }
        }

        return result;
    }

}
