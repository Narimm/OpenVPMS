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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Queries scheduled event acts, returning a limited set of data for display purposes.
 *
 * @author Tim Anderson
 */
abstract class ScheduleEventQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The schedule.
     */
    private Entity schedule;

    /**
     * The 'from' start time.
     */
    private Date from;

    /**
     * The 'to' start time.
     */
    private Date to;

    /**
     * Status lookup names, keyed on code.
     */
    private final Map<String, String> statusNames;

    /**
     * Reason lookup names, keyed on code.
     */
    private final Map<String, String> reasonNames;


    /**
     * Constructs a {@link ScheduleEventQuery}.
     *
     * @param schedule       the schedule
     * @param from           the 'from' start time
     * @param to             the 'to' start time
     * @param eventShortName the event archetype short name
     * @param service        the archetype service
     */
    public ScheduleEventQuery(Entity schedule, Date from, Date to, String eventShortName, IArchetypeService service) {
        this.schedule = schedule;
        this.from = from;
        this.to = to;
        this.service = service;
        statusNames = LookupHelper.getNames(service, eventShortName, "status");
        reasonNames = LookupHelper.getNames(service, eventShortName, "reason");
    }

    /**
     * Executes the query.
     *
     * @return the query results.
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> query() {
        IArchetypeQuery query = createQuery(from, to);
        IPage<ObjectSet> page = service.getObjects(query);
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        IMObjectReference currentAct = null;
        ObjectSet current = null;
        String scheduleType = getScheduleType();
        for (ObjectSet set : page.getResults()) {
            IMObjectReference actRef = getAct(set);
            if (currentAct == null || !currentAct.equals(actRef)) {
                if (current != null) {
                    result.add(current);
                }
                currentAct = actRef;
                current = createEvent(actRef, set);
                current.set(ScheduleEvent.ACT_VERSION, set.getLong("act.version"));
            }
            IMObjectReference entityRef = getEntity(set);
            String participation = set.getString("participation.shortName");
            String entityName = set.getString("entity.name");
            if (CustomerArchetypes.CUSTOMER_PARTICIPATION.equals(participation)) {
                current.set(ScheduleEvent.CUSTOMER_REFERENCE, entityRef);
                current.set(ScheduleEvent.CUSTOMER_NAME, entityName);
                current.set(ScheduleEvent.CUSTOMER_PARTICIPATION_VERSION, set.getLong("participation.version"));
            } else if (PatientArchetypes.PATIENT_PARTICIPATION.equals(participation)) {
                current.set(ScheduleEvent.PATIENT_REFERENCE, entityRef);
                current.set(ScheduleEvent.PATIENT_NAME, entityName);
                current.set(ScheduleEvent.PATIENT_PARTICIPATION_VERSION, set.getLong("participation.version"));
            } else if (TypeHelper.isA(entityRef, scheduleType)) {
                current.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, entityRef);
                current.set(ScheduleEvent.SCHEDULE_TYPE_NAME, entityName);
                current.set(ScheduleEvent.SCHEDULE_PARTICIPATION_VERSION, set.getLong("participation.version"));
            } else if (UserArchetypes.CLINICIAN_PARTICIPATION.equals(participation)) {
                current.set(ScheduleEvent.CLINICIAN_REFERENCE, entityRef);
                current.set(ScheduleEvent.CLINICIAN_NAME, entityName);
                current.set(ScheduleEvent.CLINICIAN_PARTICIPATION_VERSION, set.getLong("participation.version"));
            }
            String key = set.getString("act.details_Keys");
            TypedValue value = (TypedValue) set.get("act.details_Values");
            if (key != null) {
                Object object = (value != null) ? value.getObject() : null;
                current.set(key, object);
            }
        }
        if (current != null) {
            result.add(current);
        }
        return new Page<ObjectSet>(result, 0, result.size(), result.size());
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected abstract String getQueryName();

    /**
     * Returns the archetype short name of the schedule type.
     *
     * @return the short name of the schedule type
     */
    protected abstract String getScheduleType();

    /**
     * Creates a new query.
     *
     * @param from the from date
     * @param to   the to date
     * @return the query
     */
    protected IArchetypeQuery createQuery(Date from, Date to) {
        Collection<String> names = Arrays.asList("act.archetypeId",
                                                 "act.id", "act.linkId",
                                                 "act.version",
                                                 "act.startTime", "act.endTime",
                                                 "act.details_Keys",
                                                 "act.details_Values",
                                                 "act.status", "act.reason",
                                                 "act.description",
                                                 "participation.shortName",
                                                 "participation.version",
                                                 "entity.archetypeId",
                                                 "entity.id", "entity.linkId",
                                                 "entity.name");
        NamedQuery query = new NamedQuery(getQueryName(), names);

        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        return query;
    }

    /**
     * Creates a new {@link ObjectSet ObjectSet} representing a scheduled event.
     *
     * @param actRef the reference of the event act
     * @param set    the source set
     * @return a new event
     */
    protected ObjectSet createEvent(IMObjectReference actRef, ObjectSet set) {
        ObjectSet result = new ObjectSet();
        String status = set.getString(ScheduleEvent.ACT_STATUS);
        String reason = set.getString(ScheduleEvent.ACT_REASON);

        result.set(ScheduleEvent.ACT_REFERENCE, actRef);
        result.set(ScheduleEvent.ACT_START_TIME, set.get(ScheduleEvent.ACT_START_TIME));
        result.set(ScheduleEvent.ACT_END_TIME, set.get(ScheduleEvent.ACT_END_TIME));
        result.set(ScheduleEvent.ACT_STATUS, status);
        result.set(ScheduleEvent.ACT_STATUS_NAME, statusNames.get(status));
        result.set(ScheduleEvent.ACT_REASON, reason);
        result.set(ScheduleEvent.ACT_REASON_NAME, reasonNames.get(reason));
        result.set(ScheduleEvent.ACT_DESCRIPTION, set.get(ScheduleEvent.ACT_DESCRIPTION));
        result.set(ScheduleEvent.CUSTOMER_REFERENCE, null);
        result.set(ScheduleEvent.CUSTOMER_NAME, null);
        result.set(ScheduleEvent.PATIENT_REFERENCE, null);
        result.set(ScheduleEvent.PATIENT_NAME, null);
        result.set(ScheduleEvent.SCHEDULE_REFERENCE, schedule.getObjectReference());
        result.set(ScheduleEvent.SCHEDULE_NAME, schedule.getName());
        result.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, null);
        result.set(ScheduleEvent.SCHEDULE_TYPE_NAME, null);
        result.set(ScheduleEvent.CLINICIAN_REFERENCE, null);
        result.set(ScheduleEvent.CLINICIAN_NAME, null);
        result.set(ScheduleEvent.ARRIVAL_TIME, null);
        return result;
    }

    /**
     * Helper to return the act reference from a set.
     *
     * @param set the set
     * @return the ct
     */
    private IMObjectReference getAct(ObjectSet set) {
        ArchetypeId archetypeId = (ArchetypeId) set.get("act.archetypeId");
        long id = set.getLong("act.id");
        String linkId = set.getString("act.linkId");
        return new IMObjectReference(archetypeId, id, linkId);
    }

    /**
     * Helper to return the entity reference from a set.
     *
     * @param set the set
     * @return the entity
     */
    private IMObjectReference getEntity(ObjectSet set) {
        ArchetypeId archetypeId = (ArchetypeId) set.get("entity.archetypeId");
        long id = set.getLong("entity.id");
        String linkId = set.getString("entity.linkId");
        return new IMObjectReference(archetypeId, id, linkId);
    }

}
