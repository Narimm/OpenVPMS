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
 * Handles {@link IMObject} deletion and deactivation.
 *
 * @author Tim Anderson
 */
public interface IMObjectDeletionHandler<T extends IMObject> {

    /**
     * Returns the object to delete.
     *
     * @return the object to delete
     */
    T getObject();

    /**
     * Determines if the object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    boolean canDelete();

    /**
     * Deletes the object.
     *
     * @param context     the context
     * @param helpContext the help context
     * @throws IllegalStateException if the object cannot be deleted
     */
    void delete(Context context, HelpContext helpContext);

    /**
     * Determines if the object can be deactivated.
     *
     * @return {@code true} if the object can be deactivated
     */
    boolean canDeactivate();

    /**
     * Deactivates the object.
     *
     * @throws IllegalStateException if the object cannot be deleted
     */
    void deactivate();

}
