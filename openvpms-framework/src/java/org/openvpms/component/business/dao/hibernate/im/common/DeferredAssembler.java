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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DeferredAssembler extends AbstractAssembler {

    private final DOState state;
    private IMObjectReference reference;

    public DeferredAssembler(DOState state, IMObjectReference reference) {
        this.state = state;
        this.reference = reference;
        state.addDeferred(this);
    }

    public IMObjectDO getObject() {
        return state.getObject();
    }

    public IMObjectReference getReference() {
        return reference;
    }

    public void assemble(Context context) {
        doAssemble(context);
        state.removeDeferred(this);
    }

    protected abstract void doAssemble(Context context);
}
