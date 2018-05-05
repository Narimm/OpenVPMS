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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.event.SmartFlowSheetEventService;
import org.openvpms.smartflow.model.event.Event;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of the {@link SmartFlowSheetEventService}.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheetEventServiceImpl implements InitializingBean, DisposableBean, SmartFlowSheetEventService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The queue dispatchers.
     */
    private final QueueDispatchers dispatchers;

    /**
     * The scheduled event dispatcher.
     */
    private final ScheduledDispatcher dispatcher;

    /**
     * Used to handle practice location updates.
     */
    private final ExecutorService updateService;

    /**
     * The listener for practice location updates.
     */
    private final IArchetypeServiceListener listener;

    /**
     * Constructs a {@link SmartFlowSheetEventServiceImpl}.
     *
     * @param factory            the factory for SFS services
     * @param service            the archetype service
     * @param lookups            the lookup service
     * @param practiceService    the practice service
     * @param transactionManager the transaction manager
     * @param rules              the patient rules
     */
    protected SmartFlowSheetEventServiceImpl(FlowSheetServiceFactory factory, IArchetypeService service,
                                             ILookupService lookups, PracticeService practiceService,
                                             PlatformTransactionManager transactionManager, PatientRules rules) {
        this.service = service;
        this.practiceService = practiceService;

        updateService = Executors.newSingleThreadExecutor();

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
        QueueDispatcherFactory readerFactory = new QueueDispatcherFactory(factory, service, lookups, transactionManager,
                                                                          practiceService, rules);
        dispatchers = new QueueDispatchers(readerFactory);
        dispatcher = new ScheduledDispatcher(dispatchers, practiceService);
    }

    /**
     * Sets the interval between each poll.
     *
     * @param interval the interval, in seconds
     */
    @Override
    public void setPollInterval(int interval) {
        dispatcher.setPollInterval(interval);
    }

    /**
     * Triggers a poll for events.
     */
    @Override
    public void poll() {
        dispatcher.dispatch();
    }

    /**
     * Monitors for a single event with the specified id.
     * <p>
     * The listener is automatically removed, once the event is handled.
     *
     * @param location the location the event belongs to
     * @param id       the event identifier
     * @param listener the listener
     */
    @Override
    public void addListener(Party location, String id, Listener<Event> listener) {
        EventDispatcher dispatcher = dispatchers.getEventDispatcher(location);
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
        EventDispatcher dispatcher = dispatchers.getEventDispatcher(location);
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
            dispatchers.add(location);
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
        dispatcher.destroy();
        updateService.shutdown();
    }

    /**
     * Invoked when a practice location is saved.
     *
     * @param location the practice location
     */
    private void locationSaved(Party location) {
        QueueDispatcher reader = dispatchers.add(location);
        if (reader != null) {
            dispatcher.dispatch();
        }
    }

    /**
     * Invoked when a practice location is removed.
     *
     * @param location the practice location
     */
    private void locationRemoved(Party location) {
        dispatchers.remove(location);
        dispatcher.dispatch();
    }

}
