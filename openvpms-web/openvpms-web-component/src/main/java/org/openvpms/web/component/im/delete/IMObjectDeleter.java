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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.delete;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Deletes {@link IMObject}s, or deactivates them, if they cannot be deleted.
 *
 * @author Tim Anderson
 */
public interface IMObjectDeleter<T extends IMObject> {

    /**
     * Attempts to delete an object.
     *
     * @param object   the object to delete
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    void delete(T object, Context context, HelpContext help, IMObjectDeletionListener<T> listener);

}
