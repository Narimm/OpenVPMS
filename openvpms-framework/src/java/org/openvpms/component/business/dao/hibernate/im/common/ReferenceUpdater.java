/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.model.object.Reference;


/**
 * A <tt>ReferenceAssembler</tt> is used to update new
 * {@link IMObjectReference}s with their persistent versions.
 *
 * @author Tim Anderson
 */
public abstract class ReferenceUpdater {

    /**
     * The reference to update.
     */
    private final Reference reference;

    /**
     * Creates a new <tt>ReferenceUpdater</tt>.
     *
     * @param state     the state to update
     * @param reference the reference to update
     */
    public ReferenceUpdater(DOState state, Reference reference) {
        state.addReferenceUpdater(this);
        this.reference = reference;
    }

    /**
     * Returns the reference that needs to be updated.
     *
     * @return the reference
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * Updates the reference.
     *
     * @param updated the updated value
     */
    public void update(IMObjectReference updated) {
        doUpdate(updated);
    }

    /**
     * Reverts the reference to its prior value.
     */
    public void revert() {
        doUpdate(reference);
    }

    /**
     * Updates the reference.
     *
     * @param updated the updated value
     */
    protected abstract void doUpdate(Reference updated);

}
