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

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.edit.Property;


/**
 * Binds a {@link Property} to a <code>CheckBox</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundCheckBox extends CheckBox {

    /**
     * The property binder.
     */
    private final Binder _binder;

    /**
     * Checkbox listener.
     */
    private final ActionListener _listener;


    /**
     * Construct a new <code>BoundCheckBox</code>.
     *
     * @param property the property to bind
     */
    public BoundCheckBox(Property property) {
        _listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _binder.setProperty();
            }
        };

        _binder = new Binder(property) {
            protected Object getFieldValue() {
                return isSelected();
            }

            protected void setFieldValue(Object value) {
                if (value != null) {
                    removeActionListener(_listener);
                    setSelected((Boolean) value);
                    addActionListener(_listener);
                }
            }
        };
        _binder.setField();

    }

}
