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

package org.openvpms.plugin.internal.service.archetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.plugin.manager.PluginManagerListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Notifies listeners when objects are updated or removed.
 *
 * @author Tim Anderson
 */
public abstract class IMObjectUpdateNotifier<T> implements InitializingBean, DisposableBean {

    /**
     * The plugin manager listener.
     */
    private final PluginManagerListener listener;

    /**
     * The listener type.
     */
    private final Class<T> type;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The plugin manager.
     */
    private final PluginManager manager;

    /**
     * Tracks registration of listeners.
     */
    private volatile ServiceTracker<T, T> tracker;

    /**
     * The listeners.
     */
    private Map<T, Listener> listeners = Collections.synchronizedMap(new HashMap<T, Listener>());

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectUpdateNotifier.class);

    /**
     * Constructs an {@link IMObjectUpdateNotifier}.
     *
     * @param type    the listener type
     * @param service the archetype service
     * @param manager the plugin manager
     */
    public IMObjectUpdateNotifier(Class<T> type, IArchetypeService service, final PluginManager manager) {
        this.type = type;
        listener = new PluginManagerListener() {
            @Override
            public void started() {
                onStart();
            }

            @Override
            public void stopped() {

            }
        };
        this.service = service;
        this.manager = manager;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     */
    @Override
    public void afterPropertiesSet() {
        manager.addListener(listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     */
    @Override
    public void destroy() throws Exception {
        ServiceTracker<T, T> tracker = this.tracker;
        if (tracker != null) {
            tracker.close();
        }
        manager.removeListener(listener);
    }

    /**
     * Invoked when the plugin manager starts.
     */
    protected void onStart() {
        tracker = new ServiceTracker<T, T>(manager.getBundleContext(), type, null) {
            /**
             * Invoked when a service is added.
             *
             * @param reference The reference to the service being added to this {@code ServiceTracker}.
             * @return The service object to be tracked for the service added to this {@code ServiceTracker}.
             */
            @Override
            public T addingService(ServiceReference<T> reference) {
                T service = super.addingService(reference);
                if (service != null) {
                    addService(service);
                }
                return service;
            }

            /**
             * Invoked when a service is removed.
             *
             * @param reference The reference to removed service.
             * @param service   The service object for the removed service.
             */
            @Override
            public void removedService(ServiceReference<T> reference, T service) {
                removeService(service);
                super.removedService(reference, service);
            }
        };
        tracker.open();
    }

    /**
     * Adds a service.
     *
     * @param service the service to add
     */
    protected void addService(final T service) {
        try {
            Listener listener = createListener(service);
            listeners.put(service, listener);
            listener.register(this.service);
        } catch (Throwable exception) {
            log.warn("Failed to add " + service.getClass(), exception);
        }
    }

    /**
     * Creates a listener.
     *
     * @param service the service to register a listener for
     * @return the listener
     */
    protected abstract Listener createListener(T service);

    /**
     * Removes a service.
     *
     * @param service the service to remove
     */
    protected void removeService(T service) {
        Listener listener = listeners.remove(service);
        if (listener != null) {
            listener.unregister(this.service);
        }
    }

    /**
     * Retunrs the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    protected static class Listener {
        private final String[] archetypes;

        private final IArchetypeServiceListener listener;

        public Listener(String[] archetypes, IArchetypeServiceListener listener) {
            this.archetypes = archetypes;
            this.listener = listener;
        }

        public void register(IArchetypeService service) {
            for (String archetype : archetypes) {
                service.addListener(archetype, listener);
            }
        }

        public void unregister(IArchetypeService service) {
            for (String archetype : archetypes) {
                service.removeListener(archetype, listener);
            }
        }
    }
}
