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

package org.openvpms.web.workspace.workflow.worklist;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Predicates;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;


/**
 * Participation editor for task types.
 *
 * @author Tim Anderson
 */
public class TaskTypeParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The work list, used to constrain task types types. May be {@code null}.
     */
    private Entity workList;


    /**
     * Constructs a {@link TaskTypeParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent act
     * @param context       the layout context. May be {@code null}
     */
    public TaskTypeParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!participation.isA(ScheduleArchetypes.TASK_TYPE_PARTICIPATION)) {
            throw new IllegalArgumentException("Invalid participation type:" + participation.getArchetype());
        }
        service = ServiceHelper.getArchetypeService();
    }

    /**
     * Sets the work list, used to constrain task types.
     * If the current task type is null or not supported by the work list's
     * task types, sets it to a task type associated with the work list.
     * This is the default task type associated with the work list, if present.
     * If not, the first available task type.
     *
     * @param workList the work list. May be {@code null}
     */
    public void setWorkList(Entity workList) {
        this.workList = workList;
        if (workList != null) {
            IMObjectReference taskTypeRef = getEntityRef();
            if (taskTypeRef == null || !hasTaskType(workList, taskTypeRef)) {
                Entity taskType = getDefaultTaskType(workList);
                setEntity(taskType);
            }
        } else {
            setEntity(null);
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createEntityEditor(Property property) {
        return new AbstractIMObjectReferenceEditor<Entity>(property, getParent(), getLayoutContext()) {

            @Override
            protected Query<Entity> createQuery(String name) {
                Query<Entity> query = new TaskTypeQuery(workList, getLayoutContext().getContext());
                if (name != null) {
                    Entity entity = getEntity();
                    if (entity == null || !StringUtils.equals(entity.getName(), name)) {
                        query.setValue(name);
                    }
                }
                return query;

            }
        };
    }

    /**
     * Determines if a work list has a particular task type.
     *
     * @param workList the work list
     * @param taskType a reference to the task type
     * @return {@code true} if the work list has the task type
     */
    private boolean hasTaskType(Entity workList, IMObjectReference taskType) {
        IMObjectBean bean = service.getBean(workList);
        return bean.getValue("taskTypes", EntityRelationship.class, Predicates.targetEquals(taskType)) != null;
    }

    /**
     * Returns a default task for a work list.
     *
     * @param workList the work list
     * @return the default task type, or {@code null} if none is found
     */
    private Entity getDefaultTaskType(Entity workList) {
        return EntityRelationshipHelper.getDefaultTarget(workList, "taskTypes", true, service);
    }

}
