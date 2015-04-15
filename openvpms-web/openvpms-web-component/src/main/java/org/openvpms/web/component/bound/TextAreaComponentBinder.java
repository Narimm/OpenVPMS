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

import nextapp.echo2.app.Extent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.echo.text.TextArea;


/**
 * Helper to bind a property to a text area component.
 *
 * @author Tim Anderson
 */
public class TextAreaComponentBinder extends TextComponentBinder {

    /**
     * Constructs a <tt>TextAreaComponentBinder</tt>.
     * <p/>
     * If not already present, the property is associated with an {@link StringPropertyTransformer}
     * that doesn't trim leading and trailing spaces or new lines.
     *
     * @param property  the property to bind
     * @param component the component to bind
     */
    public TextAreaComponentBinder(TextArea component, Property property) {
        super(component, property);
        if (!StringUtils.isEmpty(property.getDescription())) {
            component.setToolTipText(property.getDescription());
        }
        PropertyTransformer transformer = property.getTransformer();
        if (!(transformer instanceof StringPropertyTransformer)) {
            property.setTransformer(new StringPropertyTransformer(property, false));
        } else {
            ((StringPropertyTransformer) transformer).setTrim(false);
        }
    }

    /**
     * Constructs a <tt>TextAreaComponentBinder</tt>.
     * <p/>
     * If not already present, the property is associated with an {@link StringPropertyTransformer}
     * that doesn't trim leading and trailing spaces or new lines.
     *
     * @param component the component to bind
     * @param property  the property to bind
     * @param columns   the number of columns to display
     * @param rows      the number of rows to display
     */
    public TextAreaComponentBinder(TextArea component, Property property, int columns, int rows) {
        this(component, property);
        component.setWidth(new Extent(columns, Extent.EX));
        component.setHeight(new Extent(rows, Extent.EM));
    }
}