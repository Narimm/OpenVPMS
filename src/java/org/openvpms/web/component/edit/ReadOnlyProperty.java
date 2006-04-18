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

package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Read-only version of {@link IMObjectProperty}. Attempts to modify the value
 * will result in an {@link UnsupportedOperationException}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
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

    /**
     * Notify any listeners that they need to refresh.
     */
    public void refresh() {
        // no-op
    }
}
