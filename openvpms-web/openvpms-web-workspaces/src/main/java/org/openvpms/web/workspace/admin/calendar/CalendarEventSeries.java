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

package org.openvpms.web.workspace.admin.calendar;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.workspace.workflow.appointment.repeat.ScheduleEventSeries;

/**
 * Calendar block series.
 *
 * @author Tim Anderson
 */
public class CalendarEventSeries extends ScheduleEventSeries {

    /**
     * Constructs an {@link CalendarEventSeries}.
     *
     * @param event   the event
     * @param service the archetype service
     */
    public CalendarEventSeries(Act event, IArchetypeService service) {
        super(event, service, 365 * 2); // support 2 years of events
    }

    /**
     * Creates state from an act.
     *
     * @param bean the act bean
     * @return a new state
     */
    @Override
    protected State createState(IMObjectBean bean) {
        return new EventState(bean);
    }

    /**
     * Copies state.
     *
     * @param state the state to copy
     * @return a copy of {@code state}
     */
    @Override
    protected State copy(State state) {
        return new EventState((EventState) state);
    }

    /**
     * Populates an event from state. This is invoked after the event times and schedule have been set.
     *
     * @param bean  the event bean
     * @param state the state
     */
    @Override
    protected void populate(IMObjectBean bean, State state) {
        super.populate(bean, state);
        EventState eventState = (EventState) state;
        bean.setValue("name", eventState.getName());
        bean.setValue("description", eventState.getDescription());
        bean.setTarget("location", eventState.getLocation());
    }

    private static class EventState extends State {

        /**
         * The name.
         */
        private String name;

        /**
         * The description.
         */
        private String description;

        /**
         * The location reference.
         */
        private Reference location;

        /**
         * Initialises the state from an event.
         *
         * @param event the event
         */
        public EventState(IMObjectBean event) {
            super(event);
        }

        /**
         * Copy constructor.
         *
         * @param state the state to copy
         */
        public EventState(EventState state) {
            super(state);
            name = state.getName();
            description = state.getDescription();
            location = state.getLocation();
        }

        /**
         * Updates the state from an event.
         *
         * @param event the event
         */
        @Override
        public void update(IMObjectBean event) {
            super.update(event);
            name = event.getString("name");
            description = event.getString("description");
            location = event.getTargetRef("location");
        }

        /**
         * Returns the event name.
         *
         * @return the event name. May be {@code null}
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the event description.
         *
         * @return the event description. May be {@code null}
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the event location.
         *
         * @return the event location. May be {@code null}
         */
        public Reference getLocation() {
            return location;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         */
        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if (obj instanceof EventState && super.equals(obj)) {
                EventState other = (EventState) obj;
                result = ObjectUtils.equals(name, other.name)
                         && ObjectUtils.equals(description, other.description)
                         && ObjectUtils.equals(location, other.location);
            }
            return result;
        }

    }
}
