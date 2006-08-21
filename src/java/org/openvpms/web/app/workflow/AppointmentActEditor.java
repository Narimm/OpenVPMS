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

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.IMObjectProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.edit.TimePropertyTransformer;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.TimeFieldFactory;

import java.util.Date;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentActEditor extends AbstractActEditor {

    /**
     * The appointment slot size.
     */
    private int _slotSize;

    /**
     * The appointment slot units ("minutes" or "hours")
     */
    private String _slotUnits;

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
        EntityBean schedule = getSchedule();
        if (schedule != null) {
            _slotSize = schedule.getInt("slotSize");
            _slotUnits = schedule.getString("slotUnits");
        }

        Property startTime = getProperty("startTime");
        if (startTime.getValue() == null) {
            startTime.setValue(Context.getInstance().getScheduleDate());
        }
        startTime.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStartTimeChanged();
            }
        });
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
     * Invoked when the start time changes. Calculates the end time.
     */
    private void onStartTimeChanged() {
        try {
            Property startTime = getProperty("startTime");
            Object value = startTime.getValue();
            AppointmentTypeParticipationEditor editor
                    = getAppointmentTypeEditor();
            IMObjectReference appointmentType = editor.getEntityRef();
            if (value instanceof Date && appointmentType != null) {
                Date start = (Date) value;
                int noSlots = getSlots(appointmentType);
                Property endTime = getProperty("endTime");
                int minutes;
                int time = _slotSize * noSlots;
                if ("hours".equals(_slotUnits)) {
                    minutes = time * 60;
                } else {
                    minutes = time;
                }
                int millis = minutes * DateUtils.MILLIS_IN_MINUTE;
                Date end = new Date(start.getTime() + millis);
                endTime.setValue(end);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    private void onLayoutCompleted() {
        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
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

    /**
     * Helper to return the no. of slots for an appointment type.
     *
     * @param appointmentType the appointment type
     * @return the no. of slots, or <code>0</code> if unknown
     */
    private int getSlots(IMObjectReference appointmentType) {
        int noSlots = 0;
        EntityBean schedule = getSchedule();
        EntityRelationship relationship = schedule.getRelationship(
                appointmentType);
        if (relationship != null) {
            IMObjectBean bean = new IMObjectBean(relationship);
            noSlots = bean.getInt("noSlots");
        }
        return noSlots;
    }

    /**
     * Helper to return the schedule, wrapped in a bean.
     *
     * @return the schedule, or <code>null</code> if none is available
     */
    private EntityBean getSchedule() {
        IMObjectReference ref = getParticipantRef("schedule");
        Entity schedule = (Entity) IMObjectHelper.getObject(ref);
        if (schedule != null) {
            return new EntityBean(schedule);
        }
        return null;
    }

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a <code>Component</code>, using a factory
         * to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent
         * @param context    the layout context
         * @return the component containing the rendered <code>object</code>
         */
        @Override
        public Component apply(IMObject object, PropertySet properties,
                               IMObject parent, LayoutContext context) {
            Component component = super.apply(object, properties, parent,
                                              context);
            onLayoutCompleted();
            return component;
        }

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
