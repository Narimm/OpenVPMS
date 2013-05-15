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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.workflow;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A query for <em>entity.appointmentType</em> and <em>entity.taskType</em> instances, that may be constrained on
 * name, or whether or not they are present in an associated <em>party.organisationSchedule</em> or
 * <em>party.organisationWorkList</em>.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleTypeQuery extends AbstractIMObjectQuery<Entity> {

    /**
     * The <em>party.organisationSchedule</em> or <em>party.organisationWorkList</em> to constraint types to.
     * May be {@code null}.
     */
    private Entity schedule;

    /**
     * The node to use when retrieving appointment or task types from {@link #schedule}.
     */
    private final String scheduleTypesNode;

    /**
     * The context.
     */
    private final Context context;


    /**
     * Constructs a {@code ScheduleTypeQuery}.
     *
     * @param shortNames        the short names
     * @param schedule          the <em>party.organisationSchedule</em> or <em>party.organisationWorkList</em>.
     *                          May be {@code null}
     * @param scheduleTypesNode the node to use when retrieving appointment or task types from {@link #schedule}.
     * @param context           the context
     */
    public ScheduleTypeQuery(String[] shortNames, Entity schedule, String scheduleTypesNode,
                             Context context) {
        super(shortNames, Entity.class);
        this.schedule = schedule;
        this.scheduleTypesNode = scheduleTypesNode;
        this.context = context;
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule. May be {@code null}
     */
    public void setSchedule(Entity schedule) {
        this.schedule = schedule;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          if the query fails
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        getComponent();  // ensure the component is rendered
        ResultSet<Entity> result;
        if (schedule == null) {
            result = super.query(sort);
        } else {
            List<Entity> objects = filterForSchedule();
            if (objects == null) {
                objects = Collections.emptyList();
            }
            result = new IMObjectListResultSet<Entity>(objects, getMaxResults());
            if (sort != null) {
                result.sort(sort);
            }
        }
        return result;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically; otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return (schedule != null);
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        boolean result;
        if (schedule == null) {
            result = super.selects(reference);
        } else {
            EntityBean bean = new EntityBean(schedule);
            result = bean.getNodeTargetEntityRefs(scheduleTypesNode).contains(reference);
        }
        return result;
    }

    /**
     * Filter schedule types associated with a schedule.
     *
     * @return a list of schedule types associated with the schedule that matches the specified criteria
     */
    private List<Entity> filterForSchedule() {
        List<Entity> types = getScheduleTypes(schedule);
        String name = getValue();
        types = IMObjectHelper.findByName(name, types);
        List<Entity> result = new ArrayList<Entity>();
        for (IMObject type : types) {
            if (type.isActive()) {
                result.add((Entity) type);
            }
        }
        return result;
    }

    /**
     * Returns the schedule types associated with a schedule.
     *
     * @param schedule the schedule
     * @return a list of appointment types associated with {@code schedule}
     */
    private List<Entity> getScheduleTypes(Entity schedule) {
        List<Entity> result = new ArrayList<Entity>();
        EntityBean bean = new EntityBean(schedule);
        List<IMObject> relationships = bean.getValues(scheduleTypesNode);
        for (IMObject object : relationships) {
            EntityRelationship relationship = (EntityRelationship) object;
            IMObject type = IMObjectHelper.getObject(relationship.getTarget(), context);
            if (type != null) {
                result.add((Entity) type);
            }
        }
        return result;
    }
}
