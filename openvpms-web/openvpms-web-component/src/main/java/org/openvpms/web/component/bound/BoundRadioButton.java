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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import net.sf.jasperreports.engine.util.ObjectUtils;
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
public class BoundRadioButton extends RadioButton implements BoundProperty {

    /**
     * The value to set when the button is selected.
     */
    private final Object selectionValue;

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
     * Constructs a {@link BoundRadioButton}.
     *
     * @param property the property to bind
     * @param group    the button group
     * @param value    the value to set when the button is selected
     */
    public BoundRadioButton(Property property, ButtonGroup group, Object value) {
        this.selectionValue = value;
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

            /**
             * Updates the property from the field, if the button is selected.
             *
             * @param property the property to update
             * @return {@code true} if the property was updated
             */
            @Override
            protected boolean setProperty(Property property) {
                boolean result = false;
                if (isSelected()) {
                    result = super.setProperty(property);
                }
                return result;
            }

            protected Object getFieldValue() {
                return isSelected() ? BoundRadioButton.this.selectionValue : null;
            }

            protected void setFieldValue(Object value) {
                boolean selected = ObjectUtils.equals(value, selectionValue);
                removeActionListener(actionListener);
                removeChangeListener(listener);
                setSelected(selected);
                addChangeListener(listener);
                addActionListener(actionListener);
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
     * Life-cycle method invoked when the {@code Component} is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    @Override
    public Property getProperty() {
        return binder.getProperty();
    }

}
