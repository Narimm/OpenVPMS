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

package org.openvpms.web.workspace.admin.job;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;

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
        addComponent(createComponentPair(SMS_FROM, SMS_FROM_UNITS, object, properties, context));
        addComponent(createComponentPair(SMS_TO, SMS_TO_UNITS, object, properties, context));
        addComponent(createComponentPair(NO_REMINDER, NO_REMINDER_UNITS, object, properties, context));
        return super.apply(object, properties, parent, context);
    }
}
