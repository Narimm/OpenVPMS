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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundTimeField;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.TimeFieldFactory;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentActEditor extends AbstractActEditor {

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

    private void onLayoutCompleted() {
        IMObjectReference schedule = getParticipant("schedule");
        AppointmentTypeParticipationEditor editor
                = (AppointmentTypeParticipationEditor) getEditor(
                "appointmentType");
        editor.setSchedule((Party) IMObjectHelper.getObject(schedule));
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
                BoundTimeField field = TimeFieldFactory.create(property);
                field.setDate(Context.getInstance().getScheduleDate());
                component = field;
            } else {
                component = super.createComponent(property, parent, context);
            }
            return component;
        }
    }
}
