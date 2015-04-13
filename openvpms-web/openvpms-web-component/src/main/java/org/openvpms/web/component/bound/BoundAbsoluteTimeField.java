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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextDocument;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;

/**
 * A time field that restricts entered times to the range 0:00..24:00.
 *
 * @author Tim Anderson
 */
public class BoundAbsoluteTimeField extends TextField {

    /**
     * The component binder.
     */
    private final Binder binder;

    /**
     * Constructs a {@link BoundAbsoluteTimeField}.
     *
     * @param property the property to bind
     */
    public BoundAbsoluteTimeField(Property property) {
        super(new TextDocument());
        setStyleName("default");
        binder = new FormattingBinder(this, property);
        if (!(property.getTransformer() instanceof TimePropertyTransformer)) {
            property.setTransformer(new TimePropertyTransformer(property, TimePropertyTransformer.MIN_DATE,
                                                                TimePropertyTransformer.MAX_DATE));
        }
        setAlignment(Alignment.ALIGN_RIGHT);
        setWidth(new Extent(5, Extent.EX));
    }

    /**
     * Life-cycle method invoked when the {@code Component} is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the {@code Component} is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }

    private class FormattingBinder extends TextComponentBinder {

        /**
         * Construct a {@link FormattingBinder}.
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
            if (value instanceof Date) {
                value = DateFormatter.formatTimeDiff(TimePropertyTransformer.MIN_DATE, (Date) value);
            }
            super.setFieldValue(value);
        }

        /**
         * Parses the field value.
         *
         * @param value the value to parse
         * @return the parsed value, or {@code value} if it can't be parsed
         */
        protected Object parse(String value) {
            Object result = null;
            if (value != null) {
                try {
                    result = DateFormatter.parseTime(value, true);
                } catch (Throwable exception) {
                    // failed to parse, so return the field unchanged
                    result = value;
                }
            }
            return result;
        }

    }

}
