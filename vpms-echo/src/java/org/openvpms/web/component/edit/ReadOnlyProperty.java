package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Read-only version of {@link IMObjectProperty}. Attempts to modify the value
 * will result in an {@link UnsupportedOperationException}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ReadOnlyProperty extends IMObjectProperty {

    /**
     * Construct a new <code>ReadOnlyProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public ReadOnlyProperty(IMObject object, NodeDescriptor descriptor) {
        super(object, descriptor);
    }

    /**
     * @throws UnsupportedOperationException if invoked
     */
    public void setValue(Object value) {
        throw new UnsupportedOperationException(
                "Attenpt to modify read-only property:"
                + getDescriptor().getDisplayName());
    }

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    public void add(Object value) {
        throw new UnsupportedOperationException(
                "Attenpt to modify read-only property:"
                + getDescriptor().getDisplayName());
    }

    /**
     * Remove a value.
     *
     * @param value the value to remove
     */
    public void remove(Object value) {
        throw new UnsupportedOperationException(
                "Attenpt to modify read-only property:"
                + getDescriptor().getDisplayName());
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        return false;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        // no-op
    }

    /**
     * Add a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        return true;
    }
}
