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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * Helper to bind a property to a field.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class Binder {

    /**
     * The property.
     */
    private Property property;

    /**
     * Listener for property updates.
     */
    private ModifiableListener listener;

    /**
     * Determines if <tt>listener</tt> has been registered with the property.
     */
    private boolean hasListener = false;

    /**
     * Determines if the bind() method has been invoked to bind the field to the property.
     */
    private boolean bound = false;


    /**
     * Constructs a <tt>Binder</tt>.
     * <p/>
     * This binds to the property.
     *
     * @param property the property to bind
     */
    public Binder(Property property) {
        this(property, true);
    }

    /**
     * Constructs a <tt>Binder</tt>
     *
     * @param property the property to bind
     * @param bind     if <tt>true</tt> bind the property
     */
    public Binder(Property property, boolean bind) {
        this.property = property;
        listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                setField();
            }
        };
        if (bind) {
            bind();
        }
    }

    /**
     * Updates the property from the field.
     */
    public void setProperty() {
        boolean listener = hasListener;
        if (hasListener) {
            // remove the listener to avoid cyclic notifications
            removeModifiableListener();
        }
        try {
            setProperty(property);
        } finally {
            if (listener) {
                addModifiableListener();
            }
        }
    }

    /**
     * Updates the field from the property.
     */
    public void setField() {
        setFieldValue(property.getValue());
    }

    /**
     * Registers the binder with the property to receive updates.
     */
    public void bind() {
        if (!bound) {
            setField(); // update the field from the property
            addModifiableListener();
            bound = true;
        }
    }

    /**
     * Deregisters the binder from the property.
     */
    public void unbind() {
        if (bound) {
            removeModifiableListener();
            bound = false;
        }
    }

    /**
     * Updates the property from the field.
     *
     * @param property the propery to update
     */
    protected void setProperty(Property property) {
        Object fieldValue = getFieldValue();
        if (property.setValue(fieldValue)) {
            Object propertyValue = property.getValue();
            if (!ObjectUtils.equals(fieldValue, propertyValue)) {
                setField();
            }
        }
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    protected abstract Object getFieldValue();

    /**
     * Sets the value of the field.
     *
     * @param value the value to set
     */
    protected abstract void setFieldValue(Object value);

    /**
     * Returns the property.
     *
     * @return the property, or <tt>null</tt> if the binder has been disposed
     */
    protected Property getProperty() {
        return property;
    }

    /**
     * Determines if the binder is bound to the property.
     *
     * @return <tt>true</tt> if the binder is bound, otherwise <tt>false</tt>
     */
    protected boolean isBound() {
        return bound;
    }

    /**
     * Registers the listener with the property, to receive notification when the property changes.
     */
    private void addModifiableListener() {
        property.addModifiableListener(listener);
        hasListener = true;
    }

    /**
     * Deregisters the listener from the property.
     */
    private void removeModifiableListener() {
        property.removeModifiableListener(listener);
        hasListener = false;
    }
}
