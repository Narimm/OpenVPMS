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

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.act.ActRelationship;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Predicates;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.model.party.Party;
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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;


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
     * Clinical problem item short names.
     */
    private String[] clinicalProblemItems;

    /**
     * Medical records that require locking.
     */
    private String[] lockableRecords;

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
     * Creates a clinical note for a patient.
     * <p/>
     * The note is not saved.
     *
     * @param startTime the start time for the note
     * @param patient   the patient
     * @param note      the note
     * @param clinician the clinician. May be {@code null}
     * @param author    the author. May be {@code null}
     * @return a new note
     */
    public Act createNote(Date startTime, Party patient, String note, User clinician, User author) {
        Act act = (Act) service.create(PatientArchetypes.CLINICAL_NOTE);
        IMObjectBean bean = service.getBean(act);
        bean.setValue("startTime", startTime);
        bean.setTarget("patient", patient);
        bean.setValue("note", note);
        if (author != null) {
            bean.setTarget("author", author);
        }
        if (clinician != null) {
            bean.setTarget("clinician", clinician);
        }
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
        addToEvents(Collections.singletonList(act), startTime);
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
            linkMedicalRecords(parent, child, null, null);
        } else if (!TypeHelper.isA(child, PatientArchetypes.CLINICAL_ADDENDUM)) {
            linkMedicalRecords(parent, null, child, null);
        } else {
            linkMedicalRecords(parent, null, null, child);
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
     * @param event    the <em>act.patientClinicalEvent</em>. May be {@code null}
     * @param problem  the <em>act.patientClinicalProblem</em>. May be {@code null}
     * @param item     the patient medical record or charge item. May be {@code null}
     * @param addendum the addendum. If specified, the item must be a note or medication. May be {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void linkMedicalRecords(Act event, Act problem, Act item, Act addendum) {
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
        if (addendum != null) {
            if (!TypeHelper.isA(addendum, PatientArchetypes.CLINICAL_ADDENDUM)) {
                throw new IllegalArgumentException("Argument 'addendum' is of the wrong type: "
                                                   + addendum.getArchetypeId().getShortName());
            }
        }
        Set<Act> changed = new HashSet<>();
        if (item != null && addendum != null) {
            linkAddendumToItem(item, addendum, changed);
        }
        if (problem != null) {
            IMObjectBean bean = service.getBean(problem);
            if (item != null && TypeHelper.isA(item, getClinicalProblemItems())) {
                linkItemToProblem(bean, item, changed);
            }
            if (addendum != null) {
                linkItemToProblem(bean, addendum, changed);
            }
        }
        if (event != null) {
            IMObjectBean bean = service.getBean(event);
            if (item != null && (item.isA(CustomerAccountArchetypes.INVOICE_ITEM)
                                 || item.isA(getClinicalEventItems()))) {
                linkItemToEvent(bean, item, changed);
            }
            if (addendum != null) {
                linkItemToEvent(bean, addendum, changed);
            }
        }
        if (!changed.isEmpty()) {
            service.save(changed);
        }

        if (problem != null) {
            linkProblemToEvent(event, problem);
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
    public Act getEventForAddition(Reference patient, Date startTime, Reference clinician) {
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
        Reference clinicianRef = (clinician != null) ? clinician.getObjectReference() : null;
        return createEvent(patient.getObjectReference(), startTime, clinicianRef);
    }

    /**
     * Adds a list of <em>act.patientMedication</em>, <em>act.patientInvestigation*</em>
     * and <em>act.patientDocument*</em> acts to the <em>act.patientClinicalEvent</em> associated with each act's
     * patient and the specified date. The event is obtained via {@link #getEvent(Reference, Date)}.
     * If no event exists, one will be created. If a relationship exists, it
     * will be ignored.
     *
     * @param acts the acts to add
     * @param date the event date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToHistoricalEvents(List<Act> acts, Date date) {
        Map<Reference, List<Act>> map = getByPatient(acts);
        for (Map.Entry<Reference, List<Act>> entry : map.entrySet()) {
            Reference patient = entry.getKey();
            Act event = getEvent(patient, date);
            if (event == null) {
                event = createEvent(patient, date, null);
                event.setStatus(ActStatus.COMPLETED);
            }
            boolean save = false;
            IMObjectBean bean = service.getBean(event);
            for (Act a : entry.getValue()) {
                if (bean.getValue("items", Relationship.class, Predicates.targetEquals(a)) == null) {
                    ActRelationship relationship = (ActRelationship) bean.addTarget("items", a);
                    a.addActRelationship(relationship);
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
     * <em>IN_PROGRESS</em> or <em>COMPLETED</em>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(org.openvpms.component.model.party.Party patient) {
        return getEvent(patient.getObjectReference());
    }

    /**
     * Returns the most recent <em>act.patientClinicalEvent</em> for the specified patient.
     *
     * @param patient the patient
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     * {@code null} if none is found. The event may be
     * <em>IN_PROGRESS</em> or <em>COMPLETED}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(Reference patient) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT, true, true);
        query.add(new CollectionNodeConstraint("patient").add(new ObjectRefNodeConstraint("entity", patient)));
        query.add(new NodeSortConstraint(START_TIME, false));

        // ensure that for events with the same start time, the one with the highest id is used
        query.add(new NodeSortConstraint("id", false));

        query.setMaxResults(1);
        QueryIterator<Act> iter = new IMObjectQueryIterator<>(service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Returns an <em>act.patientClinicalEvent</em> for the specified patient
     * and date.
     *
     * @param patient the patient
     * @param date    the date
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     * {@code null} if none is found. The event may be
     * <em>IN_PROGRESS</em> or <em>COMPLETED}
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
     * {@code null} if none is found. The event may be
     * <em>IN_PROGRESS</em> or <em>COMPLETED}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(Reference patient, Date date) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT, true, true);
        query.add(new CollectionNodeConstraint("patient").add(
                new ObjectRefNodeConstraint("entity", patient)));
        Date lowerBound = DateRules.getDate(date);
        Date upperBound = getEndTime(date);
        query.add(Constraints.lte(START_TIME, upperBound));
        query.add(Constraints.or(Constraints.gte(END_TIME, lowerBound), Constraints.isNull(END_TIME)));
        query.add(new NodeSortConstraint(START_TIME));
        query.add(new NodeSortConstraint("id"));
        QueryIterator<Act> iter = new IMObjectQueryIterator<>(service, query);
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
     * Determines if a medical record needs to be locked, given a period relative to the current time.
     *
     * @param act    the record act
     * @param period the period prior to the current from which medical records should be locked
     * @return {@code true} if the record needs locking
     */
    public boolean needsLock(Act act, Period period) {
        return needsLock(act, new DateTime().minus(period).toDate());
    }

    /**
     * Determines if a medical record needs to be locked.
     *
     * @param act      the record act
     * @param lockTime the time that medical records should be locked on or prior to
     * @return {@code true} if the record needs locking
     */
    public boolean needsLock(Act act, Date lockTime) {
        return !ActStatus.POSTED.equals(act.getStatus())
               && TypeHelper.isA(act, getLockableRecords())
               && DateRules.compareTo(act.getActivityStartTime(), lockTime) <= 0;
    }

    /**
     * Returns the archetype short names of patient medical records that may be locked.
     *
     * @return the archetype short names of patient medical records that may be locked
     */
    public synchronized String[] getLockableRecords() {
        if (lockableRecords == null) {
            List<String> items = new ArrayList<>(Arrays.asList(getClinicalEventItems()));
            items.remove(PatientArchetypes.CLINICAL_PROBLEM);
            Set<String> versionTargets = new HashSet<>();
            for (String item : items) {
                NodeDescriptor versions = DescriptorHelper.getNode(item, "versions", service);
                if (versions != null) {
                    for (String relationship : DescriptorHelper.getShortNames(versions, service)) {
                        String[] targets = DescriptorHelper.getNodeShortNames(relationship, "target", service);
                        versionTargets.addAll(Arrays.asList(targets));
                    }
                }
            }
            items.addAll(versionTargets);
            lockableRecords = items.toArray(new String[items.size()]);
        }
        return lockableRecords;
    }

    /**
     * Returns the most recent attachment with the specified file name associated with a patient clinical event.
     *
     * @param fileName the file name
     * @param event    the <em>act.patientClinicalEvent</em>
     * @return the attachment, or {@code null} if none is found
     */
    public DocumentAct getAttachment(String fileName, org.openvpms.component.model.act.Act event) {
        ArchetypeQuery query = createAttachmentQuery(fileName, event);
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Returns the most recent attachment with the specified file name and identity, associated with a patient clinical
     * event.
     *
     * @param fileName          the file name
     * @param event             the <em>act.patientClinicalEvent</em>
     * @param identityArchetype the identity archetype
     * @param identity          the identity
     * @return the attachment, or {@code null} if none is found
     */
    public DocumentAct getAttachment(String fileName, org.openvpms.component.model.act.Act event,
                                     String identityArchetype, String identity) {
        ArchetypeQuery query = createAttachmentQuery(fileName, event);
        query.add(Constraints.join("identities", shortName(identityArchetype)).add(eq("identity", identity)));
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
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
        Map<Reference, List<Act>> map = getByPatient(acts);
        for (Map.Entry<Reference, List<Act>> entry : map.entrySet()) {
            Reference patient = entry.getKey();
            List<Act> unlinked = new ArrayList<>(); // the acts to link to events
            for (Act act : entry.getValue()) {
                Act existingEvent = (Act) changes.getLinkedEvent(act);
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
     * Determines if an act can be added to an event.
     *
     * @param event     the event
     * @param startTime the act start time
     * @param changes   tracks changes to the patient history
     * @return {@code true} if the event can be added to, otherwise {@code false}
     */
    protected boolean canAddToEvent(Act event, Date startTime, PatientHistoryChanges changes) {
        boolean result = true;
        // get date component of start time so not comparing time components
        Date startDate = DateRules.getDate(startTime);
        // get date component of event start and end datetimes

        Date eventStart = DateRules.getDate(event.getActivityStartTime());
        Date eventEnd = DateRules.getDate(event.getActivityEndTime());
        if (ActStatus.IN_PROGRESS.equals(event.getStatus())) {
            if (!changes.isBoarding(event)) {
                Date date = DateRules.getDate(startDate, -1, DateUnits.WEEKS);
                if (eventStart.before(date)) {
                    result = false; // need to create a new event
                }
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
     * Creates a new <em>act.patientClinicalEvent</em> for the patient and start time.
     *
     * @param patient   the patient reference
     * @param startTime the event start time
     * @param clinician the clinician reference. May be {@code null}
     * @return a new event
     */
    private Act createEvent(Reference patient, Date startTime, Reference clinician) {
        Act event;
        event = (Act) service.create(PatientArchetypes.CLINICAL_EVENT);
        event.setActivityStartTime(startTime);
        IMObjectBean eventBean = service.getBean(event);
        eventBean.setTarget("patient", patient);
        if (clinician != null) {
            eventBean.setTarget("clinician", clinician);
        }
        return event;
    }

    /**
     * Returns a map of acts keyed on their associated patient reference.
     *
     * @param acts the acts
     * @return the acts keyed on patient reference
     */
    private Map<Reference, List<Act>> getByPatient(List<Act> acts) {
        Map<Reference, List<Act>> result = new HashMap<>();
        for (Act act : acts) {
            IMObjectBean bean = service.getBean(act);
            Reference patient = bean.getTargetRef("patient");
            if (patient != null) {
                List<Act> list = result.get(patient);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(act);
                result.put(patient, list);
            }
        }
        return result;
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
    private Act getEventForAddition(PatientHistoryChanges events, Reference patient, Date timestamp,
                                    Reference clinician) {
        List<org.openvpms.component.model.act.Act> patientEvents = events.getEvents(patient);
        Act result = null;
        if (patientEvents != null) {
            Date lowerBound = DateRules.getDate(timestamp);
            Date upperBound = getEndTime(timestamp);
            long resultDistance = 0;

            for (org.openvpms.component.model.act.Act event : patientEvents) {
                if (DateRules.intersects(event.getActivityStartTime(), event.getActivityEndTime(), lowerBound,
                                         upperBound)) {
                    if (result == null) {
                        resultDistance = distance(timestamp, event);
                        result = (Act) event;
                    } else {
                        long distance = distance(timestamp, event);
                        if (distance < resultDistance) {
                            resultDistance = distance;
                            result = (Act) event;
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
        if (result != null && !canAddToEvent(result, timestamp, events)) {
            result = null;
        }
        if (result == null) {
            result = createEvent(patient, timestamp, clinician);
            events.addEvent(result);
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
    private Reference getClinician(Collection<Act> acts) {
        for (Act act : acts) {
            IMObjectBean bean = service.getBean(act);
            Reference clinician = bean.getTargetRef("clinician");
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
    private synchronized String[] getClinicalEventItems() {
        if (clinicalEventItems == null) {
            clinicalEventItems = DescriptorHelper.getNodeShortNames(PatientArchetypes.CLINICAL_EVENT_ITEM, "target",
                                                                    service);
        }
        return clinicalEventItems;
    }

    /**
     * Returns the valid short names for a problem item relationship.
     *
     * @return the short names
     */
    private synchronized String[] getClinicalProblemItems() {
        if (clinicalProblemItems == null) {
            clinicalProblemItems = DescriptorHelper.getNodeShortNames(PatientArchetypes.CLINICAL_PROBLEM_ITEM, "target",
                                                                      service);
        }
        return clinicalProblemItems;
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
    private long distance(Date date, org.openvpms.component.model.act.Act event) {
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
     * @param bean    the problem
     * @param item    the item to link
     * @param changed collects the changed acts
     */
    private void linkItemToProblem(IMObjectBean bean, Act item, Set<Act> changed) {
        // link the problem and item if required
        if (addRelationship(bean, "items", item)) {
            changed.add((Act) bean.getObject());
            changed.add(item);
        }
    }

    /**
     * Links an item to an <em>act.patientClinicalEvent</em>, if no relationship currently exists.
     *
     * @param bean    the event
     * @param item    the item to link
     * @param changed collects the changed acts
     */
    private void linkItemToEvent(IMObjectBean bean, Act item, Set<Act> changed) {
        Act event = (Act) bean.getObject();
        // link the event and item
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (addRelationship(bean, "chargeItems", item)) {
                changed.add(event);
                changed.add(item);
            }
        } else if (addRelationship(bean, "items", item)) {
            changed.add(event);
            changed.add(item);
        }
    }

    /**
     * Adds a relationship between two acts, if none exists.
     *
     * @param bean   the source act bean
     * @param node   the relationship node
     * @param target the target act
     * @return {@code true} if the relationship was added
     */
    private boolean addRelationship(IMObjectBean bean, String node, Act target) {
        boolean result = false;
        if (bean.getValue(node, ActRelationship.class, Predicates.targetEquals(target)) == null) {
            ActRelationship relationship = (ActRelationship) bean.addTarget(node, target);
            target.addActRelationship(relationship);
            result = true;
        }
        return result;
    }

    /**
     * Links an <em>act.patientClinicalProblem> and its acts to an <em>act.patientClinicalEvent</em>,
     * if no relationship currently exists.
     *
     * @param event   the event
     * @param problem the problem to link
     */
    private void linkProblemToEvent(Act event, Act problem) {
        // link the event and problem
        IMObjectBean problemBean = service.getBean(problem);
        List<Act> toSave = new ArrayList<>();

        IMObjectBean bean = service.getBean(event);
        // if the problem is not linked to the event, add it
        if (addRelationship(bean, "items", problem)) {
            toSave.add(event);
        }

        // add each of the problem's child acts not linked to an event
        List<Act> acts = problemBean.getTargets("items", Act.class);
        if (!acts.isEmpty()) {
            String[] shortNames = getClinicalEventItems();
            for (Act child : acts) {
                IMObjectBean childBean = service.getBean(child);
                if (childBean.isA(shortNames) && addRelationship(bean, "items", child)) {
                    toSave.add(child);
                }
            }
        }
        if (!toSave.isEmpty()) {
            service.save(toSave);
        }
    }

    /**
     * Links an addendum to an <em>act.patientClinicalNote</em> or <em>act.patientMedication</em>, if no relationship
     * currently exists.
     *
     * @param item     the item
     * @param addendum the addendum to link
     */
    private void linkAddendumToItem(Act item, Act addendum, Set<Act> changed) {
        IMObjectBean bean = service.getBean(item);
        if (!addRelationship(bean, "addenda", addendum)) {
            changed.add(item);
            changed.add(addendum);
        }
    }

    /**
     * Creates a query to locate attachments with the specified file name, associated with an event.
     *
     * @param fileName the file name
     * @param event    the <em>act.patientClinicalEvent</em>
     * @return a new query
     */
    private ArchetypeQuery createAttachmentQuery(String fileName, org.openvpms.component.model.act.Act event) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.DOCUMENT_ATTACHMENT);
        query.add(join("event").add(eq("source", event)));
        query.add(eq("fileName", fileName));
        query.add(sort("startTime", false));
        query.add(sort("id", false));
        query.setMaxResults(1);
        return query;
    }

}
