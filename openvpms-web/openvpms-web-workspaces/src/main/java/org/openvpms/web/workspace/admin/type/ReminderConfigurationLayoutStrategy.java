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

package org.openvpms.web.workspace.admin.type;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;

/**
 * Layout strategy for <em>entity.reminderConfigurationType</em>.
 *
 * @author Tim Anderson
 */
public class ReminderConfigurationLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Email interval units node.
     */
    private static final String EMAIL_UNITS = "emailUnits";

    /**
     * Email cancel interval units node.
     */
    private static final String EMAIL_CANCEL_UNITS = "emailCancelUnits";

    /**
     * SMS interval units node.
     */
    public static final String SMS_UNITS = "smsUnits";

    /**
     * SMS cancel interval units node.
     */
    public static final String SMS_CANCEL_UNITS = "smsCancelUnits";

    /**
     * Print interval units node.
     */
    public static final String PRINT_UNITS = "printUnits";

    /**
     * Print cancel interval units node.
     */
    public static final String PRINT_CANCEL_UNITS = "printCancelUnits";

    /**
     * Export interval units node.
     */
    public static final String EXPORT_UNITS = "exportUnits";

    /**
     * Export cancel interval units node.
     */
    public static final String EXPORT_CANCEL_UNITS = "exportCancelUnits";

    /**
     * List interval units node.
     */
    public static final String LIST_UNITS = "listUnits";

    /**
     * List interval cancel units node.
     */
    public static final String LIST_CANCEL_UNITS = "listCancelUnits";

    /**
     * The nodes to render. The units are excluded as they are rendered with their corresponding interval.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude(EMAIL_UNITS, EMAIL_CANCEL_UNITS,
                                                                             SMS_UNITS, SMS_CANCEL_UNITS,
                                                                             PRINT_UNITS, PRINT_CANCEL_UNITS,
                                                                             EXPORT_UNITS, EXPORT_CANCEL_UNITS,
                                                                             LIST_UNITS, LIST_CANCEL_UNITS);

    /**
     * Constructs an {@link ReminderConfigurationLayoutStrategy}.
     */
    public ReminderConfigurationLayoutStrategy() {
        super(NODES);
    }

    /**
     * Apply the layout strategy.
     * <p>
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
        addComponent(createComponentPair("emailInterval", EMAIL_UNITS, object, properties, context));
        addComponent(createComponentPair("emailCancelInterval", EMAIL_CANCEL_UNITS, object, properties, context));
        addComponent(createComponentPair("smsInterval", SMS_UNITS, object, properties, context));
        addComponent(createComponentPair("smsCancelInterval", SMS_CANCEL_UNITS, object, properties, context));
        addComponent(createComponentPair("printInterval", PRINT_UNITS, object, properties, context));
        addComponent(createComponentPair("printCancelInterval", PRINT_CANCEL_UNITS, object, properties, context));
        addComponent(createComponentPair("exportInterval", EXPORT_UNITS, object, properties, context));
        addComponent(createComponentPair("exportCancelInterval", EXPORT_CANCEL_UNITS, object, properties, context));
        addComponent(createComponentPair("listInterval", LIST_UNITS, object, properties, context));
        addComponent(createComponentPair("listCancelInterval", LIST_CANCEL_UNITS, object, properties, context));
        return super.apply(object, properties, parent, context);
    }

}
