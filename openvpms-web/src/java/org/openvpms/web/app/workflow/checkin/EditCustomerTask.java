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

package org.openvpms.web.app.workflow.checkin;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;

import java.util.List;


/**
 * Task to edit an <em>act.customerTask</em> act.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class EditCustomerTask extends EditIMObjectTask {

    /**
     * Constructs a new <code>EditCustomerTask</code>.
     *
     * @param shortName the object short name
     */
    public EditCustomerTask(String shortName) {
        super(shortName, false, true);
    }

    /**
     * Edits an object.
     *
     * @param object  the object to edit
     * @param context the task context
     */
    @Override
    protected void edit(IMObject object, TaskContext context) {
        ActBean bean = new ActBean((Act) object);
        Party workList = context.getWorkList();
        Entity taskType = getDefaultTaskType(workList);
        if (taskType != null) {
            bean.setParticipant("participation.taskType", taskType);
        }
        super.edit(object, context);
    }

    /**
     * Returns the default task type associated with a work list.
     *
     * @param workList the work list
     * @return a the default task types associated with
     *         <code>workList</code>, or <code>null</code> if there is no
     *         default task type
     */
    private Entity getDefaultTaskType(Party workList) {
        Entity type = null;
        EntityBean bean = new EntityBean(workList);
        List<IMObject> relationships = bean.getValues("taskTypes");
        for (IMObject object : relationships) {
            EntityRelationship relationship = (EntityRelationship) object;
            IMObjectBean relBean = new IMObjectBean(relationship);
            if (relBean.getBoolean("default")) {
                type = (Entity) IMObjectHelper.getObject(
                        relationship.getTarget());
                if (type != null) {
                    break;
                }
            }
        }
        return type;
    }
}
