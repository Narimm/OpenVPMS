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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Confirms removal of an object from a collection.
 * <p/>
 * Removal may be performed asynchronously, and implementations may choose to veto removal.
 *
 * @author Tim Anderson
 */
public interface RemoveConfirmationHandler {

    /**
     * Confirms removal of an object from a collection.
     * <p/>
     * If approved, it performs the removal.
     *
     * @param object     the object to remove
     * @param collection the collection to remove the object from, if approved
     */
    void remove(IMObject object, IMObjectCollectionEditor collection);
}
