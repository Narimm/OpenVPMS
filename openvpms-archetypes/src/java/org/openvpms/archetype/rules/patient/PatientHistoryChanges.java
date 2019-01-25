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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Predicates;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;

import java.util.ArrayList;
import java.util.Date;
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
     * Appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * The events, keyed on reference.
     */
    private final Map<Reference, Act> events = new HashMap<>();

    /**
     * Determines if events are linked to boarding appointments.
     */
    private final Map<Reference, Boolean> boarding = new HashMap<>();

    /**
     * The events for a patient, keyed on reference.
     */
    private final Map<Reference, List<Act>> eventsByPatient = new HashMap<>();

    /**
     * The acts to save.
     */
    private final Map<Reference, Act> toSave = new HashMap<>();

    /**
     * Used to determine if an event was new, prior to {@link #save()} being invoked.
     */
    private final Set<Reference> newEvents = new HashSet<>();

    /**
     * The objects to remove.
     */
    private final Set<IMObject> toRemove = new HashSet<>();

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
        this.rules = new AppointmentRules(service);
    }

    /**
     * Returns an event given its reference.
     *
     * @param reference the event reference. May be {@code null}
     * @return the corresponding event, or {@code null} if none is found
     */
    public Act getEvent(Reference reference) {
        Act event = null;
        if (reference != null) {
            event = events.get(reference);
            if (event == null) {
                event = (Act) service.get(reference);
                if (event != null) {
                    events.put(reference, event);
                    Reference patient = getPatient(event);
                    if (patient != null) {
                        List<Act> events = eventsByPatient.get(patient);
                        if (events == null) {
                            events = new ArrayList<>();
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
    public Reference getPatient(Act event) {
        IMObjectBean bean = service.getBean(event);
        return getPatient(bean);
    }

    /**
     * Returns the events associated with a patient.
     *
     * @param patient the patient
     * @return the events, or {@code null} if none are found
     */
    public List<Act> getEvents(Reference patient) {
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
        Reference ref = event.getObjectReference();
        if (!events.containsKey(ref)) {
            events.put(ref, event);
            if (event.isNew()) {
                newEvents.add(ref);
            }
            IMObjectBean bean = service.getBean(event);
            if (event.isNew()) {
                // add author and location to new events
                if (author != null && bean.getTargetRef("author") == null) {
                    bean.setTarget("author", author);
                }
                if (location != null && bean.getTargetRef("location") == null) {
                    bean.setTarget("location", location);
                }
                toSave.put(ref, event);
            }

            Reference patient = getPatient(bean);
            if (patient != null) {
                List<Act> acts = eventsByPatient.get(patient);
                if (acts == null) {
                    acts = new ArrayList<>();
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
        Reference ref = getLinkedEventRef(act);
        return getEvent(ref);
    }

    /**
     * Returns the reference of an event linked to an act.
     *
     * @param act the act
     * @return the linked event reference, or {@code null} if an event isn't linked to the act
     */
    public Reference getLinkedEventRef(Act act) {
        IMObjectBean bean = service.getBean(act);
        return bean.hasNode("event") ? bean.getSourceRef("event") : null;
    }

    /**
     * Helper to add a relationship between an event and an act.
     *
     * @param event the event
     * @param act   the act
     */
    public void addRelationship(Act event, Act act) {
        IMObjectBean bean = service.getBean(event);
        String node = getRelationshipNode(act);
        ActRelationship relationship = (ActRelationship) bean.addTarget(node, act);
        act.addActRelationship(relationship);
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
        IMObjectBean bean = service.getBean(event);
        String node = getRelationshipNode(act);
        ActRelationship relationship = bean.getValue(node, ActRelationship.class, Predicates.targetEquals(act));
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
        IMObjectBean bean = service.getBean(act);
        ActRelationship relationship = bean.getValue("event", ActRelationship.class, Predicates.targetEquals(act));
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
        IMObjectBean bean = service.getBean(item);
        ActRelationship relationship = (ActRelationship) bean.addTarget("documents", document);
        document.addActRelationship(relationship);
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

        IMObjectBean itemBean = service.getBean(item);
        ActRelationship r = itemBean.getValue("documents", ActRelationship.class, Predicates.targetEquals(document));
        item.removeActRelationship(r);
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
     * Determines if an event added via {@link #addEvent(Act)} was new prior to {@link #save()}  being invoked.
     *
     * @param event the event
     * @return {@code true} if the event was new prior to save
     */
    public boolean isNew(Act event) {
        return newEvents.contains(event.getObjectReference());
    }

    /**
     * Marks events as being completed, setting their end time.
     *
     * @param endTime the end time
     */
    public void complete(Date endTime) {
        for (Act event : events.values()) {
            if (!toRemove.contains(event)) {
                event.setStatus(ActStatus.COMPLETED);
                event.setActivityEndTime(endTime);
                changed(event);
            }
        }
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
     * Determines if an event is for boarding.
     *
     * @param act the event
     * @return {@code true} if the event is used for boarding
     */
    public boolean isBoarding(Act act) {
        Reference reference = act.getObjectReference();
        Boolean result = boarding.get(reference);
        if (result == null) {
            IMObjectBean bean = service.getBean(act);
            Act appointment = bean.getSource("appointment", Act.class);
            result = appointment != null && rules.isBoardingAppointment(appointment);
            boarding.put(reference, result);
        }
        return result;
    }

    /**
     * Returns the event relationship node name for a target act.
     *
     * @param act the act
     * @return the event node name
     */
    private String getRelationshipNode(Act act) {
        return act.isA(CustomerAccountArchetypes.INVOICE_ITEM) ? "chargeItems" : "items";
    }

    /**
     * Sorts events on ascending start time.
     *
     * @param events the events to sort
     */
    private void sortEvents(List<Act> events) {
        if (events.size() > 1) {
            events.sort((o1, o2) -> DateRules.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime()));
        }
    }

    /**
     * Returns the patient associated with an act.
     *
     * @param bean the act bean
     * @return the patient reference, or {@code null} if none is found
     */
    private Reference getPatient(IMObjectBean bean) {
        return bean.getTargetRef("patient");
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
