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
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Tracks changes to patient histories.
 * <p/>
 * This can be used during invoicing to manage relationships between charge items and patient history items.
 *
 * @author Tim Anderson
 */
public class PatientHistoryChanges {

    /**
     * The author for new clinical events. May be {@code null}
     */
    private final User author;

    /**
     * The location for new clinical events. May be {@code null}
     */
    private final Entity location;

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
     * The acts to save.
     */
    private Map<IMObjectReference, Act> toSave = new HashMap<IMObjectReference, Act>();

    /**
     * The objects to remove.
     */
    private Set<IMObject> toRemove = new HashSet<IMObject>();

    /**
     * Constructs a {@link PatientHistoryChanges}.
     *
     * @param author   the author for new events. May be {@code null}
     * @param location the location for new events. May be {@code null}
     * @param service  the archetype service
     */
    public PatientHistoryChanges(User author, Entity location, IArchetypeService service) {
        this.author = author;
        this.location = location;
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
        return getPatient(bean);
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
     * <p/>
     * If the event is new, it will be committed by {@link #save}.
     *
     * @param event the event to add
     */
    public void addEvent(Act event) {
        IMObjectReference ref = event.getObjectReference();
        if (!events.containsKey(ref)) {
            events.put(ref, event);
            ActBean bean = new ActBean(event, service);
            if (event.isNew()) {
                // add author and location to new events
                if (author != null && bean.getNodeParticipantRef("author") == null) {
                    bean.addNodeParticipation("author", author);
                }
                if (location != null && bean.getNodeParticipantRef("location") == null) {
                    bean.addNodeParticipation("location", location);
                }
                toSave.put(ref, event);
            }

            IMObjectReference patient = getPatient(bean);
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
        changed(event);
        changed(act);
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
            changed(event);
            changed(act);
        }
    }

    /**
     * Removes a relationship between an act and the its <em>act.patientClinicalEvent</em>
     * <p/>
     * If a relationship is removed, both the act and event will be queued for save.
     *
     * @param act the act
     */
    public void removeRelationship(Act act) {
        ActBean bean = new ActBean(act, service);
        ActRelationship relationship;
        if (bean.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            relationship = bean.getRelationship(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM);
        } else {
            relationship = bean.getRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM);
        }
        if (relationship != null) {
            Act event = getEvent(relationship.getSource());
            if (event != null) {
                removeRelationship(event, act);
            }
        }
    }

    /**
     * Adds an invoice item document.
     * <p/>
     * This will add a relationship between them, and schedule them for commit.
     *
     * @param item     the invoice item
     * @param document the document
     */
    public void addItemDocument(FinancialAct item, Act document) {
        ActBean bean = new ActBean(item, service);
        bean.addRelationship("actRelationship.invoiceItemDocument", document);
        changed(item);
        changed(document);
    }

    /**
     * Removes an invoice item document.
     *
     * @param item     the invoice item
     * @param document the document
     */
    public void removeItemDocument(FinancialAct item, Act document) {
        changed(document);  // need to save the act with relationships removed, prior to removing it
        changed(item);
        toRemove.add(document);

        ActBean itemBean = new ActBean(item, service);
        ActRelationship r = itemBean.getRelationship(document);
        itemBean.removeRelationship(r);
        document.removeActRelationship(r);

        removeRelationship(document); // remove the event relationship
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
     * Saves any changes.
     */
    public void save() {
        if (!toSave.isEmpty()) {
            service.save(toSave.values());
        }
        if (!toRemove.isEmpty()) {
            service.save(toRemove);
            // need to save before removal in order to avoid ObjectDeletedException for old relationships
            for (IMObject object : toRemove) {
                service.remove(object);
            }
        }
    }

    /**
     * Retrieve an object given its reference.
     *
     * @param ref the reference
     * @return the object corresponding to the reference, or {@code null} if it can't be retrieved
     * @throws ArchetypeServiceException for any error
     */
    public IMObject getObject(IMObjectReference ref) {
        IMObject result = null;
        if (ref != null) {
            if (TypeHelper.isA(ref, PatientArchetypes.CLINICAL_EVENT)) {
                result = getEvent(ref);
            } else {
                result = toSave.get(ref);
            }
            if (result == null) {
                result = service.get(ref);
            }
        }
        return result;
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

    /**
     * Returns the patient associated with an act.
     *
     * @param bean the act bean
     * @return the patient reference, or {@code null} if none is found
     */
    private IMObjectReference getPatient(ActBean bean) {
        return bean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION);
    }

    /**
     * Flags an act as being changed.
     *
     * @param act the changed act
     */
    private void changed(Act act) {
        toSave.put(act.getObjectReference(), act);
    }
}
