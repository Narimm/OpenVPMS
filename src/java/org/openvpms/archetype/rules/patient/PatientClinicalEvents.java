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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Maintains a cache of <em>act.patientClinicalEvent</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientClinicalEvents {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The events, keyed on reference.
     */
    private Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();

    /**
     * The events for a patient, keyed on reference.
     */
    private Map<IMObjectReference, List<Act>> eventsByPatient = new HashMap<IMObjectReference, List<Act>>();


    /**
     * Constructs a {@link PatientClinicalEvents}.
     *
     * @param service the archetype service
     */
    public PatientClinicalEvents(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns an event given its reference.
     *
     * @param reference the event reference. May be {@code null}
     * @return the corresponding event, or {@code null} if none is found
     */
    public Act getEvent(IMObjectReference reference) {
        Act event = null;
        if (reference != null) {
            event = events.get(reference);
            if (event == null) {
                event = (Act) service.get(reference);
                if (event != null) {
                    events.put(reference, event);
                    IMObjectReference patient = getPatient(event);
                    if (patient != null) {
                        List<Act> events = eventsByPatient.get(patient);
                        if (events == null) {
                            events = new ArrayList<Act>();
                            eventsByPatient.put(patient, events);
                        }
                        events.add(event);
                        sortEvents(events);
                    }
                }
            }
        }
        return event;
    }

    /**
     * Returns the patient reference associated with an event.
     *
     * @param event the event
     * @return the event's patient reference, or {@code null} if none is found
     */
    public IMObjectReference getPatient(Act event) {
        ActBean bean = new ActBean(event, service);
        return bean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION);
    }

    /**
     * Returns the events associated with a patient.
     *
     * @param patient the patient
     * @return the events, or {@code null} if none are found
     */
    public List<Act> getEvents(IMObjectReference patient) {
        return eventsByPatient.get(patient);
    }

    /**
     * Adds an event.
     *
     * @param event the event to add
     */
    public void addEvent(Act event) {
        events.put(event.getObjectReference(), event);

        IMObjectReference patient = getPatient(event);
        if (patient != null) {
            List<Act> acts = eventsByPatient.get(patient);
            if (acts == null) {
                acts = new ArrayList<Act>();
                eventsByPatient.put(patient, acts);
            }
            acts.add(event);
            sortEvents(acts);
        }
    }

    /**
     * Returns an event linked to an act.
     *
     * @param act the act
     * @return the linked event, or {@code null} if an event isn't linked to the act
     */
    public Act getLinkedEvent(Act act) {
        IMObjectReference ref = getLinkedEventRef(act);
        return getEvent(ref);
    }

    /**
     * Returns the reference of an event linked to an act.
     *
     * @param act the act
     * @return the linked event reference, or {@code null} if an event isn't linked to the act
     */
    public IMObjectReference getLinkedEventRef(Act act) {
        IMObjectReference ref;
        ActBean bean = new ActBean(act, service);
        if (bean.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            ref = bean.getSourceObjectRef(act.getTargetActRelationships(),
                                          PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM);
        } else {
            ref = bean.getSourceObjectRef(act.getTargetActRelationships(), PatientArchetypes.CLINICAL_EVENT_ITEM);
        }
        return ref;
    }

    /**
     * Helper to add a relationship between an event and an act.
     *
     * @param event the event
     * @param act   the act
     */
    public void addRelationship(Act event, Act act) {
        ActBean bean = new ActBean(event, service);
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM, act);
        } else {
            bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, act);
        }
    }

    /**
     * Helper to remove a relationship between an event and an act.
     *
     * @param event the event
     * @param act   the act
     */
    public void removeRelationship(Act event, Act act) {
        ActBean bean = new ActBean(event);
        ActRelationship relationship = bean.getRelationship(act);
        if (relationship != null) {
            event.removeSourceActRelationship(relationship);
            act.removeTargetActRelationship(relationship);
        }
    }

    /**
     * Determines if an act has a relationship to an event.
     *
     * @param act the act
     * @return {@code true} if the act has a relationship, otherwise {@code false}
     */
    public boolean hasRelationship(Act act) {
        return getLinkedEventRef(act) != null;
    }

    /**
     * Sorts events on ascending start time.
     *
     * @param events the events to sort
     */
    private void sortEvents(List<Act> events) {
        if (events.size() > 1) {
            Collections.sort(events, new Comparator<Act>() {
                public int compare(Act o1, Act o2) {
                    return DateRules.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime());
                }
            });
        }
    }

}
