package org.openvpms.web.component.edit;

import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.NodeValidator;


/**
 * A modifiable {@link Property} that validates inputs using an {@link
 * Validator}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ModifiableProperty extends IMObjectProperty {

    /**
     * The validator.
     */
    private final Validator _validator;

    /**
     * Determines if the underlying object is dirty.
     */
    private boolean _dirty;

    /**
     * The listeners.
     */
    private ModifiableListeners _listeners = new ModifiableListeners();


    /**
     * Construct a new <code>IMObjectProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public ModifiableProperty(IMObject object, NodeDescriptor descriptor) {
        super(object, descriptor);
        _validator = new NodeValidator(descriptor);
    }

    /**
     * Set the value of the property.
     *
     * @param value the property value
     */
    @Override
    public void setValue(Object value) {
        List<String> errors = _validator.validate(value);
        if (errors.isEmpty()) {
            super.setValue(value);
            _dirty = true;
            _listeners.notifyListeners(this);
        } else {
            ErrorDialog.show(errors.get(0));
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
}
