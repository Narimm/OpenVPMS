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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.event.ChangeListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;


/**
 * Binds a {@link Property} to a {@code RadioButton}.
 *
 * @author Tim Anderson
 */
public class BoundRadioButton extends RadioButton {

    /**
     * The property binder.
     */
    private final Binder binder;

    /**
     * State change listener.
     */
    private ChangeListener listener;

    /**
     * Checkbox listener.
     */
    private final ActionListener actionListener;


    /**
     * Construct a new <tt>BoundCheckBox</tt>.
     *
     * @param property the property to bind
     * @param group    the button group
     */
    public BoundRadioButton(Property property, ButtonGroup group) {
        setGroup(group);

        listener = new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                binder.setProperty();
            }
        };
        actionListener = new ActionListener() {
            public void onAction(ActionEvent event) {
            }
        };
        addChangeListener(listener);
        addActionListener(actionListener);

        binder = new Binder(property) {
            protected Object getFieldValue() {
                return isSelected();
            }

            protected void setFieldValue(Object value) {
                if (value != null) {
                    removeActionListener(actionListener);
                    removeChangeListener(listener);
                    setSelected((Boolean) value);
                    addChangeListener(listener);
                    addActionListener(actionListener);
                }
            }
        };
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
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

}
