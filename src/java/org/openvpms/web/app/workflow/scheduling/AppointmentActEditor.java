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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.IMObjectProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.TimePropertyTransformer;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.TimeFieldFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentActEditor extends AbstractActEditor {

    /**
     * Listener for modifications to the endTime property.
     */
    private final ModifiableListener _endTimeListener;


    /**
     * Construct a new <code>AppointmentActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public AppointmentActEditor(Act act, IMObject parent,
                                LayoutContext context) {
        super(act, parent, context);
        initParticipant("schedule", Context.getInstance().getSchedule());
        Property startTime = getProperty("startTime");
        if (startTime.getValue() == null) {
            startTime.setValue(getDefaultTime());
        }
        startTime.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStartTimeChanged();
            }
        });

        Property endTime = getProperty("endTime");
        _endTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onEndTimeChanged();
            }
        };
        endTime.addModifiableListener(_endTimeListener);
    }

    /**
     * Calculates the default time as 'now' rounded to the next nearest 15
     * minute interval.
     *
     * @return the default time
     */
    private Date getDefaultTime() {
        int slotSize = 0;
        Party schedule = (Party) getParticipant("schedule");
        if (schedule != null) {
            slotSize = AppointmentRules.getSlotSize(schedule);
        }
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (slotSize != 0) {
            int mins = calendar.get(Calendar.MINUTE);
            mins = ((mins / slotSize) * slotSize) + slotSize;
            calendar.add(Calendar.MINUTE, mins);
        }
        return calendar.getTime();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAppointmentTypeChanged();
            }
        });
    }

    /**
     * Invoked when the start time changes. Calculates the end time.
     */
    private void onStartTimeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the end time changes. Sets the value to start time if
     * end time < start time.
     */
    private void onEndTimeChanged() {
        Object startValue = getProperty("startTime").getValue();
        Property end = getProperty("endTime");
        Object endValue = end.getValue();
        if (startValue instanceof Date && endValue instanceof Date) {
            Date startTime = (Date) startValue;
            Date endTime = (Date) endValue;
            if (endTime.compareTo(startTime) < 0) {
                end.setValue(startTime);
            }
        }
    }

    /**
     * Invoked when the appointment type changes. Calculates the end time
     * if the start time is set.
     */
    private void onAppointmentTypeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Calculates the end time if the start time and appointment type are set.
     *
     * @throws OpenVPMSException for any error
     */
    private void calculateEndTime() {
        Property startTime = getProperty("startTime");
        Object value = startTime.getValue();
        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor
                = getAppointmentTypeEditor();
        Entity appointmentType = editor.getEntity();
        if (value instanceof Date && schedule != null
                && appointmentType != null) {
            Date start = (Date) value;
            Date end = AppointmentRules.calculateEndTime(start,
                                                         schedule,
                                                         appointmentType);
            Property endTime = getProperty("endTime");
            endTime.removeModifiableListener(_endTimeListener);
            endTime.setValue(end);
            endTime.addModifiableListener(_endTimeListener);
        }
    }

    /**
     * Returns the appointment type editor.
     *
     * @return the appointment type editor
     */
    private AppointmentTypeParticipationEditor getAppointmentTypeEditor() {
        return (AppointmentTypeParticipationEditor) getEditor(
                "appointmentType");
    }

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Creates a component for a property. This maintains a cache of created
         * components, in order for the focus to be set on an appropriate
         * component.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected Component createComponent(Property property, IMObject parent,
                                            LayoutContext context) {
            Component component;
            String name = property.getDescriptor().getName();
            if (name.equals("startTime") || name.equals("endTime")) {
                IMObjectProperty timeProperty = (IMObjectProperty) property;
                TimePropertyTransformer transformer
                        = new TimePropertyTransformer(parent,
                                                      property.getDescriptor());
                transformer.setDate(Context.getInstance().getScheduleDate());
                timeProperty.setTransformer(transformer);
                component = TimeFieldFactory.create(property);
            } else {
                component = super.createComponent(property, parent, context);
            }
            return component;
        }
    }
}
