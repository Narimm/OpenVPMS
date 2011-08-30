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

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.TextDocument;

import java.text.Format;
import java.text.ParseException;


/**
 * Binds a {@link Property} to a <tt>TextField</tt>, providing formatting.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundFormattedField extends TextField {

    /**
     * The formatter.
     */
    private final Format format;


    /**
     * Construct a new <tt>BoundFormattedField</tt>.
     *
     * @param property the property to bind
     * @param format   the formatter
     */
    public BoundFormattedField(Property property, Format format) {
        super(new TextDocument());
        this.format = format;
        Binder binder = new FormattingBinder(this, property);
        binder.setField();
    }

    /**
     * Construct a new <tt>BoundFormattedField</tt>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display.
     * @param format   the formatter
     */
    public BoundFormattedField(Property property, int columns,
                               Format format) {
        this(property, format);
        setWidth(new Extent(columns, Extent.EX));
    }

    /**
     * Parses the field value.
     *
     * @return the parsed value, or <tt>value</tt> if it can't be parsed
     */
    protected Object parse(String value) {
        Object result = null;
        if (value != null) {
            try {
                result = format.parseObject(value);
            } catch (ParseException exception) {
                // failed to parse, so return the field unchanged
                result = value;
            }
        }
        return result;
    }

    /**
     * Returns the format.
     *
     * @return the format
     */
    protected Format getFormat() {
        return format;
    }

    private class FormattingBinder extends TextComponentBinder {

        /**
         * Construct a new <tt>FormattingtBinder</tt>.
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
            String value = (String) super.getFieldValue();
            return parse(value);
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
                    value = format.format(value);
                } catch (IllegalArgumentException ignore) {
                    // failed to format, so set the field unchanged
                }
            }
            super.setFieldValue(value);
        }
    }

}
