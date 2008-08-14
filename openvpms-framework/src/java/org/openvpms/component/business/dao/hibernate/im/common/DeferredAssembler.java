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

package org.openvpms.component.business.dao.hibernate.im.common;

import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * A <tt>DeferredAssembler</tt> is used to delay assembly of an
 * {@link IMObjectDO} until an {@link IMObjectReference} can be resolved.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DeferredAssembler extends AbstractAssembler {

    /**
     * The data object state.
     */
    private final DOState state;

    /**
     * The reference that the data object is dependent on.
     */
    private IMObjectReference reference;

    /**
     * Creates a new <tt>DeferredAssembler</tt>.
     *
     * @param state     the data object state
     * @param reference the reference that the data object is dependent on
     */
    public DeferredAssembler(DOState state, IMObjectReference reference) {
        this.state = state;
        this.reference = reference;
        state.addDeferred(this);
    }

    /**
     * Returns the data object.
     *
     * @return the data object
     */
    public IMObjectDO getObject() {
        return state.getObject();
    }

    /**
     * Returns the reference that the data object is dependent on.
     *
     * @return the reference
     */
    public IMObjectReference getReference() {
        return reference;
    }

    /**
     * Assembles the object.
     * <p/>
     * This is invoked when the reference can be resolved via the
     * {@link Context}.
     *
     * @param context the assembly context
     */
    public void assemble(Context context) {
        doAssemble(context);
        state.removeDeferred(this);
    }

    /**
     * Assembles the object.
     *
     * @param context the assembly context
     */
    protected abstract void doAssemble(Context context);
}
