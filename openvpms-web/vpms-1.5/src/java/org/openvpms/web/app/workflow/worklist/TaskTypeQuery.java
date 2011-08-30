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

package org.openvpms.web.app.workflow.worklist;

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
 * Task type query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskTypeQuery extends AbstractIMObjectQuery<Entity> {

    /**
     * The work list to constraint task types to.
     */
    private final Party workList;


    /**
     * Constructs a new <tt>TaskTypeQuery</tt> that queries IMObjects with
     * the specified criteria.
     *
     * @param workList the schedule. May be <tt>null</tt>
     */
    public TaskTypeQuery(Party workList) {
        super(new String[]{"entity.taskType"}, Entity.class);
        this.workList = workList;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        getComponent();  // ensure the component is rendered
        ResultSet<Entity> result;
        if (workList == null) {
            result = super.query(sort);
        } else {
            List<Entity> objects = filterForWorkList();
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
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    @Override
    public boolean isAuto() {
        return (workList != null);
    }

    /**
     * Filter task types associated with a work list.
     *
     * @return a list of task types associated with the work list that matches
     *         the specified criteria
     */
    private List<Entity> filterForWorkList() {
        List<Entity> types = getTaskTypes(workList);
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
     * Returns the appointment types associated with a work list.
     *
     * @param workList the work list
     * @return a list of task types associated with <code>workList</code>
     */
    private List<Entity> getTaskTypes(Party workList) {
        List<Entity> result = new ArrayList<Entity>();
        EntityBean bean = new EntityBean(workList);
        List<IMObject> relationships = bean.getValues("taskTypes");
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
