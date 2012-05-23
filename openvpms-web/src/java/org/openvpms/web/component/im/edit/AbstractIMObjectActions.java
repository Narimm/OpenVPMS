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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Abstract implementation of {@link IMObjectActions}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectActions<T extends IMObject> implements IMObjectActions<T> {

    /**
     * Determines if an object can be edited.
     *
     * @param object the object to check
     * @return {@code true} if the object can be edited
     */
    public boolean canEdit(T object) {
        return object != null;
    }

    /**
     * Determines if an object can be deleted.
     *
     * @param object the object to check
     * @return {@code true} if the object can be deleted
     */
    public boolean canDelete(T object) {
        return object != null;
    }
}
