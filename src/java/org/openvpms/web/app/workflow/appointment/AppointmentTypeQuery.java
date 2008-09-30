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

package org.openvpms.web.app.workflow.appointment;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Appointment type query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentTypeQuery extends AbstractIMObjectQuery<Entity> {

    /**
     * The schedule to constraint appointment types to.
     */
    private final Party _schedule;


    /**
     * Construct a new <tt>AppointmentTypeQuery</tt> that queries IMObjects
     * with the specified criteria.
     *
     * @param schedule the schedule. May be <tt>null</tt>
     */
    public AppointmentTypeQuery(Party schedule) {
        super(new String[]{"entity.appointmentType"}, Entity.class);
        _schedule = schedule;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        getComponent();  // ensure the component is rendered
        ResultSet<Entity> result;
        if (_schedule == null) {
            result = super.query(sort);
        } else {
            List<Entity> objects = filterForSchedule();
            if (objects == null) {
                objects = Collections.emptyList();
            }
            result = new IMObjectListResultSet<Entity>(objects,
                                                       getMaxResults());
            if (sort != null) {
                result.sort(sort);
            }
        }
        return result;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automaticaly;
     *         otherwie <tt>false</tt>
     */
    @Override
    public boolean isAuto() {
        return (_schedule != null);
    }

    /**
     * Filter appointment types associated with a schedule.
     *
     * @return a list of appointment types associated with the schedule that
     *         matches the specified criteria
     */
    private List<Entity> filterForSchedule() {
        List<Entity> types = getAppointmentTypes(_schedule);
        String name = getName();
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
     * Returns the appointment types associated with a schedule.
     *
     * @param schedule the schedule
     * @return a list of appointment types associated with <tt>schedule</tt>
     */
    private List<Entity> getAppointmentTypes(Party schedule) {
        List<Entity> result = new ArrayList<Entity>();
        EntityBean bean = new EntityBean(schedule);
        List<IMObject> relationships = bean.getValues("appointmentTypes");
        for (IMObject object : relationships) {
            EntityRelationship relationship = (EntityRelationship) object;
            IMObject type = IMObjectHelper.getObject(relationship.getTarget());
            if (type != null) {
                result.add((Entity) type);
            }
        }
        return result;
    }

}
