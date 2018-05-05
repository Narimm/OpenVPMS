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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.plugin.manager.PluginManagerListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link PluginManager} that is configured by an <em>entity.pluginConfiguration</em>.
 * <p>
 * The plugin manager is started after Spring has fully initialised, to avoid deadlocks.
 *
 * @author Tim Anderson
 */
public class ConfigurablePluginManager implements PluginManager, DisposableBean, ServletContextAware,
        ApplicationListener<ContextRefreshedEvent> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The plugin service provider;
     */
    private final PluginServiceProvider provider;

    /**
     * The listeners.
     */
    private final Set<PluginManagerListener> listeners = new HashSet<>();

    /**
     * The plugin manager.
     */
    private volatile PluginManager manager;

    /**
     * The servlet context.
     */
    private ServletContext servletContext;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ConfigurablePluginManager.class);

    /**
     * Constructs a {@link ConfigurablePluginManager}.
     *
     * @param service  the archetype service
     * @param provider the plugin service provider
     */
    public ConfigurablePluginManager(IArchetypeService service, PluginServiceProvider provider) {
        this.service = service;
        this.provider = provider;
    }

    /**
     * Returns the first service implementing the specified interface.
     *
     * @param type the interface
     * @return the first service implementing the interface, or {@code null} if none was found
     */
    @Override
    public <T> T getService(Class<T> type) {
        PluginManager manager = this.manager;
        return (manager != null) ? manager.getService(type) : null;
    }

    /**
     * Returns all services implementing the specified interface.
     *
     * @param type the interface
     * @return the services implementing the interface
     */
    @Override
    public <T> List<T> getServices(Class<T> type) {
        PluginManager manager = this.manager;
        return (manager != null) ? manager.getServices(type) : Collections.<T>emptyList();
    }

    /**
     * Returns the bundle context, or {@code null} if the manager is not running.
     *
     * @return the bundle context. May be {@code null}
     */
    @Override
    public BundleContext getBundleContext() {
        PluginManager manager = this.manager;
        return (manager != null) ? manager.getBundleContext() : null;
    }

    /**
     * Returns a list of all installed bundles.
     *
     * @return the installed bundles
     */
    @Override
    public Bundle[] getBundles() {
        PluginManager manager = this.manager;
        return (manager != null) ? manager.getBundles() : new Bundle[0];
    }

    /**
     * Determines if the plugin manager is started.
     *
     * @return {@code true} if the plugin manager is started
     */
    @Override
    public boolean isStarted() {
        PluginManager manager = this.manager;
        return manager != null && manager.isStarted();
    }

    /**
     * Starts the plugin manager.
     *
     * @throws BundleException for any error
     */
    @Override
    public synchronized void start() throws BundleException {
        if (manager == null) {
            ArchetypeQuery query = new ArchetypeQuery("entity.pluginConfiguration", false, false);
            query.add(Constraints.sort("id"));
            query.setMaxResults(1);
            IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
            Entity configuration = (iterator.hasNext()) ? iterator.next() : null;
            String path = null;
            if (configuration != null && configuration.isActive()) {
                IMObjectBean bean = new IMObjectBean(configuration, service);
                path = bean.getString("path");
            }
            if (path != null) {
                manager = new PluginManagerImpl(path, provider, servletContext);
                for (PluginManagerListener listener : listeners) {
                    manager.addListener(listener);
                }
                manager.start();
            }
        }
    }

    /**
     * Stops the plugin manager.
     * <p>
     * This method will wait until the manager shuts down.
     *
     * @throws BundleException      for any error
     * @throws InterruptedException if the thread was interrupted
     */
    @Override
    public synchronized void stop() throws BundleException, InterruptedException {
        if (manager != null) {
            manager.stop();
            manager = null;
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
    public synchronized void install(String location, InputStream stream) throws BundleException {
        if (manager == null) {
            throw new BundleException("Plugin cannot be installed: PluginManager is not running",
                                      BundleException.INVALID_OPERATION);
        }
        manager.install(location, stream);
    }

    /**
     * Adds a listener to be notified of plugin manager events.
     *
     * @param listener the listener to notify
     */
    @Override
    public synchronized void addListener(PluginManagerListener listener) {
        listeners.add(listener);
        if (manager != null) {
            manager.addListener(listener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removeListener(PluginManagerListener listener) {
        listeners.remove(listener);
        if (manager != null) {
            manager.removeListener(listener);
        }
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            start();
        } catch (Throwable exception) {
            log.error("Failed to start the plugin manager", exception);
        }
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        try {
            stop();
        } finally {
            synchronized (this) {
                listeners.clear();
            }
        }
    }

    /**
     * Set the {@link ServletContext} that this object runs in.
     * <p>Invoked after population of normal bean properties but before an init
     * callback like InitializingBean's {@code afterPropertiesSet} or a
     * custom init-method. Invoked after ApplicationContextAware's
     * {@code setApplicationContext}.
     *
     * @param servletContext ServletContext object to be used by this object
     * @see InitializingBean#afterPropertiesSet
     * @see ApplicationContextAware#setApplicationContext
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
