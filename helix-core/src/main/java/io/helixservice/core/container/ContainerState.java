
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

/**
 * Helix Container State
 */
public enum ContainerState {
   /**
    * Container is stopped
    */
   STOPPED,

   /**
    * Container is starting, features and components are being created
    */
   STARTING,

   /**
    * Container has started, all features and components created.
    */
   STARTED,

   /**
    * Container is finishing in-flight requests. New requests served HTTP 599.
    * This state lasts a configurable amount of time before continuing.
    */
   FINISHING,

   /**
    * Container is stopping, resources are being freed. Feature and component
    * registries will be cleared. Vert.x is stopped.
    */
   STOPPING
}
