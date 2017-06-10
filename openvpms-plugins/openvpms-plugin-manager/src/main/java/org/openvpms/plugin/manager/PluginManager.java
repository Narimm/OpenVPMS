package org.openvpms.plugin.manager;

import org.osgi.framework.BundleException;

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
}
