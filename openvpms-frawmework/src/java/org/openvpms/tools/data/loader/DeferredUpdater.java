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

import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Helper to defer update of an object until the object references that it
 * depends on can be resolved.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DeferredUpdater {

    /**
     * The identifier of the object that is required.
     */
    private final String id;


    /**
     * Creates a new <tt>DeferredUpdater</tt>.
     *
     * @param id the object identifier
     */
    public DeferredUpdater(String id) {
        this.id = LoadCache.stripPrefix(id);
    }

    /**
     * Returns the object identifier.
     *
     * @return the object identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the object.
     *
     * @param reference the reference of the required object
     * @param context the load context
     * @return <tt>true</tt> if the update was successful
     */
    public abstract boolean update(IMObjectReference reference,
                                   LoadContext context);

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj
     *         argument; <tt>false</tt> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DeferredUpdater) {
            return ((DeferredUpdater) obj).getId().equals(id);

        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
