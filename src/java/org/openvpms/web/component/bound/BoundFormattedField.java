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

package org.openvpms.web.component.bound;

import java.text.Format;
import java.text.ParseException;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.web.component.edit.Property;


/**
 * Binds a {@link Property} to a <code>TextField</code>, providing formatting.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundFormattedField extends TextField {

    /**
     * The formatter.
     */
    private final Format _format;


    /**
     * Construct a new <code>BoundFormattedField</code>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display.
     * @param format   the formatter
     */
    public BoundFormattedField(Property property, int columns,
                               Format format) {
        setWidth(new Extent(columns, Extent.EX));
        _format = format;
        Binder binder = new FormattingBinder(this, property);
        binder.setField();
    }

    private class FormattingBinder extends TextComponentBinder {

        /**
         * Construct a new <code>FormattingtBinder</code>.
         *
         * @param component the component to bind
         * @param property  the property to bind
         */
        public FormattingBinder(TextComponent component, Property property) {
            super(component, property);
        }

        /**
         * Returns the value of the field.
         *
         * @return the value of the field
         */
        @Override
        protected Object getFieldValue() {
            Object result = null;
            String value = (String) super.getFieldValue();
            if (value != null) {
                try {
                    result = _format.parseObject(value);
                } catch (ParseException exception) {
                    // failed to parse, so return the field unchanged
                    result = value;
                }
            }
            return result;
        }

        /**
         * Sets the value of the field.
         *
         * @param value the value to set
         */
        @Override
        protected void setFieldValue(Object value) {
            if (value != null) {
                try {
                    value = _format.format(value);
                } catch (IllegalArgumentException ignore) {
                    // failed to format, so set the field unchanged
                }
            }
            super.setFieldValue(value);
        }
    }

}
