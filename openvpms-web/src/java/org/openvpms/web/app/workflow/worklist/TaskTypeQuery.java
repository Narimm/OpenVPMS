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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractQuery;
import org.openvpms.web.component.im.query.PreloadedResultSet;
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
public class TaskTypeQuery extends AbstractQuery<IMObject> {

    /**
     * The work list to constraint task types to.
     */
    private final Party _workList;


    /**
     * Construct a new <code>TaskTypeQuery</code> that queries IMObjects with
     * the specified criteria.
     *
     * @param workList the schedule. May be <code>null</code>
     */
    public TaskTypeQuery(Party workList) {
        super("common", "entity", "taskType");
        _workList = workList;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     */
    @Override
    public ResultSet<IMObject> query(SortConstraint[] sort) {
        getComponent();  // ensure the component is rendered
        ResultSet<IMObject> result;
        if (_workList == null) {
            result = super.query(sort);
        } else {
            List<IMObject> objects = filterForWorkList();
            if (objects == null) {
                objects = Collections.emptyList();
            }
            result = new PreloadedResultSet<IMObject>(objects, getMaxRows());
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
        return (_workList != null);
    }

    /**
     * Filter task types associated with a work list.
     *
     * @return a list of task types associated with the work list that matches
     *         the specified criteria
     */
    private List<IMObject> filterForWorkList() {
        List<IMObject> types = getTaskTypes(_workList);
        String name = getName();
        return filter(types, name);
    }

    /**
     * Filter a list of objects.
     *
     * @param objects the objects to filter
     * @param name    the object instance name to matches on
     * @return a list of objects that matches the specified criteria
     */
    private List<IMObject> filter(List<IMObject> objects, String name) {
        final String wildcard = "*";
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : objects) {
            if (!StringUtils.isEmpty(name)) {
                if (name.startsWith(wildcard) || name.endsWith(wildcard)) {
                    name = StringUtils.strip(name, wildcard);
                }
                if (StringUtils.indexOf(object.getName(), name) == -1) {
                    continue;
                }
            }
            if (object.isActive()) {
                result.add(object);
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
    private List<IMObject> getTaskTypes(Party workList) {
        List<IMObject> result = new ArrayList<IMObject>();
        EntityBean bean = new EntityBean(workList);
        List<IMObject> relationships = bean.getValues("taskTypes");
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
