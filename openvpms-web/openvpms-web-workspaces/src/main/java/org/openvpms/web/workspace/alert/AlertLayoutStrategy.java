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

package org.openvpms.web.workspace.alert;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.text.TextField;

import java.util.List;


/**
 * Layout strategy for <em>act.customerAlert</em> and <em>act.patientAlert</em>.
 * This includes a field to display the associated alert type's priority and colour.
 *
 * @author Tim Anderson
 */
public class AlertLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The field to display the alert priority and colour.
     */
    private TextField priority;

    /**
     * The alert.
     */
    private Alert alert;


    /**
     * Constructs a {@link AlertLayoutStrategy}.
     */
    public AlertLayoutStrategy() {
        this(TextComponentFactory.create());
    }

    /**
     * Constructs a {@link AlertLayoutStrategy}.
     *
     * @param priority the field to display the priority and colour
     */
    public AlertLayoutStrategy(TextField priority) {
        this.priority = priority;
        priority.setWidth(new Extent(15, Extent.EX));
        priority.setEnabled(false);
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
        alert = Alert.create((Act) object);
        if (alert != null) {
            addComponent(createAlert(alert, object, properties, context));
        }
        return super.apply(object, properties, parent, context);
    }

    protected ComponentSet createComponentSet(IMObject object, List<Property> properties, LayoutContext context) {
        ComponentSet set = super.createComponentSet(object, properties, context);
        if (alert != null) {
            ComponentState priority = getPriority(alert);
            int index = set.indexOf("alertType");
            if (index >= 0) {
                set.add(index + 1, priority);
            } else {
                set.add(priority);
            }
        }
        return set;
    }

    /**
     * Creates a component for the alert type.
     *
     * @param alert      the alert
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     * @return the component
     */
    protected ComponentState createAlert(Alert alert, IMObject object, PropertySet properties, LayoutContext context) {
        ComponentState alertType;
        if (TypeHelper.isA(alert.getAlertType(), PatientArchetypes.ALERT)) {
            alertType = createComponent(properties.get("alertType"), object, context);
        } else {
            alertType = createComponent(properties.get("alertType"), object, context);
        }
        setAlertColour(alert, alertType.getComponent());
        return alertType;
    }

    /**
     * Sets the background/foreground of the alert type field, if it is a text field.
     *
     * @param alert     the alert type
     * @param component the component to display the alert type
     */
    protected void setAlertColour(Alert alert, Component component) {
        if (component instanceof TextField) {
            Color background = alert.getColour();
            if (background != null) {
                Color foreground = alert.getTextColour();
                component.setBackground(background);
                component.setForeground(foreground);
            }
        }
    }

    /**
     * Returns the component state of the priority field.
     *
     * @param alert the alert. May be {@code null}
     * @return the priority field, populated with the alert type
     */
    private ComponentState getPriority(Alert alert) {
        priority.setText(alert.getPriority().getName());
        return new ComponentState(priority, null, null, alert.getPriority().getDisplayName());
    }
}
