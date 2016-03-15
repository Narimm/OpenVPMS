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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.boarding;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups schedules by cage type.
 *
 * @author Tim Anderson
 */
class CageScheduleGroup {

    /**
     * The cage type. May be {@code null}
     */
    private final Entity cageType;

    /**
     * The cage type name.
     */
    private final String name;

    /**
     * The schedules.
     */
    private final List<Schedule> schedules = new ArrayList<>();

    /**
     * Determines if the group is expanded.
     */
    private boolean expanded = false;

    /**
     * The expand/collapse label.
     */
    private Label component;

    /**
     * Constructs a {@link CageScheduleGroup}.
     *
     * @param cageType the cage type. May be {@code null}
     */
    public CageScheduleGroup(Entity cageType) {
        this.cageType = cageType;
        this.name = (cageType != null) ? cageType.getName() : Messages.get("workflow.scheduling.appointment.nocage");
        component = new Label();
        setExpanded(false);
    }

    /**
     * Returns the cage type.
     *
     * @return the cage type. May be {@code null}
     */
    public Entity getCageType() {
        return cageType;
    }

    /**
     * Returns the group name.
     *
     * @return the group name. May be {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a schedule.
     *
     * @param schedule the schedule to add
     */
    public void add(Schedule schedule) {
        Schedule previous = schedules.size() > 0 ? schedules.get(schedules.size() - 1) : null;
        schedules.add(schedule);
        boolean even;
        if (previous == null) {
            even = false;
        } else {
            if (previous.getSchedule().equals(schedule.getSchedule())) {
                even = previous.getRenderEven();
            } else {
                even = !previous.getRenderEven();
            }
        }
        schedule.setRenderEven(even);
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules. Schedules may be included more than once to handle overlapping bookings
     */
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Returns the no. of unique schedules in the group.
     *
     * @return the no. of unique schedules
     */
    public int getScheduleCount() {
        Set<Entity> set = new HashSet<>();
        for (Schedule schedule : schedules) {
            set.add(schedule.getSchedule());
        }
        return set.size();
    }

    /**
     * Determines if the group is expanded.
     *
     * @return {@code true} if the group is expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Expands or contracts the group.
     *
     * @param expanded {if {@code true}, the group is expanded, otherwise it is contracted
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        component.setStyleName(expanded ? "CageType.minus" : "CageType.plus");
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Determines if the group matches an object.
     *
     * @param obj the object to compare with
     * @return {@code true} with this equals {@code obj}}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof CageScheduleGroup) {
            return ObjectUtils.equals(cageType, ((CageScheduleGroup) obj).cageType);
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return cageType != null ? cageType.hashCode() : name.hashCode();
    }
}
