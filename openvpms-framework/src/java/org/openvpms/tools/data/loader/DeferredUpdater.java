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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DeferredUpdater {

    private final IMObjectState state;

    private final NodeDescriptor descriptor;

    private final String id;

    public DeferredUpdater(IMObjectState state, NodeDescriptor descriptor,
                           String id) {
        this.state = state;
        this.descriptor = descriptor;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void update(IMObjectReference reference, LoadContext context) {
        state.setReference(descriptor, reference, context);
        state.removeDeferred(this);

    }
}
