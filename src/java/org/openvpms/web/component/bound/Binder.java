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
     * Constructs a <tt>Binder</tt>.
     *
     * @param property the property to bind
     */
    public Binder(Property property) {
        this.property = property;
        listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                setField();
            }
        };
        this.property.addModifiableListener(listener);
    }

    /**
     * Updates the property from the field.
     */
    public void setProperty() {
        property.removeModifiableListener(listener);
        try {
            setProperty(property);
        } finally {
            if (property != null) {  // binder may have been disposed
                property.addModifiableListener(listener);
            }
        }
    }

    /**
     * Updates the field from the property.
     */
    public void setField() {
        if (property != null) {
            setFieldValue(property.getValue());
        }
    }

    /**
     * Disposes this binder.
     * <p/>
     * After disposal, the binder is invalid
     */
    public void dispose() {
        if (property != null) {
            property.removeModifiableListener(listener);
            property = null;
            listener = null;
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
}
