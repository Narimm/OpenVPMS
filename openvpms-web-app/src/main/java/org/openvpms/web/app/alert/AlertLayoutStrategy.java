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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.alert;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.colour.ColourHelper;
import org.openvpms.web.echo.factory.TextComponentFactory;

import java.util.List;


/**
 * Layout strategy for <em>act.customerAlert</em> and <em>act.patientAlert</em>.
 * This includes a field to display the associated alert type's priority and colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class AlertLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The field to display the alert priority and colour.
     */
    private TextField priority;


    /**
     * Constructs a <tt>AlertLayoutStrategy</tt>.
     */
    public AlertLayoutStrategy() {
        this(TextComponentFactory.create());
    }

    /**
     * Constructs a <tt>AlertLayoutStrategy</tt>.
     *
     * @param priority the field to display the priority and colour
     */
    public AlertLayoutStrategy(TextField priority) {
        this.priority = priority;
        priority.setWidth(new Extent(15, Extent.EX));
        priority.setEnabled(false);
    }

    /**
     * Creates a set of components to be rendered from the supplied descriptors.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param context     the layout context
     * @return the components
     */
    @Override
    protected ComponentSet createComponentSet(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                                              LayoutContext context) {
        ComponentSet set = super.createComponentSet(object, descriptors, properties, context);

        Lookup lookup = AlertHelper.getAlertType((Act) object);

        ComponentState alertType = set.get("alertType");
        if (lookup != null && alertType != null) {
            initAlertType(lookup, alertType.getComponent());
        }

        ComponentState priority = getPriority(lookup);
        int index = set.indexOf("alertType");
        if (index >= 0) {
            set.add(index + 1, priority);
        } else {
            set.add(priority);
        }
        return set;
    }

    /**
     * Sets the background/foreground of the alert type field, if it is a text field.
     *
     * @param alertType the alert type
     * @param component the component to display the alert type
     */
    private void initAlertType(Lookup alertType, Component component) {
        if (component instanceof TextField) {
            Color background = AlertHelper.getColour(alertType);
            Color foreground = ColourHelper.getTextColour(background);
            component.setBackground(background);
            component.setForeground(foreground);
        }
    }

    /**
     * Returns the component state of the priority field.
     *
     * @param alertType the alert type lookup. May be <tt>null</tt>
     * @return the priority field, populated with the alert type
     */
    private ComponentState getPriority(Lookup alertType) {
        String displayName = DescriptorHelper.getDisplayName("lookup.customerAlertType", "priority");
        if (alertType != null) {
            priority.setText(AlertHelper.getPriorityName(alertType));
        }
        return new ComponentState(priority, null, null, displayName);
    }
}
