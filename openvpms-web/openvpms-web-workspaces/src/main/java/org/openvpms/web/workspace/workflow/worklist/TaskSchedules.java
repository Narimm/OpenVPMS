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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.AbstractSchedules;

import java.util.Collections;
import java.util.List;

/**
 * Task schedules (a.k.a work-lists).
 *
 * @author Tim Anderson
 */
public class TaskSchedules extends AbstractSchedules {

    /**
     * Constructs an {@link TaskSchedules}.
     *
     * @param location the location. May be {@code null}
     */
    public TaskSchedules(Party location) {
        super(location, ScheduleArchetypes.WORK_LIST_VIEW);
    }

    /**
     * Returns the schedule views.
     *
     * @return the schedule views
     */
    @Override
    public List<Entity> getScheduleViews() {
        Party location = getLocation();
        return (location != null) ? getLocationRules().getWorkListViews(location) : Collections.<Entity>emptyList();
    }

    /**
     * Returns the default schedule view.
     *
     * @return the default schedule view. May be {@code null}
     */
    @Override
    public Entity getDefaultScheduleView() {
        Party location = getLocation();
        return (location != null) ? getLocationRules().getDefaultWorkListView(location) : null;
    }

    /**
     * Returns the schedules for the specified schedule view.
     *
     * @param view the schedule view
     * @return the corresponding schedules
     */
    @Override
    public List<Entity> getSchedules(Entity view) {
        EntityBean bean = new EntityBean(view);
        return bean.getNodeTargetEntities("workLists", SequenceComparator.INSTANCE);
    }

    /**
     * Returns a display name for the schedule selector.
     *
     * @return a display name for the schedule selector
     */
    @Override
    public String getScheduleDisplayName() {
        return Messages.get("workflow.scheduling.query.worklist");
    }

}
