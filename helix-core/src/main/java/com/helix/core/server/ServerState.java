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

/**
 * Helix Server State
 */
public enum ServerState {
   /**
    * Server is stopped
    */
   STOPPED,

   /**
    * Server is starting, features and components are being created
    */
   STARTING,

   /**
    * Server has started, all features and components created.
    */
   STARTED,

   /**
    * Server is finishing in-flight requests. New requests served HTTP 599.
    * This state lasts a configurable amount of time before continuing.
    */
   FINISHING,

   /**
    * Server is stopping, resources are being freed. Feature and component
    * registries will be cleared. Vert.x is stopped.
    */
   STOPPING
}
