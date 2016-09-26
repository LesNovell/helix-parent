
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
public class Controller implements Component {
    public static final String TYPE_NAME = "Controller";

    private Object controller;
    private List<Endpoint> endpointList;

    /**
     * Create a Controller Component
     *
     * @param controller Controller object
     * @param endpointList List of EndpointComponents in this Controller
     */
    public Controller(Object controller, List<Endpoint> endpointList) {
        this.controller = controller;
        this.endpointList = endpointList;
    }

    /**
     * Build a ControllerComponent and its contained EndpointComponents,
     * by reading the annotations on a Controller object.
     *
     * @param controller The controller object
     * @return ControllerComponent that was created
     * @throws IllegalArgumentException if the controller is missing @Controller annotation
     */
    public static Controller fromAnnotationsOn(Object controller)  {
        List<Endpoint> endpointList = new ArrayList<>();

        io.helixservice.feature.restservice.controller.annotation.Controller
                controllerAnnotation = controller.getClass().getAnnotation(io.helixservice.feature.restservice.controller.annotation.Controller.class);
        if (controllerAnnotation != null) {
            Method[] methods = controller.getClass().getMethods();

            for (Method method : methods) {
                io.helixservice.feature.restservice.controller.annotation.Endpoint
                        annotation = method.getAnnotation(io.helixservice.feature.restservice.controller.annotation.Endpoint.class);
                if (annotation != null) {
                    endpointList.add(Endpoint.forPath(annotation.value(), annotation.methods(), method, controller));
                }
            }
        } else {
            throw new IllegalArgumentException("Controller className=" + controller + " should have @Controller annotation present");
        }

        return new Controller(controller, endpointList);
    }

    public Object getController() {
        return controller;
    }

    public List<Endpoint> getEndpointList() {
        return endpointList;
    }

    @Override
    public String getComponentDescription() {
        return null;
    }

    @Override
    public Component[] getContainedComponents() {
        return endpointList.toArray(new Component[endpointList.size()]);
    }

    @Override
    public String getComponentType() {
        return TYPE_NAME;
    }
}
