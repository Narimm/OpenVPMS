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

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Abstract implementation of the {@link IArchetypeServiceListener} interface.
 * <p/>
 * This provides empty implementations for each method.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractArchetypeServiceListener
        implements IArchetypeServiceListener {

    /**
     * Invoked prior to an object being saved.
     *
     * @param object the object being saved
     */
    public void save(IMObject object) {
    }

    /**
     * Invoked prior to an object being removed.
     *
     * @param object the object being removed
     */
    public void remove(IMObject object) {
    }

    /**
     * Invoked after an object has been saved and the transaction committed.
     *
     * @param object the saved object
     */
    public void saved(IMObject object) {
    }

    /**
     * Invoked after an object has been removed and the transaction committed.
     *
     * @param object the removed object
     */
    public void removed(IMObject object) {
    }

    /**
     * Invoked on transaction rollback.
     *
     * @param object the object that was rolled back
     */
    public void rollback(IMObject object) {
    }
}
