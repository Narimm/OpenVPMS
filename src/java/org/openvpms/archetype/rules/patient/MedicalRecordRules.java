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

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
     * Clinicial event item act relationship archetype short name.
     */
    private static final String CLINICAL_EVENT_ITEM
            = "actRelationship.patientClinicalEventItem";

    /**
     * Clinical event archetype short name.
     */
    private static final String CLINICAL_EVENT = "act.patientClinicalEvent";

    /**
     * Patient participation short name.
     */
    private static final String PARTICIPATION_PATIENT = "participation.patient";

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
        if (TypeHelper.isA(act, CLINICAL_EVENT)) {
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
     * to the <em>act.patientClinicalEvent</em> associated with the patient and
     * date. If no event exists, one will be created.
     *
     * @param act  the act to add
     * @param date the event date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToEvent(Act act, Date date) {
        addToEvents(Arrays.asList(act), date);
    }

    /**
     * Adds a list of
     * <em>act.patientMedication</em> <em>act.patientInvestigation*</em> acts
     * to the <em>act.patientClinicalEvent</em> associated with each act's
     * patient and the specified date. If no event exists, one will be created.
     *
     * @param acts the acts to add
     * @param date the event date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToEvents(List<Act> acts, Date date) {
        Map<IMObjectReference, List<Act>> map = getByPatient(acts);
        for (Map.Entry<IMObjectReference, List<Act>> entry : map.entrySet()) {
            IMObjectReference patient = entry.getKey();
            Act event = getEvent(patient, date);
            if (event == null) {
                event = (Act) service.create(CLINICAL_EVENT);
                event.setActivityStartTime(date);
                ActBean eventBean = new ActBean(event, service);
                eventBean.addParticipation(PARTICIPATION_PATIENT, patient);
            }
            ActBean bean = new ActBean(event, service);
            for (Act a : entry.getValue()) {
                bean.addRelationship(CLINICAL_EVENT_ITEM, a);
            }
            bean.save();
        }
    }

    /**
     * Returns an <em>act.patientClinicalEvent</em> for the specified patient
     * and date.
     *
     * @param patient the patient
     * @param date    the date
     * @return the corresponding <em>act.patientClinicalEvent</em> or
     *         <tt>null</tt> if none is found
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
     *         <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getEvent(IMObjectReference patient, Date date) {
        ArchetypeQuery query = new ArchetypeQuery(CLINICAL_EVENT,
                                                  true, true);
        query.add(new CollectionNodeConstraint("patient").add(
                new ObjectRefNodeConstraint("entity", patient)));
        OrConstraint or = new OrConstraint();
        Date lowerBound = getDate(date);
        Date upperBound = getEndTime(date);
        or.add(new NodeConstraint(START_TIME, RelationalOp.LTE,
                                  lowerBound));
        or.add(new NodeConstraint(START_TIME, RelationalOp.BTW,
                                  lowerBound, upperBound));
        query.add(or);
        OrConstraint or2 = new OrConstraint();
        or2.add(new NodeConstraint(END_TIME, RelationalOp.IsNULL));
        or2.add(new NodeConstraint(END_TIME, RelationalOp.GTE,
                                   getEndTime(date)));
        or2.add(new NodeConstraint(END_TIME, RelationalOp.BTW,
                                   lowerBound, upperBound));
        query.add(or2);
        query.add(new NodeSortConstraint(START_TIME));
        QueryIterator<Act> iter
                = new IMObjectQueryIterator<Act>(service, query);
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
        Map<IMObjectReference, List<Act>> result
                = new HashMap<IMObjectReference, List<Act>>();
        for (Act act : acts) {
            ActBean bean = new ActBean(act, service);
            List<ActRelationship> relationships
                    = bean.getRelationships(CLINICAL_EVENT_ITEM);
            if (relationships.isEmpty()) {
                IMObjectReference patient
                        = bean.getParticipantRef(PARTICIPATION_PATIENT);
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
     * Returns the date part of a date-time, zero-ing out any time
     * component.
     *
     * @param dateTime the date-time
     * @return the date part of <tt>dateTime</tt>
     */
    private Date getDate(Date dateTime) {
        return DateUtils.truncate(dateTime, Calendar.DAY_OF_MONTH);
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
        return (Act) ArchetypeQueryHelper.getByObjectReference(service, ref);
    }

}
