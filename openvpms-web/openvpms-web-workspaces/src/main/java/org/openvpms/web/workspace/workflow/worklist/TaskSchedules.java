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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
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
     * Constructs a {@link TaskSchedules}.
     *
     * @param location the location. May be {@code null}
     * @param prefs    the user preferences
     */
    public TaskSchedules(Party location, Preferences prefs) {
        this(location, prefs, ServiceHelper.getBean(LocationRules.class));
    }

    /**
     * Constructs a {@link TaskSchedules}.
     *
     * @param location the location. May be {@code null}
     * @param prefs    the user preferences
     * @param rules    the location rules
     */
    public TaskSchedules(Party location, Preferences prefs, LocationRules rules) {
        super(location, ScheduleArchetypes.WORK_LIST_VIEW, prefs, rules);
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
        IMObjectReference reference = getScheduleView(PreferenceArchetypes.WORK_LIST);
        LocationRules rules = getLocationRules();
        Entity view = null;
        Party location = getLocation();
        if (reference != null && rules.hasWorkListView(location, reference)) {
            // only use the view from preferences if it is available at the current location
            view = (Entity) IMObjectHelper.getObject(reference);
        }
        if (view == null) {
            view = rules.getDefaultWorkListView(location);
        }
        return view;
    }

    /**
     * Returns the default schedule view.
     *
     * @param views the available schedule views
     * @return the default schedule view. May be {@code null}
     */
    @Override
    public Entity getDefaultScheduleView(List<Entity> views) {
        Entity view = getScheduleView(PreferenceArchetypes.WORK_LIST, views);
        if (view == null) {
            Party location = getLocation();
            if (location != null) {
                view = getLocationRules().getDefaultWorkListView(location);
            }
        }
        return view;
    }

    /**
     * Returns the active schedules for the specified schedule view.
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
     * Returns the default schedule for the specified view.
     *
     * @param view      the view
     * @param schedules the available schedules in the view
     * @return the default schedule, or {@code null} for all schedules
     */
    @Override
    public Entity getDefaultSchedule(Entity view, List<Entity> schedules) {
        return getSchedule(PreferenceArchetypes.WORK_LIST, schedules);
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
