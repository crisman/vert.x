/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vertx.java.platform.impl.management;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.vertx.java.core.impl.management.VertxManagementException;
import org.vertx.java.platform.impl.DefaultPlatformManager;

/**
 * @author swilliams
 *
 */
public class Management {

  private static final boolean MANAGEMENT_ENABLED = Boolean.getBoolean("vertx.management.jmx");

  private static final String DOMAIN = "org.vertx.platform";

  private static final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

  /**
   * @param platformManager
   */
  public static void registerPlatformManager(DefaultPlatformManager platformManager) {
    if (!MANAGEMENT_ENABLED) return;

    try {
      ObjectName objName = ObjectName.getInstance(DOMAIN, "Name", "Platform");
      if (!platformMBeanServer.isRegistered(objName)) {
        PlatformMXBean platformMXBean = new PlatformMXBeanImpl(platformManager);
        platformMBeanServer.registerMBean(platformMXBean, objName);
      }
    } catch (MalformedObjectNameException | InstanceAlreadyExistsException
        | MBeanRegistrationException | NotCompliantMBeanException e) {
      throw new VertxManagementException(e);
    }
  }

  /**
   * @param deploymentID
   * @param theMain
   * @param modName
   * @param worker
   * @param multiThreaded
   * @param instances
   */
  public static void registerVerticle(String deploymentID, String theMain, String modName, boolean worker, boolean multiThreaded, int instances) {
    if (!MANAGEMENT_ENABLED) return;

    try {
      Hashtable<String, String> table = new Hashtable<>();
      table.put("type", "Verticle");
      table.put("deploymentID", deploymentID);

      ObjectName objName = ObjectName.getInstance(DOMAIN, table);
      if (!platformMBeanServer.isRegistered(objName)) {
        VerticleMXBean verticleMXBean = new VerticleMXBeanImpl(deploymentID, theMain, modName, worker, multiThreaded, instances);
        platformMBeanServer.registerMBean(verticleMXBean, objName);
      }
    } catch (MalformedObjectNameException | InstanceAlreadyExistsException
        | MBeanRegistrationException | NotCompliantMBeanException e) {
      throw new VertxManagementException(e);
    }
  }


  /**
   * @param deploymentID
   */
  public static void unregisterVerticle(String deploymentID) {

    if (!MANAGEMENT_ENABLED) return;

    try {
      Hashtable<String, String> table = new Hashtable<>();
      table.put("type", "Verticle");
      table.put("address", deploymentID);

      ObjectName objName = ObjectName.getInstance(DOMAIN, table);
      if (platformMBeanServer.isRegistered(objName)) {
        platformMBeanServer.unregisterMBean(objName);
      }
    } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException e) {
      throw new VertxManagementException(e);
    }
  }
}
