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
 *
 *  $Id$
 */
package org.openvpms.web.app.workflow.worklist;

import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.app.workflow.ScheduleTypeQueryTest;
import org.openvpms.web.component.im.query.Query;


/**
 * Tests the {@link TaskTypeQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskTypeQueryTestCase extends ScheduleTypeQueryTest {

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<Entity> createQuery() {
        return new TaskTypeQuery(null);
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected Entity createObject(String value, boolean save) {
        return ScheduleTestHelper.createTaskType(value, save);
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected String getUniqueValue() {
        return getUniqueValue("ZTaskType");
    }

    /**
     * Creates a new schedule.
     *
     * @return a new schedule
     */
    protected Party createSchedule() {
        return ScheduleTestHelper.createWorkList();
    }

    /**
     * Adds a schedule type to a schedule.
     *
     * @param schedule     the schedule
     * @param scheduleType type the schedule type
     */
    protected void addScheduleType(Party schedule, Entity scheduleType) {
        EntityBean bean = new EntityBean(schedule);
        EntityRelationship relationship = bean.addNodeRelationship("taskTypes", scheduleType);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("noSlots", 1);
    }
}