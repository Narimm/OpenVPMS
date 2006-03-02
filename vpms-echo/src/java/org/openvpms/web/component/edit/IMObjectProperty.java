package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.ErrorDialog;


/**
 * Represents a property of an {@link IMObject}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class IMObjectProperty implements Property {

    /**
     * The object that the property belongs to.
     */
    private final IMObject _object;

    /**
     * The property descriptor.
     */
    private final NodeDescriptor _descriptor;


    /**
     * Construct a new <code>IMObjectProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public IMObjectProperty(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;
    }

    /**
     * Set the value of the property.
     *
     * @param value the property value
     */
    public void setValue(Object value) {
        try {
            _descriptor.setValue(_object, value);
        } catch (DescriptorException exception) {
            ErrorDialog.show(exception);
        }
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return _descriptor.getValue(_object);
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

}
