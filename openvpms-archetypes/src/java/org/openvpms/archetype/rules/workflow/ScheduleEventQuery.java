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

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
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
public abstract class ScheduleEventQuery {

    /**
     * Names of the fields being queried.
     */
    protected static final Collection<String> NAMES = Arrays.asList("act.archetypeId",
                                                                    "act.id", "act.linkId",
                                                                    "act.version",
                                                                    "act.startTime", "act.endTime",
                                                                    "act.details_Keys",
                                                                    "act.details_Values",
                                                                    "act.status", "act.reason",
                                                                    "act.name",
                                                                    "act.description",
                                                                    "participation.shortName",
                                                                    "participation.version",
                                                                    "entity.archetypeId",
                                                                    "entity.id", "entity.linkId",
                                                                    "entity.name");

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Status lookup names, keyed on code.
     */
    private final Map<String, String> statusNames;

    /**
     * Reason lookup names, keyed on code.
     */
    private final Map<String, String> reasonNames;

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
     * Constructs a {@link ScheduleEventQuery}.
     *
     * @param schedule    the schedule
     * @param from        the 'from' start time
     * @param to          the 'to' start time
     * @param statusNames the status names, keyed on status code
     * @param reasonNames the reason names, keyed on reason code
     * @param service     the archetype service
     */
    public ScheduleEventQuery(Entity schedule, Date from, Date to, Map<String, String> statusNames,
                              Map<String, String> reasonNames, IArchetypeService service) {
        this.schedule = schedule;
        this.from = from;
        this.to = to;
        this.service = service;
        this.statusNames = statusNames;
        this.reasonNames = reasonNames;
    }

    /**
     * Executes the query.
     *
     * @return the query results.
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> query() {
        IArchetypeQuery query = createQuery(schedule, from, to);
        IPage<ObjectSet> page = service.getObjects(query);
        List<ObjectSet> result = new ArrayList<>();
        Reference currentAct = null;
        ObjectSet current = null;
        String scheduleType = null;
        for (ObjectSet set : page.getResults()) {
            Reference actRef = getAct(set);
            if (currentAct == null || !currentAct.equals(actRef)) {
                if (current != null) {
                    result.add(current);
                }
                currentAct = actRef;
                current = createEvent(actRef, set);
                scheduleType = getScheduleType(actRef.getArchetype());
                current.set(ScheduleEvent.ACT_VERSION, set.getLong("act.version"));
            }
            Reference entityRef = getEntity(set);
            String participation = set.getString("participation.shortName");
            String entityName = set.getString("entity.name");
            long version = set.getLong("participation.version");
            if (!populateParticipation(current, participation, entityRef, entityName, version)) {
                if (scheduleType != null && entityRef.isA(scheduleType)) {
                    populate(current, "scheduleType", entityRef, entityName, version);
                }
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
        return new Page<>(result, 0, result.size(), result.size());
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected abstract String getQueryName();

    /**
     * Returns the archetype of the schedule type.
     *
     * @param eventArchetype the event archetype
     * @return the archetype of the schedule type, or {@code null} if none is present
     */
    protected abstract String getScheduleType(String eventArchetype);

    /**
     * Creates a new query.
     *
     * @param schedule the schedule
     * @param from     the from date
     * @param to       the to date
     * @return the query
     */
    protected IArchetypeQuery createQuery(Entity schedule, Date from, Date to) {
        NamedQuery query = new NamedQuery(getQueryName(), NAMES);
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
    protected ObjectSet createEvent(Reference actRef, ObjectSet set) {
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
        result.set(ScheduleEvent.ACT_NAME, set.get(ScheduleEvent.ACT_NAME));
        result.set(ScheduleEvent.ACT_DESCRIPTION, set.get(ScheduleEvent.ACT_DESCRIPTION));
        result.set(ScheduleEvent.CUSTOMER_REFERENCE, null);
        result.set(ScheduleEvent.CUSTOMER_NAME, null);
        result.set(ScheduleEvent.PATIENT_REFERENCE, null);
        result.set(ScheduleEvent.PATIENT_NAME, null);
        result.set(ScheduleEvent.SCHEDULE_REFERENCE, null);
        result.set(ScheduleEvent.SCHEDULE_NAME, null);
        result.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, null);
        result.set(ScheduleEvent.SCHEDULE_TYPE_NAME, null);
        result.set(ScheduleEvent.CLINICIAN_REFERENCE, null);
        result.set(ScheduleEvent.CLINICIAN_NAME, null);
        result.set(ScheduleEvent.ARRIVAL_TIME, null);
        return result;
    }

    /**
     * Populates a set with participation relationship details.
     *
     * @param set        the set to populate
     * @param archetype  the participation archetype
     * @param entityRef  the entity reference
     * @param entityName the entity name
     * @param version    the participation version
     * @return {@code true} if the set was populated
     */
    protected boolean populateParticipation(ObjectSet set, String archetype, Reference entityRef, String entityName,
                                            long version) {
        boolean populated = true;
        if (ScheduleArchetypes.SCHEDULE_PARTICIPATION.equals(archetype)) {
            populate(set, "schedule", entityRef, entityName, version);
        } else if (CustomerArchetypes.CUSTOMER_PARTICIPATION.equals(archetype)) {
            populate(set, "customer", entityRef, entityName, version);
        } else if (PatientArchetypes.PATIENT_PARTICIPATION.equals(archetype)) {
            populate(set, "patient", entityRef, entityName, version);
        } else if (UserArchetypes.CLINICIAN_PARTICIPATION.equals(archetype)) {
            populate(set, "clinician", entityRef, entityName, version);
        } else {
            populated = false;
        }
        return populated;
    }

    /**
     * Populates a set with participation details.
     *
     * @param set        the set to populate
     * @param prefix     the participation prefix
     * @param entityRef  the entity reference
     * @param entityName the entity name
     * @param version    the participation version
     */
    protected void populate(ObjectSet set, String prefix, Reference entityRef, String entityName,
                            long version) {
        set.set(prefix + ".objectReference", entityRef);
        set.set(prefix + ".name", entityName);
        set.set(prefix + "Participation.version", version);
    }

    /**
     * Helper to return the act reference from a set.
     *
     * @param set the set
     * @return the ct
     */
    private Reference getAct(ObjectSet set) {
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
    private Reference getEntity(ObjectSet set) {
        ArchetypeId archetypeId = (ArchetypeId) set.get("entity.archetypeId");
        long id = set.getLong("entity.id");
        String linkId = set.getString("entity.linkId");
        return new IMObjectReference(archetypeId, id, linkId);
    }

}
