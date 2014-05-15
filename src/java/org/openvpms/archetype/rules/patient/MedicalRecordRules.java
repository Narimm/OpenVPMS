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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Patient medical record rules.
 *
 * @author Tim Anderson
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
     * Creates a {@link MedicalRecordRules}.
     *
     * @param service the archetype service
     */
    public MedicalRecordRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Adds an <em>act.patientClinicalNote</em> to an <em>act.patientClinicalEvent</em>.
     *
     * @param event     the event
     * @param startTime the start time for the note
     * @param note      the note
     * @param clinician the clinician. May be {@code null}
     * @param author    the author. May be {@code null}
     * @return the note act
     */
    public Act addNote(Act event, Date startTime, String note, User clinician, User author) {
        Act act = (Act) service.create(PatientArchetypes.CLINICAL_NOTE);
        ActBean bean = new ActBean(act, service);
        ActBean eventBean = new ActBean(event, service);
        bean.setValue("startTime", startTime);
        IMObjectReference patient = eventBean.getNodeParticipantRef("patient");
        if (patient != null) {
            bean.addNodeParticipation("patient", patient);
        }
        if (author != null) {
            bean.addNodeParticipation("author", author);
        }
        if (clinician != null) {
            bean.addNodeParticipation("clinician", clinician);
        }
        bean.setValue("note", note);
        eventBean.addNodeRelationship("items", act);
        service.save(Arrays.asList(event, act));
        return act;
    }

    /**
     * Adds an <em>act.patientMedication</em>, <em>act.patientInvestigation*</em> or
     * <em>act.customerAccountInvoiceItem</em> to an <em>act.patientClinicalEvent</em> associated with the act's
     * patient.
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
     * Links a patient medical child record to their parent act.
     * If the child is an <em>act.patientClinicalProblem</em>, all of its items will be also be linked to the event.
     *
     * @param parent the parent act. An <em>act.patientClinicalEvent</em> or <em>>act.patientClinicalProblem</em>
     * @param child  the child act
     */
    public void linkMedicalRecords(Act parent, Act child) {
        if (TypeHelper.isA(child, PatientArchetypes.CLINICAL_PROBLEM)) {
            linkMedicalRecords(parent, child, null);
        } else {
            linkMedicalRecords(parent, null, child);
        }
    }

    /**
     * Links a patient medical record to an <em>act.patientClinicalEvent</em>,
     * and optionally an <em>act.patientClinicalProblem</em>, if no relationship exists.
     * <p/>
     * If {@code problem} is specified:
     * <ul>
     * <li>it will be linked to the event, if no relationship exists
     * <li>any of its items not presently linked to the event will be linked
     * </ul>
     *
     * @param event   the <em>act.patientClinicalEvent</em>. May be {@code null}
     * @param problem the <em>act.patientClinicalProblem</em>. May be {@code null}
     * @param item    the patient medical record or charge item. May be {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void linkMedicalRecords(Act event, Act problem, Act item) {
        if (event != null && !TypeHelper.isA(event, PatientArchetypes.CLINICAL_EVENT)) {
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
        ActBean bean = (event != null) ? new ActBean(event, service) : null;

        if (problem != null && item != null) {
            linkItemToProblem(problem, item);
        }

        if (event != null && item != null) {
            linkItemToEvent(bean, item);
        }

        if (event != null && problem != null) {
            linkProblemToEvent(bean, problem);
        }
    }

    /**
     * Adds a list of <em>act.patientMedication</em>, <em>act.patientInvestigation*</em>,
     * <em>act.patientDocument*</em> and <em>act.customerAccountInvoiceItem</em> acts to an
     * <em>act.patientClinicalEvent</em> associated with each act's patient.
     * <p/>
     * The <em>act.patientClinicalEvent</em> is selected using {@link #getEventForAddition}.
     *
     * @param acts      the acts to add
     * @param startTime the startTime used to select the event
     */
    public void addToEvents(List<Act> acts, Date startTime) {
        PatientHistoryChanges changes = new PatientHistoryChanges(null, null, service);
        addToEvents(acts, startTime, changes);
        changes.save();
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
     * <li>create a new event, with <em>IN_PROGRESS</em> status and startTime
     * </ol>
     *
     * @param patient   the patient
     * @param startTime the start time
     * @param clinician the clinician. May be {@code null}
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
     * <li>create a new event, with <em>IN_PROGRESS</em> status and startTime
     * </ol>
     *
     * @param patient   the patient
     * @param startTime the start time
     * @param clinician the clinician. May be {@code null}
     * @return an event. May be newly created
     */
    public Act getEventForAddition(IMObjectReference patient, Date startTime, IMObjectReference clinician) {
        PatientHistoryChanges events = new PatientHistoryChanges(null, null, service);
        return getEventForAddition(events, patient, startTime, clinician);
    }

    /**
     * Creates a new <em>act.patientClinicalEvent</em> for the patient and start time.
     *
     * @param patient   the patient
     * @param startTime the event start time
     * @param clinician the clinician. May be {@code null}
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
                event.setStatus(ActStatus.COMPLETED);
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
     * @return the corresponding <em>act.patientClinicalEvent</em> or {@code null} if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED</em>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(Party patient) {
        return getEvent(patient.getObjectReference());
    }

    /**
     * Returns the most recent <em>act.patientClinicalEvent</em> for the specified patient.
     *
     * @param patient the patient
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         {@code null} if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(IMObjectReference patient) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT, true, true);
        query.add(new CollectionNodeConstraint("patient").add(new ObjectRefNodeConstraint("entity", patient)));
        query.add(new NodeSortConstraint(START_TIME, false));

        // ensure that for events with the same start time, the one with the highest id is used
        query.add(new NodeSortConstraint("id", false));

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
     *         {@code null} if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(Party patient, Date date) {
        return getEvent(patient.getObjectReference(), date);
    }

    /**
     * Returns an <em>act.patientClinicalEvent<em> for the specified patient
     * reference and date.
     * NOTES:
     * <ul>
     * <li>this method will return the closest matching event for the specified date, ignoring any time component if
     * there is no exact match.</li>
     * <li>if two events have the same timestamp, the event with the smaller id will be returned</li>
     * </ul>
     *
     * @param patient the patient reference
     * @param date    the date
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         {@code null} if none is found. The event may be
     *         <em>IN_PROGRESS</em> or <em>COMPLETED}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(IMObjectReference patient, Date date) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT, true, true);
        query.add(new CollectionNodeConstraint("patient").add(
                new ObjectRefNodeConstraint("entity", patient)));
        Date lowerBound = DateRules.getDate(date);
        Date upperBound = getEndTime(date);
        query.add(Constraints.lte(START_TIME, upperBound));
        query.add(Constraints.or(Constraints.gte(END_TIME, lowerBound), Constraints.isNull(END_TIME)));
        query.add(new NodeSortConstraint(START_TIME));
        query.add(new NodeSortConstraint("id"));
        QueryIterator<Act> iter = new IMObjectQueryIterator<Act>(service, query);
        Act result = null;
        long resultDistance = 0;
        while (iter.hasNext()) {
            Act event = iter.next();
            if (result == null) {
                resultDistance = distance(date, event);
                result = event;
            } else {
                long distance = distance(date, event);
                if (distance < resultDistance) {
                    resultDistance = distance;
                    result = event;
                }
            }
        }
        return result;
    }

    /**
     * Creates a new <em>act.patientClinicalEvent</em> for the patient and start time.
     *
     * @param patient   the patient reference
     * @param startTime the event start time
     * @param clinician the clinician reference. May be {@code null}
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
     * Returns a map of acts keyed on their associated patient reference.
     *
     * @param acts the acts
     * @return the acts keyed on patient reference
     */
    private Map<IMObjectReference, List<Act>> getByPatient(List<Act> acts) {
        Map<IMObjectReference, List<Act>> result = new HashMap<IMObjectReference, List<Act>>();
        for (Act act : acts) {
            ActBean bean = new ActBean(act, service);
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
        return result;
    }

    /**
     * Adds a list of <em>act.patientMedication</em>, <em>act.patientInvestigation*</em>, <em>act.patientDocument*</em>,
     * and <em>act.customerAccountInvoiceItem</em> acts to an <em>act.patientClinicalEvent</em> associated with each
     * act's patient.
     * <p/>
     * If an act already has a relationship, but it belongs to a different patient, the relationship will be removed
     * and a relationship to the patient's own event added.
     *
     * @param acts      the acts to add
     * @param startTime the startTime used to select the event
     * @param changes   the changes to patient history
     */
    protected void addToEvents(List<Act> acts, Date startTime, PatientHistoryChanges changes) {
        Map<IMObjectReference, List<Act>> map = getByPatient(acts);
        for (Map.Entry<IMObjectReference, List<Act>> entry : map.entrySet()) {
            IMObjectReference patient = entry.getKey();
            List<Act> unlinked = new ArrayList<Act>(); // the acts to link to events
            for (Act act : entry.getValue()) {
                Act existingEvent = changes.getLinkedEvent(act);
                if (existingEvent != null) {
                    // the act is already linked to an event
                    if (!ObjectUtils.equals(changes.getPatient(existingEvent), patient)) {
                        // the existing event is for a different patient. Need to unlink this.
                        changes.removeRelationship(existingEvent, act);
                        unlinked.add(act);
                    }
                } else {
                    unlinked.add(act);
                }
            }
            if (!unlinked.isEmpty()) {
                Act event = getEventForAddition(changes, patient, startTime, getClinician(unlinked));
                addToEvent(event, unlinked, changes);
            }
        }
    }

    /**
     * Adds acts to an event, where no relationship exists.
     *
     * @param event   the event
     * @param acts    the acts to add
     * @param changes tracks changes to the patient history
     */
    protected void addToEvent(Act event, List<Act> acts, PatientHistoryChanges changes) {
        changes.addEvent(event);
        for (Act act : acts) {
            if (!changes.hasRelationship(act)) {
                changes.addRelationship(event, act);
            }
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
     * <li>create a new event, with <em>IN_PROGRESS</em> status and startTime
     * </ol>
     *
     * @param events    the cache of events
     * @param patient   the patient to use
     * @param timestamp the time to select the event
     * @param clinician the clinician to use when creating new events. May be {@code null}
     * @return an event
     */
    private Act getEventForAddition(PatientHistoryChanges events, IMObjectReference patient, Date timestamp,
                                    IMObjectReference clinician) {
        List<Act> patientEvents = events.getEvents(patient);
        Act result = null;
        if (patientEvents != null) {
            Date lowerBound = DateRules.getDate(timestamp);
            Date upperBound = getEndTime(timestamp);
            long resultDistance = 0;

            for (Act event : patientEvents) {
                if (DateRules.intersects(event.getActivityStartTime(), event.getActivityEndTime(), lowerBound,
                                         upperBound)) {
                    if (result == null) {
                        resultDistance = distance(timestamp, event);
                        result = event;
                    } else {
                        long distance = distance(timestamp, event);
                        if (distance < resultDistance) {
                            resultDistance = distance;
                            result = event;
                        }
                    }
                }
            }
        }
        if (result == null) {
            result = getEvent(patient, timestamp); // hit the database
            if (result != null) {
                events.addEvent(result);
            }
        }
        if (result != null && !canAddToEvent(result, timestamp)) {
            result = null;
        }
        if (result == null) {
            result = createEvent(patient, timestamp, clinician);
            events.addEvent(result);
        }
        return result;
    }

    /**
     * Determines if an act can be added to an event.
     *
     * @param event     the event
     * @param startTime the act start time
     * @return {@code true} if the event can be added to, otherwise {@code false}
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
     * Returns the first clinician found in a Collection of Acts.
     *
     * @param acts a collection of Acts
     * @return the clinician, or {@code null} if none is found
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

    /**
     * Calculates the distance of a date to an event.
     * <p/>
     * The distance is the minimum difference between the date and the event's start or end times, ignoring
     * seconds.
     *
     * @param date  the date
     * @param event the event
     * @return the the minimum difference between the date and the event's start or end times, ignoring seconds.
     */
    private long distance(Date date, Act event) {
        long dateSecs = getSeconds(date);          // truncate milliseconds are not stored in db
        long startTime = getSeconds(event.getActivityStartTime());
        long endTime = (event.getActivityEndTime() != null) ? getSeconds(event.getActivityEndTime()) : 0;
        long distStartTime = Math.abs(startTime - dateSecs);
        if (endTime != 0) {
            return distStartTime;
        }
        long distEndTime = Math.abs(endTime - dateSecs);
        return distStartTime < distEndTime ? distStartTime : distEndTime;
    }

    /**
     * Returns a date in seconds.
     *
     * @param date the date
     * @return the date in seconds
     */
    private long getSeconds(Date date) {
        return date.getTime() / 1000;
    }


    /**
     * Links an item to an <em>act.patientClinicalProblem</em>, if no relationship currently exists.
     *
     * @param problem the problem
     * @param item    the item to link
     */
    private void linkItemToProblem(Act problem, Act item) {
        // link the problem and item if required
        ActBean problemBean = new ActBean(problem, service);
        if (!problemBean.hasRelationship(PatientArchetypes.CLINICAL_PROBLEM_ITEM, item)) {
            problemBean.addNodeRelationship("items", item);
            service.save(Arrays.asList(problem, item));
        }
    }

    /**
     * Links an item to an <em>act.patientClinicalEvent</em>, if no relationship currently exists.
     *
     * @param bean the event
     * @param item the item to link
     */
    private void linkItemToEvent(ActBean bean, Act item) {
        Act event = bean.getAct();
        // link the event and item
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM, item)) {
                bean.addNodeRelationship("chargeItems", item);
                service.save(Arrays.asList(event, item));
            }
        } else if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, item)) {
            bean.addNodeRelationship("items", item);
            service.save(Arrays.asList(event, item));
        }
    }

    /**
     * Links an <em>act.patientClinicalProblem> and its acts to an <em>act.patientClinicalEvent</em>,
     * if no relationship currently exists.
     *
     * @param bean    the event
     * @param problem the problem to link
     */
    private void linkProblemToEvent(ActBean bean, Act problem) {
        Act event = bean.getAct();
        // link the event and problem
        ActBean problemBean = new ActBean(problem, service);
        List<Act> toSave = new ArrayList<Act>();

        // if the problem is not linked to the event, add it
        if (!bean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem)) {
            bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem);
            toSave.add(event);
        }

        // add each of the problem's child acts not linked to an event
        List<Act> acts = problemBean.getNodeActs("items");
        if (!acts.isEmpty()) {
            String[] shortNames = getClinicalEventItems();
            for (Act child : acts) {
                ActBean childBean = new ActBean(child, service);
                if (childBean.isA(shortNames) && !childBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM)) {
                    bean.addRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, child);
                    toSave.add(child);
                }
            }
        }
        if (!toSave.isEmpty()) {
            service.save(toSave);
        }
    }

}
