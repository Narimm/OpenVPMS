package org.openvpms.web.component.edit;

import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.dialog.ErrorDialog;


/**
 * A modifiable {@link Property} that validates inputs using an {@link
 * Validator}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ModifiableProperty extends IMObjectProperty {

    /**
     * Determines if the underlying object is dirty.
     */
    private boolean _dirty;

    /**
     * Determines if the object is valid.
     */
    private Boolean _valid;

    /**
     * The listeners.
     */
    private ModifiableListeners _listeners = new ModifiableListeners();

    /**
     * The property handler.
     */
    private final PropertyHandler _handler;


    /**
     * Construct a new <code>IMObjectProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public ModifiableProperty(IMObject object, NodeDescriptor descriptor) {
        super(object, descriptor);
        _handler = PropertyHandlerFactory.create(descriptor);
    }

    /**
     * Set the value of the property.
     *
     * @param value the property value
     */
    @Override
    public void setValue(Object value) {
        try {
            value = _handler.apply(value);
            super.setValue(value);
            _dirty = true;
            _valid = true;
            _listeners.notifyListeners(this);
        } catch (ValidationException exception) {
            _valid = false;
            String title = "Error: " + getDescriptor().getDisplayName();
            String message;
            List<ValidationError> errors = exception.getErrors();
            if (!errors.isEmpty()) {
                ValidationError error = errors.get(0);
                message = error.getErrorMessage();
            } else {
                message = exception.getMessage();
            }
            ErrorDialog.show(title, message);
        }
    }

    /**
     * Determines if the underlying object has been modified.
     *
     * @return <code>true</code> if this has been modified; otherwise
     *         <code>false</code>
     */
    public boolean isModified() {
        return _dirty;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _dirty = false;
    }

    /**
     * Notify any listeners that they need to refresh.
     */
    public void refresh() {
        _listeners.notifyListeners(this);
    }

    /**
     * Add a listener to be notified when a this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        _listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        _listeners.removeListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        if (_valid == null) {
            _valid = _handler.isValid(getValue());
        }
        return _valid;
    }

}
