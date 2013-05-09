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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Color;
import org.openvpms.web.component.echo.ColorSelect;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.colour.ColourHelper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Binds a property to a <tt>ColorSelect</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundColorSelect extends ColorSelect {

    /**
     * The property binder.
     */
    private final Binder binder;
    private PropertyChangeListener listener;


    /**
     * Construct a new <tt>BoundColorSelect</tt>.
     *
     * @param property the property to bind
     */
    public BoundColorSelect(Property property) {
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                binder.setProperty();
            }
        };
        addPropertyChangeListener(listener);
        binder = new Binder(property) {
            protected Object getFieldValue() {
                return ColourHelper.getString(getColor());
            }

            protected void setFieldValue(Object value) {
                Color color = convert(value);
                try {
                    removePropertyChangeListener(listener);
                    setColor(color);
                } finally {
                    addPropertyChangeListener(listener);
                }
            }

        };
    }

    /**
     * Life-cycle method invoked when the <code>Component</code> is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }

    private Color convert(Object value) {
        return value != null ? ColourHelper.getColor(value.toString()) : null;
    }
}
