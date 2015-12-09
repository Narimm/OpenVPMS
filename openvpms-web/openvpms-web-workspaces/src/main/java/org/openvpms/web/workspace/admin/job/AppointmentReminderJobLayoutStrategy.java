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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.job;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

/**
 * Layout strategy for <em>entity.jobAppointmentReminder</em>.
 *
 * @author Tim Anderson
 */
public class AppointmentReminderJobLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The SMS From node.
     */
    public static final String SMS_FROM = "smsFrom";

    /**
     * The SMS From Units node.
     */
    public static final String SMS_FROM_UNITS = "smsFromUnits";

    /**
     * The SMS To node.
     */
    public static final String SMS_TO = "smsTo";

    /**
     * The SMS To Units node.
     */
    public static final String SMS_TO_UNITS = "smsToUnits";

    /**
     * The No Reminder node.
     */
    public static final String NO_REMINDER = "noReminder";

    /**
     * The No Reminder Units node.
     */
    public static final String NO_REMINDER_UNITS = "noReminderUnits";

    /**
     * The nodes to render. The units are excluded as they are rendered with their corresponding interval.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude(SMS_FROM_UNITS, SMS_TO_UNITS,
                                                                             NO_REMINDER_UNITS);

    /**
     * Constructs an {@link AppointmentReminderJobLayoutStrategy}.
     */
    public AppointmentReminderJobLayoutStrategy() {
        super(NODES);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        addComponent(getInterval(SMS_FROM, SMS_FROM_UNITS, object, properties, context));
        addComponent(getInterval(SMS_TO, SMS_TO_UNITS, object, properties, context));
        addComponent(getInterval(NO_REMINDER, NO_REMINDER_UNITS, object, properties, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Creates a component representing an interval and its units.
     *
     * @param interval   the interval property name
     * @param units      the units property name
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     * @return a new component
     */
    private ComponentState getInterval(String interval, String units, IMObject object, PropertySet properties,
                                       LayoutContext context) {
        Property property = properties.get(interval);
        ComponentState intervalState = createComponent(property, object, context);
        ComponentState unitsState = createComponent(properties.get(units), object, context);
        FocusGroup group = new FocusGroup(interval);
        group.add(intervalState.getComponent());
        group.add(unitsState.getComponent());
        return new ComponentState(RowFactory.create(Styles.CELL_SPACING, intervalState.getComponent(),
                                                    unitsState.getComponent()), intervalState.getProperty(), group);
    }
}
