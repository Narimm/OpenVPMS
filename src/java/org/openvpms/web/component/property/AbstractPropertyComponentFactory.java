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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.TextComponentFactory;

import java.text.Format;


/**
 * Abstract implementation of the {@link PropertyComponentFactory} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractPropertyComponentFactory
        implements PropertyComponentFactory {

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
        return new BoundCheckBox(property);
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
            if (property.getMaxLength() < 500)
                result = TextComponentFactory.createTextArea(property, columns,
                                                             5);
            else
                result = TextComponentFactory.createTextArea(property, 90, 15);
        } else {
            result = TextComponentFactory.create(property, columns);
        }
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
        boolean edit = !property.isReadOnly() || property.isDerived();
        Format format = NumberFormatter.getFormat(property, edit);
        TextField text = TextComponentFactory.create(property, maxColumns,
                                                     format);
        if (!edit) {
            Alignment align = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
            text.setAlignment(align);
        }
        return text;
    }

    /**
     * Returns a component bound to a date property.
     *
     * @param property the property to bind
     * @return a new component
     */
    protected Component createDate(Property property) {
        return DateFieldFactory.create(property);
    }
}
