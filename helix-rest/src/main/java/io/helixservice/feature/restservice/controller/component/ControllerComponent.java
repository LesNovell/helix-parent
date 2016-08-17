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

package io.helixservice.feature.restservice.controller.component;

import io.helixservice.core.component.Component;
import io.helixservice.feature.restservice.controller.annotation.Controller;
import io.helixservice.feature.restservice.controller.annotation.Endpoint;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller Configuration Component
 * <p>
 * Each REST Controller should have a ControllerComponent
 * that is registered with a feature.
 * <p>
 * Each registered ControllerComponent is a Helix component,
 * and it contains a set of EndpointComponents.
 *
 * Using the ComponentRegistry it is possible to enumerate
 * all the controllers and their associated endpoints.
 */
public class ControllerComponent implements Component {
    public static final String TYPE_NAME = "Controller";

    private Object controller;
    private List<EndpointComponent> endpointComponentList;

    /**
     * Create a Controller Component
     *
     * @param controller Controller object
     * @param endpointComponentList List of EndpointComponents in this Controller
     */
    public ControllerComponent(Object controller, List<EndpointComponent> endpointComponentList) {
        this.controller = controller;
        this.endpointComponentList = endpointComponentList;
    }

    /**
     * Build a ControllerComponent and its contained EndpointComponents,
     * by reading the annotations on a Controller object.
     *
     * @param controller The controller object
     * @return ControllerComponent that was created
     * @throws IllegalArgumentException if the controller is missing @Controller annotation
     */
    public static ControllerComponent fromAnnotationsOn(Object controller)  {
        List<EndpointComponent> endpointComponentList = new ArrayList<>();

        Controller controllerAnnotation = controller.getClass().getAnnotation(Controller.class);
        if (controllerAnnotation != null) {
            Method[] methods = controller.getClass().getMethods();

            for (Method method : methods) {
                Endpoint annotation = method.getAnnotation(Endpoint.class);
                if (annotation != null) {
                    endpointComponentList.add(EndpointComponent.forPath(annotation.value(), annotation.methods(), method, controller));
                }
            }
        } else {
            throw new IllegalArgumentException("Controller className=" + controller + " should have @Controller annotation present");
        }

        return new ControllerComponent(controller, endpointComponentList);
    }

    public Object getController() {
        return controller;
    }

    public List<EndpointComponent> getEndpointComponentList() {
        return endpointComponentList;
    }

    @Override
    public String getComponentDescription() {
        return null;
    }

    @Override
    public Component[] getContainedComponents() {
        return endpointComponentList.toArray(new Component[endpointComponentList.size()]);
    }

    @Override
    public String getComponentType() {
        return TYPE_NAME;
    }
}
