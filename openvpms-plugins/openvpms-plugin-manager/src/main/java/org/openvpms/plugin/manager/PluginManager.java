/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.manager;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.InputStream;
import java.util.List;

/**
 * OpenVPMS plugin manager.
 *
 * @author Tim Anderson
 */
public interface PluginManager {

    /**
     * Returns the first service implementing the specified interface.
     *
     * @param type the interface
     * @return the first service implementing the interface, or {@code null} if none was found
     */
    <T> T getService(Class<T> type);

    /**
     * Returns all services implementing the specified interface.
     *
     * @param type the interface
     * @return the services implementing the interface
     */
    <T> List<T> getServices(Class<T> type);

    /**
     * Returns the bundle context, or {@code null} if the manager is not running.
     *
     * @return the bundle context. May be {@code null}
     */
    BundleContext getBundleContext();

    /**
     * Returns a list of all installed bundles.
     *
     * @return the installed bundles
     */
    Bundle[] getBundles();

    /**
     * Determines if the plugin manager is started.
     *
     * @return {@code true} if the plugin manager is started
     */
    boolean isStarted();

    /**
     * Starts the plugin manager.
     *
     * @throws BundleException for any error
     */
    void start() throws BundleException;

    /**
     * Stops the plugin manager.
     * <p>
     * This method will wait until the manager shuts down.
     *
     * @throws BundleException      for any error
     * @throws InterruptedException if the thread was interrupted
     */
    void stop() throws BundleException, InterruptedException;

    /**
     * Installs a plugin from the specified stream.
     * <p>
     * The plugin manager must be started for this operation to be successful.
     *
     * @param location the location identifier of the plugin to install.
     * @param stream   the stream object from which the plugin will be read
     * @throws BundleException if the plugin cannot be installed
     */
    void install(String location, InputStream stream) throws BundleException;

    /**
     * Adds a listener to be notified of plugin manager events.
     *
     * @param listener the listener to notify
     */
    void addListener(PluginManagerListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(PluginManagerListener listener);
}
