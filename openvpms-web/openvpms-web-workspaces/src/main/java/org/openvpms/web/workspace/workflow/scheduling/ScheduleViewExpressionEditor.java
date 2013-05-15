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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.workflow.scheduling;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.property.DefaultPropertyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyComponentFactory;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.text.TextHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Expression editor for schedule view expressions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ScheduleViewExpressionEditor {

    /**
     * The expression.
     */
    private final Property expression;

    /**
     * The available expression properties.
     */
    private final List<Property> properties;

    /**
     * The component.
     */
    private final Component component;


    /**
     * Creates a new <tt>ScheduleViewExpressionEditor</tt>.
     *
     * @param expression  the expression property to edit
     * @param arrivalTime if <tt>true</tt> add an arrival time property
     */
    public ScheduleViewExpressionEditor(Property expression,
                                        boolean arrivalTime) {
        this.expression = expression;
        properties = createProperties(arrivalTime);

        PropertyComponentFactory factory
            = DefaultPropertyComponentFactory.INSTANCE;

        Label propertyName = LabelFactory.create();
        propertyName.setText(expression.getDisplayName());
        Component editor = factory.create(expression);

        Label help = LabelFactory.create("scheduleview.expression.help", true);

        Grid expressionGrid = GridFactory.create(2, propertyName, editor,
                                                 LabelFactory.create(), help);

        Grid propertyGrid = GridFactory.create(2);
        for (Property property : properties) {
            if (property.isBoolean() || property.isString()
                || property.isNumeric() || property.isDate()) {
                Component component = factory.create(property);
                Label label = LabelFactory.create();
                label.setText(property.getDisplayName());
                propertyGrid.add(label);
                propertyGrid.add(component);
            }
        }

        GroupBox box = GroupBoxFactory.create(
            "scheduleview.expression.properties", propertyGrid);
        component = ColumnFactory.create("Inset", ColumnFactory.create(
            "WideCellSpacing", expressionGrid, box));
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
     * Tests the expression, displaying the results in a modal dialog.
     */
    public void test() {
        try {
            String result = evaluate();
            InformationDialog.show(getDisplayName(), result);
        } catch (Throwable exception) {
            ErrorHelper.show(exception, false);
        }
    }

    /**
     * Evaluates the expression.
     *
     * @return the evaluated result. May be <tt>null</tt>
     */
    public String evaluate() {
        Object value = expression.getValue();
        String expr = (value != null) ? value.toString() : null;
        if (expr != null) {
            PropertySet set = new ObjectSet();
            for (Property property : properties) {
                set.set(property.getName(), property.getValue());
            }
            return SchedulingHelper.evaluate(expr, set);
        }
        return null;
    }

    /**
     * Returns the display name of the expression.
     *
     * @return the expression's display name
     */
    public String getDisplayName() {
        return expression.getDisplayName();
    }

    /**
     * Creates a list of editable properties for testing the expression.
     *
     * @param arrivalTime if <tt>true</tt> add an arrivalTime property
     * @return the properties
     */
    private List<Property> createProperties(boolean arrivalTime) {
        List<Property> result = new ArrayList<Property>();
        result.add(create(ScheduleEvent.ACT_DESCRIPTION));
        result.add(create(ScheduleEvent.ACT_STATUS));
        result.add(create(ScheduleEvent.ACT_STATUS_NAME));
        result.add(create(ScheduleEvent.ACT_REASON));
        result.add(create(ScheduleEvent.ACT_REASON_NAME));
        result.add(create(ScheduleEvent.CUSTOMER_NAME));
        result.add(create(ScheduleEvent.PATIENT_NAME));
        result.add(create(ScheduleEvent.CLINICIAN_NAME));
        result.add(create(ScheduleEvent.SCHEDULE_NAME));
        result.add(create(ScheduleEvent.SCHEDULE_TYPE_NAME));
        if (arrivalTime) {
            result.add(create(ScheduleEvent.ARRIVAL_TIME, new Date()));
        }
        return result;
    }

    /**
     * Creates a new property with the specified name and value.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new property
     */
    private Property create(String name, String value) {
        SimpleProperty property = new SimpleProperty(name, value, String.class);
        property.setDisplayName(name);
        return property;
    }

    /**
     * Creates a new property with the specified name and value.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new property
     */
    private Property create(String name, Date value) {
        SimpleProperty property = new SimpleProperty(name, value, Date.class);
        property.setDisplayName(name);
        return property;
    }

    /**
     * Creates a new property, deriving the value from its name.
     *
     * @param name the property name
     * @return a new property
     */
    private Property create(String name) {
        String value = TextHelper.unCamelCase(name);
        value = value.replace('.', ' ');
        return create(name, value);
    }

}


