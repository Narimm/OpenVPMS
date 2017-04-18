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

package org.openvpms.smartflow.event.impl;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.event.SmartFlowSheetEventService;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.event.Event;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the {@link SmartFlowSheetEventService}.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheetEventServiceImpl implements InitializingBean, DisposableBean, SmartFlowSheetEventService {

    /**
     * The SFS service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Practice locations keyed on clinic API key.
     */
    private Map<String, Set<Party>> locationsByKey = new HashMap<>();

    /**
     * Clinic API key keyed on location.
     */
    private Map<Party, String> keysByLocation = new HashMap<>();

    /**
     * The queue readers, keyed on clinic API key.
     */
    private Map<String, QueueReader> readers = new HashMap<>();

    /**
     * The event dispatchers, keyed on clinic API key.
     */
    private final Map<String, EventDispatcher> dispatchers = new HashMap<>();

    /**
     * Used to schedule dispatching.
     */
    private final ExecutorService executor;

    /**
     * Used to handle practice location updates.
     */
    private final ExecutorService updateService;

    /**
     * The listener for practice location updates.
     */
    private final IArchetypeServiceListener listener;

    /**
     * The default interval between polls, in seconds.
     */
    private int interval = 30;

    /**
     * The user assigned to the dispatch thread.
     */
    private User user;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SmartFlowSheetEventServiceImpl.class);

    /**
     * Constructs a {@link SmartFlowSheetEventServiceImpl}.
     *
     * @param factory         the factory for SFS services
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param handlers        the document handlers
     */
    protected SmartFlowSheetEventServiceImpl(FlowSheetServiceFactory factory, IArchetypeService service,
                                             PracticeService practiceService, DocumentHandlers handlers) {
        this.factory = factory;
        this.service = service;
        this.practiceService = practiceService;
        this.handlers = handlers;

        updateService = Executors.newSingleThreadExecutor();
        executor = Executors.newSingleThreadExecutor();

        // listen for practice location update events, and schedule a thread to handle them.
        // This avoids blocking the user thread that updated the location.
        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(final IMObject object) {
                updateService.execute(new Runnable() {
                    @Override
                    public void run() {
                        locationSaved((Party) object);
                    }
                });
            }

            @Override
            public void removed(final IMObject object) {

                updateService.execute(new Runnable() {
                    @Override
                    public void run() {
                        locationRemoved((Party) object);
                    }
                });
            }
        };

    }

    /**
     * Sets the interval between each poll.
     *
     * @param interval the interval, in seconds
     */
    @Override
    public void setPollInterval(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Argument 'interval' must be > 0");
        }
        this.interval = interval;
    }

    /**
     * Triggers a poll for events.
     */
    @Override
    public void poll() {
        QueueReader[] list;
        synchronized (this) {
            list = readers.values().toArray(new QueueReader[readers.size()]);
        }
        for (QueueReader reader : list) {
            reader.poll();
        }
    }

    /**
     * Monitors for a single event with the specified id.
     * <p/>
     * The listener is automatically removed, once the event is handled.
     *
     * @param location the location the event belongs to
     * @param id       the event identifier
     * @param listener the listener
     */
    @Override
    public void addListener(Party location, String id, Listener<Event> listener) {
        EventDispatcher dispatcher = getDispatcher(location);
        if (dispatcher != null) {
            dispatcher.addListener(id, listener);
            poll();
        }
    }

    /**
     * Removes a listener for an event identifier.
     *
     * @param location the location the event belongs to
     * @param id       the event identifier
     */
    @Override
    public void removeListener(Party location, String id) {
        EventDispatcher dispatcher = getDispatcher(location);
        if (dispatcher != null) {
            dispatcher.removeListener(id);
        }
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        for (Party location : practiceService.getLocations()) {
            String key = factory.getClinicAPIKey(location);
            if (key != null) {
                addKey(key, location);
            }
        }
        service.addListener(PracticeArchetypes.LOCATION, listener);
        poll();
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors
     */
    @Override
    public void destroy() throws Exception {
        service.removeListener(PracticeArchetypes.LOCATION, listener);
        updateService.shutdown();
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException exception) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the dispatcher for the specified location.
     *
     * @param location the location
     * @return the dispatcher, or {@code null} if none is found
     */
    private synchronized EventDispatcher getDispatcher(Party location) {
        EventDispatcher result = null;
        String key = keysByLocation.get(location);
        if (key != null) {
            result = dispatchers.get(key);
        }
        return result;
    }

    /**
     * Returns the user to set the security context in the dispatch thread.
     *
     * @return the user, or {@code null} if none has been configured
     */
    private User getServiceUser() {
        if (user == null) {
            synchronized (this) {
                user = practiceService.getServiceUser();
                if (user == null) {
                    log.error("Missing party.organisationPractice serviceUser. Messages cannot be sent until "
                              + "this is configured");
                }
            }
        }
        return user;
    }

    /**
     * Returns the Azure Service Bus configuration for a practice location.
     *
     * @return the configuration, or {@code null} if it is not configured
     */
    private ServiceBusConfig getConfig(Party location) {
        ReferenceDataService referenceData = factory.getReferenceDataService(location);
        return referenceData.getServiceBusConfig();
    }

    private synchronized void locationSaved(Party location) {
        String existingKey = keysByLocation.get(location);
        String newKey = (location.isActive()) ? factory.getClinicAPIKey(location) : null;
        if (!ObjectUtils.equals(existingKey, newKey)) {
            QueueReader reader;
            if (existingKey == null) {
                reader = addKey(newKey, location);
            } else {
                removeKey(existingKey, location);
                reader = addKey(newKey, location);
            }
            if (reader != null) {
                reader.poll();
            }
        }
    }

    private synchronized void locationRemoved(Party location) {
        String existingKey = keysByLocation.get(location);
        if (existingKey != null) {
            removeKey(existingKey, location);
        }
    }

    /**
     * Adds an API key for the specified practice location.
     * <p/>
     * If the key is not already registered, this creates an {@link EventDispatcher} to handle messages read from
     * the SFS Azure Service Bus queue.
     *
     * @param key      the clinic API key
     * @param location the practice location
     * @return the queue reader, or {@code null} if none was added
     */
    private QueueReader addKey(String key, Party location) {
        QueueReader reader = null;
        try {
            Set<Party> locations = locationsByKey.get(key);
            if (locations == null) {
                locations = new HashSet<>();
                locationsByKey.put(key, locations);
                ServiceBusConfig config = getConfig(location);
                EventDispatcher dispatcher = new DefaultEventDispatcher(service, handlers);
                dispatchers.put(key, dispatcher);
                reader = new QueueReader(config, dispatcher, getServiceUser(), executor);
                readers.put(key, reader);
            }
            locations.add(location);
            keysByLocation.put(location, key);
        } catch (Exception exception) {
            log.error("Failed initialise Smart Flow Sheet queue for location=" + location.getName(), exception);
        }
        return reader;
    }

    private boolean removeKey(String key, Party location) {
        boolean reset = false;
        Set<Party> locations = locationsByKey.get(key);
        if (locations != null) {
            locations.remove(location);
            if (locations.isEmpty()) {
                reset = true;
            }
        }
        if (reset) {
            QueueReader reader = readers.remove(key);
            if (reader != null) {
                reader.destroy();
            }
        }
        return reset;
    }

}
