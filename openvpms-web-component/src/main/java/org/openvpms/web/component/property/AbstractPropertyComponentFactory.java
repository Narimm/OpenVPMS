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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextField;


/**
 * Abstract implementation of the {@link PropertyComponentFactory} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPropertyComponentFactory
        implements PropertyComponentFactory {

    /**
     * The style name to use.
     */
    private final String style;


    /**
     * Creates a new <tt>AbstractPropertyComponentFactory</tt>.
     *
     * @param style the style name to use
     */
    public AbstractPropertyComponentFactory(String style) {
        this.style = style;
    }

    /**
     * Creates components for boolean, string, numeric and date properties.
     *
     * @param property the property
     * @return a new component, or <tt>null</tt> if the property isn't supported
     */
    public Component create(Property property) {
        if (property.isBoolean()) {
            return createBoolean(property);
        } else if (property.isString()) {
            return createString(property);
        } else if (property.isNumeric()) {
            return createNumeric(property);
        } else if (property.isDate()) {
            return createDate(property);
        }
        return null;
    }

    /**
     * Returns a component bound to a boolean property.
     *
     * @param property the property to bind
     * @return a new component
     */
    protected Component createBoolean(Property property) {
        BoundCheckBox result = new BoundCheckBox(property);
        ComponentFactory.setStyle(result, getStyle());
        return result;
    }

    /**
     * Returns a component bound to a string property.
     * This implementation returns a text field to display the node, or a text
     * area if it is large.
     *
     * @param property the property to bind
     * @return a new component
     */
    protected Component createString(Property property) {
        final int maxDisplayLength = 50;
        int length = property.getMaxLength();
        int maxColumns = (length < maxDisplayLength) ? length : maxDisplayLength;
        return createString(property, maxColumns);
    }

    /**
     * Returns a component bound to a string property.
     * This implementation returns a text field to display the node, or a text
     * area if it is large.
     *
     * @param property the property to bind
     * @param columns  the maximum no, of columns to display
     * @return a new component
     */
    protected Component createString(Property property, int columns) {
        TextComponent result;
        if (property.getMaxLength() > 255) {
            if (property.getMaxLength() < 500) {
                result = BoundTextComponentFactory.createTextArea(property, columns, 5);
            } else {
                result = BoundTextComponentFactory.createTextArea(property, 80, 15);
            }
        } else {
            result = BoundTextComponentFactory.create(property, columns);
        }
        ComponentFactory.setStyle(result, getStyle());
        return result;
    }

    /**
     * Returns a component bound to a numeric property.
     *
     * @param property the property to bind
     * @return a new component
     */
    protected Component createNumeric(Property property) {
        int maxColumns = 10;
        TextField result = BoundTextComponentFactory.createNumeric(property, maxColumns);
        ComponentFactory.setStyle(result, getStyle());
        return result;
    }

    /**
     * Returns a component bound to a date property.
     *
     * @param property the property to bind
     * @return a new component
     */
    protected Component createDate(Property property) {
        DateField result = BoundDateFieldFactory.create(property);
        ComponentFactory.setStyle(result, getStyle());
        ComponentFactory.setStyle(result.getTextField(), getStyle());
        return result;
    }

    /**
     * Returns the component style name.
     *
     * @return the component style name
     */
    protected String getStyle() {
        return style;
    }
}
