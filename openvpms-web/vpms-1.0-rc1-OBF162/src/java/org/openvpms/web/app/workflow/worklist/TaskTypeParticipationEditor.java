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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.AbstractParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;

import java.util.List;


/**
 * Participation editor for task types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-15 06:42:15Z $
 */
public class TaskTypeParticipationEditor
        extends AbstractParticipationEditor<Entity> {

    /**
     * The work list, used to constrain task types types. May be <tt>null</tt>.
     */
    private Party workList;


    /**
     * Construct a new <tt>TaskTypeParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent act
     * @param context       the layout context. May be <code>null</code>
     */
    public TaskTypeParticipationEditor(Participation participation,
                                       Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.taskType")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Sets the work list, used to constrain task types.
     * If the current task type is null or not supported by the work list's
     * task types, sets it to a task type associated with the work list.
     * This is the default task type associated with the work list, if present.
     * If not, the first available task task.
     *
     * @param workList the work list. May be <tt>null</tt>
     */
    public void setWorkList(Party workList) {
        this.workList = workList;
        if (workList != null) {
            IMObjectReference taskTypeRef = getEntityRef();
            if (taskTypeRef == null || !hasTaskType(workList, taskTypeRef)) {
                Entity taskType = getDefaultTaskType(workList);
                getEditor().setObject((Entity) taskType);
            }
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createObjectReferenceEditor(
            Property property) {
        return new AbstractIMObjectReferenceEditor<Entity>(
                property, getParent(), getLayoutContext()) {

            @Override
            protected Query<Entity> createQuery(String name) {
                Query<Entity> query = new TaskTypeQuery(workList);
                query.setName(name);
                return query;

            }
        };
    }

    /**
     * Determines if a work list has a particular task type
     *
     * @param workList the work list
     * @param taskType a reference to the task type
     * @return <tt>true</tt> if the work list has the task type
     */
    private boolean hasTaskType(Party workList, IMObjectReference taskType) {
        EntityBean workListBean = new EntityBean(workList);
        List<EntityRelationship> relationships
                = workListBean.getValues("taskTypes", EntityRelationship.class);
        for (EntityRelationship relationship : relationships) {
            if (taskType.equals(relationship.getTarget())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a default task for a work list.
     *
     * @param workList the work list
     * @return the default task type, or <tt>null</tt> if none is found
     */
    private Entity getDefaultTaskType(Party workList) {
        Entity result = null;
        EntityBean workListBean = new EntityBean(workList);
        List<EntityRelationship> relationships
                = workListBean.getValues("taskTypes", EntityRelationship.class);
        for (EntityRelationship relationship : relationships) {
            if (result == null) {
                result = (Entity) IMObjectHelper.getObject(
                        relationship.getTarget());
            } else {
                IMObjectBean bean = new IMObjectBean(relationship);
                if (bean.getBoolean("default")) {
                    result = (Entity) IMObjectHelper.getObject(
                            relationship.getTarget());
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

}
