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
    private Property _property;

    /**
     * Listener for property updates.
     */
    private final ModifiableListener _listener;

    /**
     * Construct a new <code>Binder</code>.
     *
     * @param property the property to bind
     */
    public Binder(Property property) {
        _property = property;
        _listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                setField();
            }
        };
        _property.addModifiableListener(_listener);
    }

    /**
     * Updates the property from the field.
     */
    public void setProperty() {
        _property.removeModifiableListener(_listener);
        try {
            setProperty(_property);
        } finally {
            _property.addModifiableListener(_listener);
        }
    }

    /**
     * Updates the field from the property.
     */
    public void setField() {
        setFieldValue(_property.getValue());
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
}
