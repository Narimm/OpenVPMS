/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Patient medical record rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MedicalRecordRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Clinical event item short names.
     */
    private String[] clinicalEventItems;

    /**
     * Start time node name.
     */
    private static final String START_TIME = "startTime";

    /**
     * End time node name.
     */
    private static final String END_TIME = "endTime";


    /**
     * Creates a new <tt>MedicalRecordRules</tt>.
     */
    public MedicalRecordRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>MedicalRecordRules</tt>.
     *
     * @param service the archetype service
     */
    public MedicalRecordRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Recursively deletes children of <em>act.patientClinicalEvent</em> acts.
     *
     * @param act the deleted act
     */
    public void deleteChildRecords(Act act) {
        if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) {
            for (ActRelationship relationship :
                    act.getSourceActRelationships()) {
                Act child = get(relationship.getTarget());
                if (child != null) {
                    delete(child);
                }
            }
        }
    }

    /**
     * Adds an <em>act.patientMedication</em>/<em>act.patientInvestigation*</em>
     * to an <em>act.patientClinicalEvent</em> associated with the act's patient.
     * <p/>
     * The <em>act.patientClinicalEvent</em> is selected using {@link #getEventForAddition}.
     *
     * @param act       the act to add
     * @param startTime the startTime used to select the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToEvent(Act act, Date startTime) {
        addToEvents(Arrays.asList(act), startTime);
    }

    /**
     * Links a patient medical record to an <em>act.patientClinicalEvent</em>.
     * If the item is an <em>act.patientClinicalProblem</em>, all of its items will be also be linked to the event.
     *
     * @param event the event
     * @param item  the item
     */
    public void linkMedicalRecords(Act event, Act item) {
        if (TypeHelper.isA(item, PatientArchetypes.CLINICAL_PROBLEM)) {
            linkMedicalRecords(event, item, null);
        } else {
            linkMedicalRecords(event, null, item);
        }
    }

    /**
     * Links a patient medical record to an <em>act.patientClinicalEvent</em>,
     * and optionally an <em>act.patientClinicalProblem</em>, if no relationship exists.
     * <p/>
     * If <tt>problem</tt> is specified:
     * <ul>
     * <li>it will be linked to the event, if no relationship exists
     * <li>any of its items not presently linked to the event will be linked
     * </ul>
     *
     * @param event   the <em>act.patientClinicalEvent</em>
     * @param problem the <em>act.patientClinicalProblem</em>. May be <tt>null</tt>
     * @param item    the patient medical record. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void linkMedicalRecords(Act event, Act problem, Act item) {
        if (!TypeHelper.isA(event, PatientArchetypes.CLINICAL_EVENT)) {
            throw new IllegalArgumentException("Argument 'event' is of the wrong type: "
                                               + event.getArchetypeId().getShortName());
        }
        if (TypeHelper.isA(item, PatientArchetypes.CLINICAL_PROBLEM)) {
            throw new IllegalArgumentException("Argument 'item' is of the wrong type: "
                                               + item.getArchetypeId().getShortName());
        }
        if (problem != null && !TypeHelper.isA(problem, PatientArchetypes.CLINICAL_PROBLEM)) {
            throw new IllegalArgumentException("Argument 'problem' is of the wrong type: "
                                               + problem.getArchetypeId().getShortName());
        }
        ActBean bean = new ActBean(event, service);

        if (problem != null && item != null) {
            // link the problem and item if required
            ActBean problemBean = new ActBean(problem, service);
            if (!problemBean.hasRelationship(PatientArchetypes.CLINICAL_PROBLEM_ITEM, item)) {
                problemBean.addNodeRelationship("items", item);
                service.save(Arrays.asList(problem, item));
            }
        }

        if (item != null) {
            // link the event and item
            if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, item)) {
                bean.addNodeRelationship("items", item);
                service.save(Arrays.asList(event, item));
            }
        }

        if (problem != null) {
            // link the event and problem
            ActBean problemBean = new ActBean(problem, service);
            List<Act> toSave = new ArrayList<Act>();

            // if the problem has no parent event, add it
            if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem)) {
                bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem);
                toSave.add(event);
            }

            // for each of the problem's child acts, link them to the parent event
            List<Act> acts = problemBean.getNodeActs("items");
            for (Act child : acts) {
                if (TypeHelper.isA(child, getClinicalEventItems())
                    && !bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, child)) {
                    bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, child);
                    toSave.add(child);
                }
            }
            if (!toSave.isEmpty()) {
                service.save(toSave);
            }
        }
    }

    /**
     * Adds a list of <em>act.patientMedication</em>,
     * <em>act.patientInvestigation*</em> and <em>act.patientDocument*</em> acts
     * to an <em>act.patientClinicalEvent</em> associated with each act's
     * patient.
     * <p/>
     * The <em>act.patientClinicalEvent</em> is selected using {@link #getEventForAddition}.
     *
     * @param acts      the acts to add
     * @param startTime the startTime used to select the event
     */
    public void addToEvents(List<Act> acts, Date startTime) {
        Map<IMObjectReference, List<Act>> events = new HashMap<IMObjectReference, List<Act>>();
        Set<Act> changed = addToEvents(acts, startTime, events);
        if (!changed.isEmpty()) {
            service.save(changed);
        }
    }

    /**
     * Returns an <em>act.patientClinicalEvent</em> that may have acts added.
     * <p/>
     * The <em>act.patientClinicalEvent</em> is selected as follows:
     * <ol>
     * <li>find the event intersecting the start time for the patient
     * <li>if it is an <em>IN_PROGRESS</em> and
     * <pre>event.startTime &gt;= (startTime - 1 week)</pre>
     * use it
     * <li>if it is <em>COMPLETED</em> and
     * <pre>startTime &gt;= event.startTime && startTime <= event.endTime</pre>
     * use it; otherwise
     * <li>create a new event, with <em>COMPLETED</em> status and startTime
     * </ol>
     *
     * @param patient   the patient
     * @param startTime the start time
     * @param clinician the clinician. May be <tt>null</tt>
     * @return an event. May be newly created
     */
    public Act getEventForAddition(Party patient, Date startTime, Entity clinician) {
        IMObjectReference clinicianRef = (clinician != null) ? clinician.getObjectReference() : null;
        return getEventForAddition(patient.getObjectReference(), startTime, clinicianRef);
    }

    /**
     * The <em>act.patientClinicalEvent</em> is selected as follows:
     * <ol>
     * <li>find the event intersecting the start time for the patient
     * <li>if it is an <em>IN_PROGRESS</em> and
     * <pre>event.startTime &gt;= (startTime - 1 week)</pre>
     * use it
     * <li>if it is <em>COMPLETED</em> and
     * <pre>startTime &gt;= event.startTime && startTime <= event.endTime</pre>
     * use it; otherwise
     * <li>create a new event, with <em>COMPLETED</em> status and startTime
     * </ol>
     *
     * @param patient   the patient
     * @param startTime the start time
     * @param clinician the clinician. May be <tt>null</tt>
     * @return an event. May be newly created
     */
    public Act getEventForAddition(IMObjectReference patient, Date startTime, IMObjectReference clinician) {
        Map<IMObjectReference, List<Act>> events = new HashMap<IMObjectReference, List<Act>>();
        return getEventForAddition(events, patient, startTime, clinician);
    }

    /**
     * Creates a new <em>act.patientClinicalEvent</em> for the patient and start time.
     *
     * @param patient   the patient
     * @param startTime the event start time
     * @param clinician the clinician. May be <tt>null</tt>
     * @return a new event
     */
    public Act createEvent(Party patient, Date startTime, Entity clinician) {
        IMObjectReference clinicianRef = (clinician != null) ? clinician.getObjectReference() : null;
        return createEvent(patient.getObjectReference(), startTime, clinicianRef);
    }

    /**
     * Adds a list of
     * <em>act.patientMedication</em>, <em>act.patientInvestigation*</em>
     * and <em>act.patientDocument*</em> acts
     * to the <em>act.patientClinicalEvent</em> associated with each act's
     * patient and the specified date. The event is obtained via
     * {@link #getEvent(IMObjectReference, Date)}.
     * If no event exists, one will be created. If a relationship exists, it
     * will be ignored.
     *
     * @param acts the acts to add
     * @param date the event date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToHistoricalEvents(List<Act> acts, Date date) {
        Map<IMObjectReference, List<Act>> map = getByPatient(acts);
        for (Map.Entry<IMObjectReference, List<Act>> entry : map.entrySet()) {
            IMObjectReference patient = entry.getKey();
            Act event = getEvent(patient, date);
            if (event == null) {
                event = createEvent(patient, date, null);
            }
            boolean save = false;
            ActBean bean = new ActBean(event, service);
            for (Act a : entry.getValue()) {
                if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, a)) {
                    bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, a);
                    save = true;
                }
            }
            if (save) {
                bean.save();
            }
        }
    }

    /**
     * Returns an <em>act.patientClinicalEvent</em> for the specified patient.
     *
     * @param patient the patient
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         <tt>null</tt> if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(Party patient) {
        return getEvent(patient.getObjectReference());
    }

    /**
     * Returns the most recent <em>act.patientClinicalEvent</em> for the
     * specified patient.
     *
     * @param patient the patient
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         <tt>null</tt> if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(IMObjectReference patient) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT, true, true);
        query.add(new CollectionNodeConstraint("patient").add(new ObjectRefNodeConstraint("entity", patient)));
        query.add(new NodeSortConstraint(START_TIME, false));
        query.setMaxResults(1);
        QueryIterator<Act> iter = new IMObjectQueryIterator<Act>(service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Returns an <em>act.patientClinicalEvent</em> for the specified patient
     * and date.
     *
     * @param patient the patient
     * @param date    the date
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         <tt>null</tt> if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(Party patient, Date date) {
        return getEvent(patient.getObjectReference(), date);
    }

    /**
     * Returns an <em>act.patientClinicalEvent<em> for the specified patient
     * reference and date.
     * NOTE: this method will return the closest matching event for the
     * specified date, ignoring any time component if there is no exact match.
     *
     * @param patient the patient reference
     * @param date    the date
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         <tt>null</tt> if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(IMObjectReference patient, Date date) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT, true, true);
        query.add(new CollectionNodeConstraint("patient").add(
                new ObjectRefNodeConstraint("entity", patient)));
        OrConstraint or = new OrConstraint();
        Date lowerBound = DateRules.getDate(date);
        Date upperBound = getEndTime(date);
        or.add(new NodeConstraint(START_TIME, RelationalOp.LTE, lowerBound));
        or.add(new NodeConstraint(START_TIME, RelationalOp.BTW, lowerBound, upperBound));
        query.add(or);
        OrConstraint or2 = new OrConstraint();
        or2.add(new NodeConstraint(END_TIME, RelationalOp.IS_NULL));
        or2.add(new NodeConstraint(END_TIME, RelationalOp.GTE, getEndTime(date)));
        or2.add(new NodeConstraint(END_TIME, RelationalOp.BTW, lowerBound, upperBound));
        query.add(or2);
        query.add(new NodeSortConstraint(START_TIME));
        QueryIterator<Act> iter = new IMObjectQueryIterator<Act>(service, query);
        Act result = null;
        while (iter.hasNext()) {
            Act event = iter.next();
            Date eventStart = new Date(event.getActivityStartTime().getTime());
            if (result == null || eventStart.compareTo(date) <= 0) {
                result = event;
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Creates a new <em>act.patientClinicalEvent</em> for the patient and start time.
     *
     * @param patient   the patient reference
     * @param startTime the event start time
     * @param clinician the clinician reference. May be <tt>null</tt>
     * @return a new event
     */
    private Act createEvent(IMObjectReference patient, Date startTime, IMObjectReference clinician) {
        Act event;
        event = (Act) service.create(PatientArchetypes.CLINICAL_EVENT);
        event.setActivityStartTime(startTime);
        ActBean eventBean = new ActBean(event, service);
        eventBean.addNodeParticipation("patient", patient);
        if (clinician != null) {
            eventBean.addNodeParticipation("clinician", clinician);
        }
        return event;
    }

    /**
     * Recursively deletes an act heirarchy, from the top down.
     *
     * @param act the act to delete
     * @throws ArchetypeServiceException for any error
     */
    private void delete(Act act) {
        service.remove(act);
        for (ActRelationship relationship :
                act.getSourceActRelationships()) {
            Act child = get(relationship.getTarget());
            if (child != null) {
                delete(child);
            }
        }
    }

    /**
     * Returns a map of acts keyed on their associated patient reference.
     * If an act has an <em>actRelationship.patientClinicalEventItem</em>
     * it will be ignored.
     *
     * @param acts the acts
     * @return the acts keyed on patient reference
     */
    private Map<IMObjectReference, List<Act>> getByPatient(List<Act> acts) {
        Map<IMObjectReference, List<Act>> result = new HashMap<IMObjectReference, List<Act>>();
        for (Act act : acts) {
            ActBean bean = new ActBean(act, service);
            List<ActRelationship> relationships = bean.getRelationships(PatientArchetypes.CLINICAL_EVENT_ITEM);
            if (relationships.isEmpty()) {
                IMObjectReference patient = bean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION);
                if (patient != null) {
                    List<Act> list = result.get(patient);
                    if (list == null) {
                        list = new ArrayList<Act>();
                    }
                    list.add(act);
                    result.put(patient, list);
                }
            }
        }
        return result;
    }

    /**
      * Adds a list of <em>act.patientMedication</em>,
      * <em>act.patientInvestigation*</em> and <em>act.patientDocument*</em> acts
      * to an <em>act.patientClinicalEvent</em> associated with each act's
      * patient.
      *
      * @param acts      the acts to add
      * @param startTime the startTime used to select the event
      * @param events    the cache of events keyed on patient reference
      * @return the changed acts
      */
     protected Set<Act> addToEvents(List<Act> acts, Date startTime, Map<IMObjectReference, List<Act>> events) {
         Map<IMObjectReference, List<Act>> map = getByPatient(acts);
         Set<Act> changed = new HashSet<Act>();
         for (Map.Entry<IMObjectReference, List<Act>> entry : map.entrySet()) {
             IMObjectReference patient = entry.getKey();
             Act event = getEventForAddition(events, patient, startTime, getClinician(entry.getValue()));
             ActBean bean = new ActBean(event, service);
             for (Act act : entry.getValue()) {
                 if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, act)) {
                     bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, act);
                     changed.add(event);
                     changed.add(act);
                 }
             }
         }
         return changed;
     }

    /**
     * Returns an <em>act.patientClinicalEvent</em> that may have acts added.
     * <p/>
     * The <em>act.patientClinicalEvent</em> is selected as follows:
     * <ol>
     * <li>find the event intersecting the start time for the patient
     * <li>if it is an <em>IN_PROGRESS</em> and
     * <pre>event.startTime &gt;= (startTime - 1 week)</pre>
     * use it
     * <li>if it is <em>COMPLETED</em> and
     * <pre>startTime &gt;= event.startTime && startTime <= event.endTime</pre>
     * use it; otherwise
     * <li>create a new event, with <em>COMPLETED</em> status and startTime
     * </ol>
     *
     * @param events    the cache of events keyed on patient reference
     * @param patient   the patient to use
     * @param timestamp the time to select the event
     * @param clinician the clinician to use when creating new events. May be <tt>null</tt>
     * @return an event
     */
    private Act getEventForAddition(Map<IMObjectReference, List<Act>> events, IMObjectReference patient, Date timestamp,
                                    IMObjectReference clinician) {
        List<Act> patientEvents = events.get(patient);
        Act result = null;
        if (patientEvents == null) {
            patientEvents = new ArrayList<Act>();
            events.put(patient, patientEvents);
        } else {
            Date lowerBound = DateRules.getDate(timestamp);
            Date upperBound = getEndTime(timestamp);

            for (Act event : patientEvents) {
                Date startTime = event.getActivityStartTime();
                Date endTime = event.getActivityEndTime();
                if ((DateRules.compareTo(startTime, lowerBound) <= 0
                     || DateRules.between(startTime, lowerBound, upperBound))
                    || (endTime == null
                        || DateRules.compareTo(endTime, upperBound) >= 0
                        || DateRules.between(endTime, lowerBound, upperBound))) {
                    if (result == null || DateRules.compareTo(startTime, timestamp) <= 0) {
                        result = event;
                    } else {
                        break;
                    }
                }
            }
        }
        if (result == null) {
            result = getEvent(patient, timestamp); // hit the database
            if (result != null) {
                patientEvents.add(result);
            }
        }
        if (result != null && !canAddToEvent(result, timestamp)) {
            result = null;
        }
        if (result == null) {
            result = createEvent(patient, timestamp, clinician);
            result.setStatus(ActStatus.COMPLETED);
            patientEvents.add(result);
        }
        Collections.sort(patientEvents, new Comparator<Act>() {
            public int compare(Act o1, Act o2) {
                return DateRules.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime());
            }
        });

        return result;
    }

    /**
     * Determines if an act can be added to an event.
     *
     * @param event     the event
     * @param startTime the act start time
     * @return <tt>true</tt> if the event can be added to, otherwise <tt>false</tt>
     */
    private boolean canAddToEvent(Act event, Date startTime) {
        boolean result = true;
        // get date component of start time so not comparing time components
        Date startDate = DateRules.getDate(startTime);
        // get date component of event start and end datetimes

        Date eventStart = DateRules.getDate(event.getActivityStartTime());
        Date eventEnd = DateRules.getDate(event.getActivityEndTime());
        if (ActStatus.IN_PROGRESS.equals(event.getStatus())) {
            Date date = DateRules.getDate(startDate, -1, DateUnits.WEEKS);
            if (eventStart.before(date)) {
                result = false; // need to create a new event
            }
        } else {  // COMPLETED
            if (startDate.before(eventStart)
                || (eventEnd != null && startDate.after(eventEnd))
                || (eventEnd == null && startDate.after(eventStart))) {
                result = false; // need to create a new event
            }
        }
        return result;
    }

    /**
     * Returns a new date-time comprising the date of the specified date-time
     * and the time 23:59:59.
     *
     * @param dateTime the date-time
     * @return the new date-time
     */
    private Date getEndTime(Date dateTime) {
        if (dateTime == null) {
            return null;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * Returns an act given its reference.
     *
     * @param ref a reference to the object
     * @return the object corresponding to <code>ref</code> or null if none
     *         is found
     * @throws ArchetypeServiceException for any error
     */
    private Act get(IMObjectReference ref) {
        return (Act) service.get(ref);
    }

    /**
     * Returns the first clinician found in a Collection of Acts.
     *
     * @param acts a collection of Acts
     * @return the clinician, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any error
     */
    private IMObjectReference getClinician(Collection<Act> acts) {
        for (Act act : acts) {
            ActBean bean = new ActBean(act, service);
            IMObjectReference clinician = bean.getParticipantRef(UserArchetypes.CLINICIAN_PARTICIPATION);
            if (clinician != null) {
                return clinician;
            }
        }
        return null;
    }

    /**
     * Returns the valid short names for an event item relationship.
     *
     * @return the short names
     */
    private String[] getClinicalEventItems() {
        if (clinicalEventItems == null) {
            clinicalEventItems = DescriptorHelper.getNodeShortNames(PatientArchetypes.CLINICAL_EVENT_ITEM, "target",
                                                                    service);
        }
        return clinicalEventItems;
    }
}
