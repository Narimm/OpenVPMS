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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Used to defer population of {@link IMObjectReference}s in an {@link IMObject}
 * when the source {@link IMObjectDO} is uninitialized (i.e has yet to be
 * retrieved from the database).
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DeferredReference {

    /**
     * The unitialized object.
     */
    private final IMObjectDO object;

    /**
     * The type of the object.
     */
    private final Class<? extends IMObjectDOImpl> type;

    /**
     * Creates a new <tt>DeferredReference</tt>.
     *
     * @param object the uninitialized object
     * @param type   the object type
     */
    public DeferredReference(IMObjectDO object,
                             Class<? extends IMObjectDOImpl> type) {
        this.object = object;
        this.type = type;
    }

    /**
     * Returns the object.
     *
     * @return the object
     */
    public IMObjectDO getObject() {
        return object;
    }

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public Class<? extends IMObjectDOImpl> getType() {
        return type;
    }

    /**
     * Populates the resolved reference.
     *
     * @param reference the reference
     */
    public abstract void update(IMObjectReference reference);
}
