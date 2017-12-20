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

package org.openvpms.plugin.internal.manager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.Util;
import org.apache.felix.main.AutoProcessor;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.plugin.manager.PluginManagerListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * OpenVPMS Plugin manager.
 *
 * @author Tim Anderson
 */
public class PluginManagerImpl implements PluginManager {

    /**
     * The plugins path.
     */
    private final String path;

    /**
     * The plugin service provider.
     */
    private final PluginServiceProvider provider;

    /**
     * The servlet context.
     */
    private final ServletContext context;

    /**
     * The Felix logger.
     */
    private final Logger logger = new Logger();

    /**
     * The listeners.
     */
    private final Set<PluginManagerListener> listeners = Collections.synchronizedSet(
            new HashSet<PluginManagerListener>());

    /**
     * Apache Felix.
     */
    private Felix felix;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PluginManagerImpl.class);

    /**
     * Constructs a {@link PluginManagerImpl}.
     *
     * @param path     the plugin installation path
     * @param provider the plugin service provider
     * @param context  the servlet context
     */
    public PluginManagerImpl(String path, PluginServiceProvider provider, ServletContext context) {
        this.path = path;
        this.provider = provider;
        this.context = context;
    }

    /**
     * Returns the first service implementing the specified interface.
     *
     * @param type the interface
     * @return the first service implementing the interface, or {@code null} if none was found
     */
    @Override
    public <T> T getService(Class<T> type) {
        T result = null;
        BundleContext context = getBundleContext();
        if (context != null) {
            ServiceReference<T> reference = context.getServiceReference(type);
            if (reference != null) {
                result = getService(reference, context);
            }
        }
        return result;
    }

    /**
     * Returns all services implementing the specified interface.
     *
     * @param type the interface
     * @return the services implementing the interface
     */
    @Override
    public <T> List<T> getServices(Class<T> type) {
        List<T> result = new ArrayList<>();
        BundleContext context = getBundleContext();
        if (context != null) {
            try {
                Collection<ServiceReference<T>> references = context.getServiceReferences(type, null);
                for (ServiceReference<T> reference : references) {
                    T service = getService(reference, context);
                    if (service != null) {
                        result.add(service);
                    }
                }
            } catch (InvalidSyntaxException ignore) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Returns the bundle context, or {@code null} if the manager is not running.
     *
     * @return the bundle context. May be {@code null}
     */
    public synchronized BundleContext getBundleContext() {
        return (felix != null) ? felix.getBundleContext() : null;
    }

    /**
     * Returns a list of all installed bundles.
     *
     * @return the installed bundles
     */
    @Override
    public Bundle[] getBundles() {
        BundleContext context = getBundleContext();
        return context != null ? context.getBundles() : new Bundle[0];
    }

    /**
     * Starts the plugin manager.
     *
     * @throws BundleException for any error
     */
    @Override
    public synchronized void start() throws BundleException {
        if (felix == null) {
            File home = getHome();
            File etc = getDir(home, "etc", false);
            getDir(home, "system", false);
            File deploy = getDir(home, "deploy", false);
            File data = getDir(home, "data", true);
            File storage = getDir(data, "cache", true);

            String exports = new ExportPackages().getPackages(logger);

            Map<String, Object> config = new HashMap<>();
            config.put("plugin.home", home.getAbsolutePath());
            getConfiguration(etc, config);

            List<Object> list = new ArrayList<>();
            list.add(new PluginServiceBundleActivator(provider, context));
            config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
            config.put(FelixConstants.FRAMEWORK_STORAGE, storage.getAbsolutePath());
            config.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY,
                       AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + ", " + AutoProcessor.AUTO_DEPLOY_START_VALUE);
            config.put(AutoProcessor.AUTO_DEPLOY_STARTLEVEL_PROPERTY, 5);
            config.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY, deploy.getAbsolutePath());
            config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, exports);
            config.put(FelixConstants.LOG_LOGGER_PROP, logger);
            config.put(FelixConstants.LOG_LEVEL_PROP, "3");

            felix = new Felix(config);
            felix.init();
            AutoProcessor.process(config, felix.getBundleContext());
            felix.start();
            notifyStarted();
        }
    }

    /**
     * Determines if the plugin manager is started.
     *
     * @return {@code true} if the plugin manager is started
     */
    @Override
    public synchronized boolean isStarted() {
        return felix != null;
    }

    /**
     * Stops the plugin manager.
     * <p>
     * This method will wait until the manager shuts down.
     */
    @Override
    public synchronized void stop() throws BundleException, InterruptedException {
        if (felix != null) {
            felix.stop();
            felix.waitForStop(0);
            felix = null;
            notifyStopped();
        }
    }

    /**
     * Installs a plugin from the specified stream.
     * <p>
     * The plugin manager must be started for this operation to be successful.
     *
     * @param location the location identifier of the plugin to install.
     * @param stream   the stream object from which the plugin will be read
     * @throws BundleException if the plugin cannot be installed
     */
    @Override
    public void install(String location, InputStream stream) throws BundleException {
        try {
            File home = getHome();
            File deploy = getDir(home, "deploy", true);
            String name = FilenameUtils.getName(location);
            if (StringUtils.isEmpty(name) || !name.toLowerCase().endsWith(".jar")) {
                throw new BundleException("Invalid plugin name: " + location, BundleException.INVALID_OPERATION);
            }
            File file = new File(deploy, name);
            try (FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(stream, output);
            } catch (IOException exception) {
                throw new BundleException("Failed to deploy plugin to " + file, exception);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Adds a listener to be notified of plugin manager events.
     *
     * @param listener the listener to notify
     */
    @Override
    public void addListener(PluginManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeListener(PluginManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns a service given its reference.
     *
     * @param reference the reference
     * @param context   the bundle context
     * @return the service, or {@code null}, if it doesn't exist
     */
    private <T> T getService(ServiceReference<T> reference, BundleContext context) {
        T result = null;
        try {
            result = context.getService(reference);
        } catch (Exception exception) {
            log.debug(exception.getMessage(), exception);
        }
        return result;
    }

    /**
     * Returns the plugins home directory.
     *
     * @return the directory
     * @throws IllegalStateException if the directory is invalid
     */
    private File getHome() {
        File file;
        try {
            file = new File(path).getCanonicalFile();
            if (!file.exists() || !file.isDirectory()) {
                throw new IllegalStateException("Invalid plugin directory: " + path);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Invalid plugin directory: " + path);
        }
        return file;
    }

    /**
     * Loads the config.properties and startup.properties into the supplied map, substituting any variables
     *
     * @param dir    the configuration directory
     * @param config the configuration map to populate
     * @throws IllegalStateException if a configuration cannot be loaded
     */
    private void getConfiguration(File dir, Map config) {
        getConfig(dir, "config.properties", config);
        getConfig(dir, "startup.properties", config);
    }

    /**
     * Loads a configuration properties file into the supplied map, substituting any variables.
     *
     * @param dir    the configuration directory
     * @param path   the configuration file name
     * @param config the configuration map to populate
     * @throws IllegalStateException if a configuration cannot be loaded
     */
    @SuppressWarnings("unchecked")
    private void getConfig(File dir, String path, Map config) {
        Properties properties = new Properties();
        properties.putAll(config);
        File file = new File(dir, path);
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load configuration: " + file);
        }
        for (String key : properties.stringPropertyNames()) {
            String value = Util.substVars(properties.getProperty(key), key, null, properties);
            properties.setProperty(key, value);
        }
        config.putAll(properties);
    }

    /**
     * Returns a directory, optionally creating it if it doesn't exist.
     *
     * @param parent the parent directory
     * @param path   the child path
     * @param create if {@code true}, create the directory if it doesn't exist
     * @return the directory
     * @throws IllegalArgumentException if the directory is invalid or cannot be created
     */
    private File getDir(File parent, String path, boolean create) {
        File dir = new File(parent, path);
        if (!dir.exists()) {
            if (!create) {
                throw new IllegalArgumentException("Directory doesn't exist: " + dir);
            }
            if (!dir.mkdirs()) {
                throw new IllegalArgumentException("Failed to create directory: " + dir);
            }
        } else if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + dir);
        }

        return dir;
    }

    private void notifyStarted() {
        synchronized (listeners) {
            for (PluginManagerListener listener : listeners) {
                try {
                    listener.started();
                } catch (Throwable exception) {
                    log.error("PluginManagerListener threw exception", exception);
                }
            }
        }
    }

    private void notifyStopped() {
        synchronized (listeners) {
            for (PluginManagerListener listener : listeners) {
                try {
                    listener.stopped();
                } catch (Throwable exception) {
                    log.error("PluginManagerListener threw exception", exception);
                }
            }
        }
    }

}
