package org.openvpms.web.component.bound;

import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;


/**
 * Helper to bind a property to a field.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
abstract class Binder {

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
        property.setValue(getFieldValue());
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
