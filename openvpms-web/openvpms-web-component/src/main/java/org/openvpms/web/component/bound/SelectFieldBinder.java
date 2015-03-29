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

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.event.ChangeListener;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;

import java.util.List;


/**
 * Helper to bind a property to a select field.
 * <p/>
 * If the property is a collection property, only a single element may be present in the collection.
 *
 * @author Tim Anderson
 */
public class SelectFieldBinder extends Binder {

    /**
     * The component to bind to.
     */
    private SelectField component;

    /**
     * The listener.
     */
    private final ChangeListener listener;

    /**
     * The action listener.
     */
    private final ActionListener actionListener;


    /**
     * Constructs a <tt>SelectFieldBinder</tt>.
     *
     * @param component the component to bind
     * @param property  the property to bind
     */
    public SelectFieldBinder(SelectField component, Property property) {
        super(property, false);  // need to bind after listener created
        this.component = component;
        listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setProperty();
            }
        };
        actionListener = new ActionListener() {
            public void onAction(ActionEvent e) {
            }
        };
        int index = indexOf(property.getValue());
        component.setSelectedIndex(index);

        bind();
    }

    /**
     * Updates the field from the property.
     */
    @Override
    public void setField() {
        Property property = getProperty();
        if (!property.isCollection()) {
            setFieldValue(property.getValue());
        } else {
            List values = ((CollectionProperty) property).getValues();
            if (!values.isEmpty()) {
                setFieldValue(values.get(0));
            }
        }
    }

    /**
     * Updates the property from the field.
     *
     * @param property the property to update
     * @return {@code true} if the property was updated
     */
    @Override
    protected boolean setProperty(Property property) {
        boolean result = false;
        if (!property.isCollection()) {
            result = super.setProperty(property);
        } else {
            // if its a collection property add the selected value to the collection, replacing any existing values
            CollectionProperty collection = (CollectionProperty) property;
            List values = collection.getValues();
            if (component.getSelectedIndex() == -1) {
                // nothing selected, so remove any existing value
                if (!values.isEmpty()) {
                    for (Object value : values) {
                        collection.remove(value);
                    }
                    result = true;
                }
            } else {
                // replace any existing values with the selected value
                Object fieldValue = getFieldValue();
                if (!values.contains(fieldValue)) {
                    for (Object value : values) {
                        collection.remove(value);
                    }
                    if (fieldValue != null) {
                        collection.add(fieldValue);
                    }
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Registers the binder with the property to receive updates.
     */
    @Override
    public void bind() {
        if (!isBound()) {
            super.bind();
            component.getSelectionModel().addChangeListener(listener);
            component.addActionListener(actionListener);
        }
    }

    /**
     * Deregisters the binder from the property.
     */
    @Override
    public void unbind() {
        if (isBound()) {
            super.unbind();
            component.getSelectionModel().removeChangeListener(listener);
            component.removeActionListener(actionListener);
        }
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    protected Object getFieldValue() {
        return component.getSelectedItem();
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value to set
     */
    protected void setFieldValue(Object value) {
        int index = indexOf(value);
        if (index != -1 && component.getSelectedIndex() != index) {
            boolean bound = isBound();
            if (bound) {
                component.getSelectionModel().removeChangeListener(listener);
            }
            component.setSelectedIndex(index);
            if (bound) {
                component.getSelectionModel().addChangeListener(listener);
            }
        }
    }

    /**
     * Returns the index of a value in the list.
     *
     * @param value the value
     * @return the index, or <tt>-1</tt> if the value wasn't found
     */
    private int indexOf(Object value) {
        int result = -1;
        ListModel model = component.getModel();
        for (int i = 0; i < model.size(); ++i) {
            if (ObjectUtils.equals(model.get(i), value)) {
                result = i;
                break;
            }
        }
        return result;
    }

}
