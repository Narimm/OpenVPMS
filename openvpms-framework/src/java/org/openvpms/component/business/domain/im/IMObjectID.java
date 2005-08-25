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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.domain.im;

import org.openehr.rm.support.identification.ObjectID;

/**
 * This class extends {@link ObjectID} and represents a unique identity.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class IMObjectID extends ObjectID {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param value
     */
    public IMObjectID(String value) {
        super(value);
    }

    /* (non-Javadoc)
     * @see org.openehr.rm.support.identification.ObjectID#versionID()
     */
    @Override
    public String versionID() {
        // not implemented
        return null;
    }
}
