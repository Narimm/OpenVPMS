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

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;


/**
 * Binds a {@link Property} to a <code>SelectField</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundSelectField extends SelectField {

    /**
     * The property binder.
     */
    private final Binder binder;

    /**
     * Checkbox listener.
     */
    private final ActionListener listener;


    /**
     * Construct a new <code>BoundSelectField</code>.
     *
     * @param property the property to bind
     * @param model    the list model
     */
    public BoundSelectField(final Property property, final ListModel model) {
        super(model);
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                binder.setProperty();
            }
        };
        addActionListener(listener);

        binder = new Binder(property) {
            protected Object getFieldValue() {
                return getSelectedItem();
            }

            protected void setFieldValue(Object value) {
                if (value != null) {
                    removeActionListener(listener);
                    int index = setSelected(value);
                    if (index == -1 && model.size() != 0) {
                        // current value not in the list, so default it to the first
                        // list value.
                        setSelectedIndex(0);
                        property.setValue(getSelectedItem());
                    }
                    addActionListener(listener);
                }
            }
        };
        binder.setField();
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }

    }

    /**
     * Sets the selected object based on the supplied value.
     *
     * @param value the value
     * @return the selected index, or <code>-1</code> if the value wasn't found
     *         in the list
     */
    private int setSelected(Object value) {
        int result = -1;
        ListModel model = getModel();
        for (int i = 0; i < model.size(); ++i) {
            if (ObjectUtils.equals(model.get(i), value)) {
                setSelectedIndex(i);
                result = i;
                break;
            }
        }
        return result;
    }

}
