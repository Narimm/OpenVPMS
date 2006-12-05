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

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.PreloadedResultSet;
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
public class AppointmentTypeQuery extends AbstractIMObjectQuery<IMObject> {

    /**
     * The schedule to constraint appointment types to.
     */
    private final Party _schedule;


    /**
     * Construct a new <code>AppointmentTypeQuery</code> that queries IMObjects
     * with the specified criteria.
     *
     * @param schedule the schedule. May be <code>null</code>
     */
    public AppointmentTypeQuery(Party schedule) {
        super("common", "entity", "appointmentType");
        _schedule = schedule;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<IMObject> query(SortConstraint[] sort) {
        getComponent();  // ensure the component is rendered
        ResultSet<IMObject> result;
        if (_schedule == null) {
            result = super.query(sort);
        } else {
            List<IMObject> objects = filterForSchedule();
            if (objects == null) {
                objects = Collections.emptyList();
            }
            result = new PreloadedResultSet<IMObject>(objects, getMaxResults());
            if (sort != null) {
                result.sort(sort);
            }
        }
        return result;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
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
    private List<IMObject> filterForSchedule() {
        List<IMObject> types = getAppointmentTypes(_schedule);
        String name = getName();
        types = IMObjectHelper.findByName(name, types);
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject type : types) {
            if (type.isActive()) {
                result.add(type);
            }
        }
        return result;
    }

    /**
     * Returns the appointment types associated with a schedule.
     *
     * @param schedule the schedule
     * @return a list of appointment types associated with <code>schedule</code>
     */
    private List<IMObject> getAppointmentTypes(Party schedule) {
        List<IMObject> result = new ArrayList<IMObject>();
        EntityBean bean = new EntityBean(schedule);
        List<IMObject> relationships = bean.getValues("appointmentTypes");
        for (IMObject object : relationships) {
            EntityRelationship relationship = (EntityRelationship) object;
            IMObject type = IMObjectHelper.getObject(relationship.getTarget());
            if (type != null) {
                result.add(type);
            }
        }
        return result;
    }


}
