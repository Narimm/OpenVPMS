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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.tools.data.loader;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Helper to update nodes with object references with their persistent values.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class UnsavedRefUpdater {

    /**
     * The load state.
     */
    private final LoadState state;

    /**
     * The unsaved reference.
     */
    private final IMObjectReference reference;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor descriptor;


    /**
     * Creates a new <tt>UnsavedRefUpdater</tt>.
     *
     * @param state      the load state
     * @param reference  the unsaved reference
     * @param descriptor the node descriptor
     */
    public UnsavedRefUpdater(LoadState state, IMObjectReference reference,
                             NodeDescriptor descriptor) {
        this.state = state;
        this.reference = reference;
        this.descriptor = descriptor;
    }

    /**
     * Returns the object reference.
     *
     * @return the object reference
     */
    public IMObjectReference getRefeference() {
        return reference;
    }

    /**
     * Updates the node with the saved reference.
     *
     * @param reference the saved reference
     */
    public void update(IMObjectReference reference) {
        descriptor.setValue(state.getObject(), reference);
        state.removeUnsaved(reference);
    }
}
