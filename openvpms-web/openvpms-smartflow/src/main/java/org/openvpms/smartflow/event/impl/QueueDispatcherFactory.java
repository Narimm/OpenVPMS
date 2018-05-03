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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Factory for {@link QueueDispatcher} instances.
 *
 * @author Tim Anderson
 */
class QueueDispatcherFactory {

    /**
     * Factory for Smart Flow Sheet services.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The object mapper.
     */
    private final ObjectMapper mapper;


    /**
     * Constructs a {@link QueueDispatcherFactory}.
     *
     * @param factory            the factory for SFS services
     * @param service            the archetype service
     * @param lookups            the lookup service
     * @param transactionManager the transaction manager
     * @param practiceService    the practice service
     * @param rules              the patient rules
     */
    public QueueDispatcherFactory(FlowSheetServiceFactory factory, IArchetypeService service, ILookupService lookups,
                                  PlatformTransactionManager transactionManager, PracticeService practiceService,
                                  PatientRules rules) {
        this.factory = factory;
        this.service = service;
        this.lookups = lookups;
        this.transactionManager = transactionManager;
        this.practiceService = practiceService;
        this.rules = rules;
        mapper = new ObjectMapper();
    }

    /**
     * Creates a new queue dispatcher.
     *
     * @param location the location to dispatch events for
     * @return a new queue dispatcher
     */
    public QueueDispatcher createQueueDispatcher(Party location) {
        ServiceBusConfig config = getConfig(location);
        if (config == null) {
            throw new IllegalStateException("Azure Service Bus is not configured for location: " + location.getName());
        }
        EventDispatcher dispatcher = createEventDispatcher(location);
        return new QueueDispatcher(location, config, dispatcher, mapper, transactionManager);
    }

    /**
     * Returns the clinic API key for the practice.
     *
     * @param location the practice location
     * @return the clinic API key, or {@code null} if none exists
     */
    public String getClinicAPIKey(Party location) {
        return factory.getClinicAPIKey(location);
    }

    /**
     * Creates a new event dispatcher.
     *
     * @param location the location to dispatch events for
     * @return a new event dispatcher
     */
    protected EventDispatcher createEventDispatcher(Party location) {
        return new DefaultEventDispatcher(location, service, lookups, factory, practiceService, rules);
    }

    /**
     * Returns the Azure Service Bus configuration for a practice location.
     *
     * @param location the practice location
     * @return the configuration, or {@code null} if it is not configured
     */
    protected ServiceBusConfig getConfig(Party location) {
        ReferenceDataService referenceData = factory.getReferenceDataService(location);
        return referenceData.getServiceBusConfig();
    }

}
