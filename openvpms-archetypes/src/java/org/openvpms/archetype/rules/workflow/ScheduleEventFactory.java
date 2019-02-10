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

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.Participation;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A factory for events used by the {@link ScheduleService}.
 * <p>
 * Events are converted from acts to {@link PropertySet} instances to:
 * <ul>
 * <li>lower memory requirements</li>
 * <li>cache customer, patient, clinician and schedule names</li>
 * </ul>
 *
 * @author Tim Anderson
 * @see ScheduleEvent
 */
public abstract class ScheduleEventFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Cache of status lookup names, keyed on code.
     */
    private final Map<String, String> statusNames;


    /**
     * Constructs a {@link ScheduleEventFactory}.
     *
     * @param eventShortName the event archetype short name
     * @param service        the archetype service
     * @param lookups        the lookup service
     */
    public ScheduleEventFactory(String eventShortName, IArchetypeService service, ILookupService lookups) {
        this(LookupHelper.getNames(service, lookups, eventShortName, "status"), service);
    }

    /**
     * Constructs a {@link ScheduleEventFactory}.
     *
     * @param service the archetype service
     */
    public ScheduleEventFactory(Map<String, String> statusNames, IArchetypeService service) {
        this.service = service;
        this.statusNames = statusNames;
    }

    /**
     * Returns all events for an entity on the given day.
     *
     * @param reference the entity reference
     * @param startTime the start time, inclusive
     * @param endTime   the end time exclusive
     * @return all events on the specified day for the schedule
     */
    public List<PropertySet> getEvents(Reference reference, Date startTime, Date endTime) {
        Entity entity = (Entity) service.get(reference);
        if (entity == null) {
            throw new IllegalStateException("Cannot locate entity with reference=" + reference);
        }
        return getEvents(entity, startTime, endTime);
    }

    /**
     * Returns all events for an entity between two times.
     *
     * @param entity    the entity
     * @param startTime the start time, inclusive
     * @param endTime   the end time, exclusive
     * @return all events on the specified day for the schedule
     */
    public List<PropertySet> getEvents(Entity entity, Date startTime, Date endTime) {
        ScheduleEventQuery query = createQuery(entity, startTime, endTime);
        IPage<ObjectSet> page = query.query();
        return new ArrayList<>(page.getResults());
    }

    /**
     * Creates an event from an event act.
     *
     * @param act the act
     * @return a new event
     */
    public PropertySet createEvent(Act act) {
        ObjectSet set = new ObjectSet();
        IMObjectBean bean = service.getBean(act);
        assemble(set, bean);
        return set;
    }

    /**
     * Assembles a {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    protected void assemble(PropertySet target, IMObjectBean source) {
        Act event = (Act) source.getObject();
        String status = event.getStatus();
        target.set(ScheduleEvent.ACT_VERSION, event.getVersion());
        target.set(ScheduleEvent.ACT_REFERENCE, event.getObjectReference());
        target.set(ScheduleEvent.ACT_START_TIME, event.getActivityStartTime());
        target.set(ScheduleEvent.ACT_END_TIME, event.getActivityEndTime());
        target.set(ScheduleEvent.ACT_STATUS, status);
        target.set(ScheduleEvent.ACT_STATUS_NAME, statusNames.get(status));
        target.set(ScheduleEvent.ACT_DESCRIPTION, event.getDescription());

        populate(target, source, "customer");
        populate(target, source, "patient");
        populate(target, source, "clinician");
    }

    protected void populate(PropertySet target, IMObjectBean source, String node) {
        Participation participation = null;
        if (source.hasNode(node)) {
            participation = source.getObject(node, Participation.class);
        }
        populate(target, participation, node);
    }

    protected void populate(PropertySet target, Participation source, String node) {
        Reference reference = null;
        String name = null;
        long version = -1;

        if (source != null) {
            reference = source.getEntity();
            name = getName(reference);
            version = source.getVersion();
        }
        target.set(node + ".objectReference", reference);
        target.set(node + ".name", name);
        target.set(node + "Participation.version", version);
    }

    /**
     * Creates a query to query events for a particular entity between two times.
     *
     * @param entity    the entity
     * @param startTime the start time, inclusive
     * @param endTime   the end time, exclusive
     * @return a new query
     */
    protected abstract ScheduleEventQuery createQuery(Entity entity, Date startTime, Date endTime);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be {@code null}
     * @return the name or {@code null} if none exists
     */
    protected String getName(Reference reference) {
        if (reference != null) {
            ObjectRefConstraint constraint = new ObjectRefConstraint("o", reference);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("o.name"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                return set.getString("o.name");
            }
        }
        return null;
    }

    /**
     * Returns the status names.
     *
     * @return the status names, keyed on status code
     */
    protected Map<String, String> getStatusNames() {
        return statusNames;
    }

}
