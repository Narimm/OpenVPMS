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
 * Implementation of {@link AbstractIMObjectDeleter} that doesn't prompt for confirmation.
 *
 * @author Tim Anderson
 */
public class SilentIMObjectDeleter<T extends IMObject> extends AbstractIMObjectDeleter<T> {

    /**
     * Constructs a {@link SilentIMObjectDeleter}.
     *
     * @param factory the deletion handler factory
     */
    public SilentIMObjectDeleter(IMObjectDeletionHandlerFactory factory) {
        super(factory);
    }

    /**
     * Invoked to delete an object.
     *
     * @param handler  the deletion handler
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    @Override
    protected void delete(IMObjectDeletionHandler<T> handler, Context context, HelpContext help,
                          IMObjectDeletionListener<T> listener) {
        doDelete(handler, context, help, listener);
    }

    /**
     * Invoked to deactivate an object.
     *
     * @param handler  the deletion handler
     * @param listener the listener
     * @param help     the help context
     */
    @Override
    protected void deactivate(IMObjectDeletionHandler<T> handler, IMObjectDeletionListener<T> listener,
                              HelpContext help) {
        doDeactivate(handler, listener);
    }

    /**
     * Invoked when an object cannot be de deleted, and has already been deactivated.
     *
     * @param object the object
     * @param help   the help context
     */
    @Override
    protected void deactivated(T object, HelpContext help) {
        // no-op
    }

    /**
     * Invoked when deletion and deactivation of an object is not supported.
     *
     * @param object  the deletion handler
     * @param help*   the help context
     */
    @Override
    protected void unsupported(T object, HelpContext help) {
        // no-op
    }
}
