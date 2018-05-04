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

import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Hospitalizations;
import org.openvpms.smartflow.model.event.AdmissionEvent;
import org.openvpms.smartflow.model.event.AnestheticsEvent;
import org.openvpms.smartflow.model.event.DischargeEvent;
import org.openvpms.smartflow.model.event.Event;
import org.openvpms.smartflow.model.event.InventoryImportedEvent;
import org.openvpms.smartflow.model.event.MedicsImportedEvent;
import org.openvpms.smartflow.model.event.NotesEvent;
import org.openvpms.smartflow.model.event.TreatmentEvent;

import java.util.Collections;
import java.util.Map;

/**
 * Dispatches Smart Flow Sheet events.
 *
 * @author Tim Anderson
 */
public class DefaultEventDispatcher implements EventDispatcher {

    /**
     * The treatment event processor.
     */
    private final TreatmentEventProcessor treatmentProcessor;

    /**
     * The notes event processor.
     */
    private final NotesEventProcessor notesProcessor;

    /**
     * The anesthetics event processor.
     */
    private final AnestheticsEventProcessor anestheticsProcessor;

    /**
     * The discharge event processor.
     */
    private final DischargeEventProcessor dischargeProcessor;

    /**
     * The inventory imported event processor.
     */
    private final InventoryImportedEventProcessor inventoryImportedProcessor;

    /**
     * The medics imported event processor.
     */
    private final MedicsImportedEventProcessor medicsImportedProcessor;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(DefaultEventDispatcher.class);

    /**
     * Listeners for specific messages.
     * These are held on to with soft references, to avoid memory leaks if the client doesn't de-register them.
     */
    private Map<String, Listener<Event>> listeners = Collections.synchronizedMap(new ReferenceMap<>());

    /**
     * Constructs a {@link DefaultEventDispatcher}.
     *
     * @param location        the practice location. May be {@code null}
     * @param service         the archetype service
     * @param lookups         the lookups
     * @param factory         the Smart Flow Sheet service factory
     * @param practiceService the practice service
     * @param rules           the patient rules
     */
    public DefaultEventDispatcher(Party location, IArchetypeService service, ILookupService lookups,
                                  FlowSheetServiceFactory factory, PracticeService practiceService,
                                  PatientRules rules) {
        FlowSheetConfigService configService = new FlowSheetConfigService(service, practiceService);
        treatmentProcessor = new TreatmentEventProcessor(location, service, lookups, rules);
        notesProcessor = new NotesEventProcessor(service, configService);
        anestheticsProcessor = new AnestheticsEventProcessor(service, factory);
        dischargeProcessor = new DischargeEventProcessor(service, factory, configService);
        inventoryImportedProcessor = new InventoryImportedEventProcessor(service);
        medicsImportedProcessor = new MedicsImportedEventProcessor(service);
    }

    /**
     * Dispatches an event.
     *
     * @param event the event
     */
    @Override
    public void dispatch(Event event) {
        if (event instanceof NotesEvent) {
            notesProcessor.process((NotesEvent) event);
        } else if (event instanceof TreatmentEvent) {
            treatmentProcessor.process((TreatmentEvent) event);
        } else if (event instanceof AdmissionEvent) {
            admitted((AdmissionEvent) event);
        } else if (event instanceof DischargeEvent) {
            dischargeProcessor.process((DischargeEvent) event);
        } else if (event instanceof AnestheticsEvent) {
            // TODO: ignoring AnestheticsEvent for now as they can be handled at discharge and there is currently
            // no easy way to avoid duplicates
            // anestheticsProcessor.process((AnestheticsEvent) event);
        } else if (event instanceof InventoryImportedEvent) {
            inventoryImportedProcessor.process((InventoryImportedEvent) event);
        } else if (event instanceof MedicsImportedEvent) {
            medicsImportedProcessor.process((MedicsImportedEvent) event);
        }
    }

    /**
     * Monitors for a single event with the specified id.
     *
     * @param id       the event identifier
     * @param listener the listener
     */
    @Override
    public void addListener(String id, Listener<Event> listener) {
        listeners.put(id, listener);
    }

    /**
     * Removes a listener for an event identifier.
     *
     * @param id the event identifier
     */
    @Override
    public void removeListener(String id) {
        listeners.remove(id);
    }

    /**
     * Invoked when one or more patients are admitted.
     *
     * @param event the admission event
     */
    protected void admitted(AdmissionEvent event) {
        if (log.isDebugEnabled()) {
            Hospitalizations list = event.getObject();
            if (list != null) {
                for (Hospitalization hospitalization : list.getHospitalizations()) {
                    log.debug("Admitted: " + hospitalization.getPatient().getName());
                }
            }
        }
    }

}
