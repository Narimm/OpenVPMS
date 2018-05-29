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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.delete;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Abstract implementation of the {@link IMObjectDeleter} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectDeleter<T extends IMObject> implements IMObjectDeleter<T> {

    /**
     * The deletion handler factory.
     */
    private final IMObjectDeletionHandlerFactory factory;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractIMObjectDeleter.class);

    /**
     * Constructs an {@link AbstractIMObjectDeleter}.
     *
     * @param factory the deletion handler factory
     */
    public AbstractIMObjectDeleter(IMObjectDeletionHandlerFactory factory) {
        this.factory = factory;
    }

    /**
     * Attempts to delete an object.
     *
     * @param object   the object to delete
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    @Override
    public void delete(T object, Context context, HelpContext help, IMObjectDeletionListener<T> listener) {
        try {
            IMObjectDeletionHandler<T> handler = factory.create(object);
            if (handler.canDelete()) {
                delete(handler, context, help, listener);
            } else if (object.isActive()) {
                if (handler.canDeactivate()) {
                    deactivate(handler, listener, help);
                } else {
                    unsupported(object, help);
                }
            } else {
                deactivated(object, help);
            }
        } catch (Throwable exception) {
            log.error("Failed to delete object=" + object.getObjectReference() + ": " + exception.getMessage(),
                      exception);
            listener.failed(object, exception);
        }
    }

    /**
     * Invoked to delete an object.
     *
     * @param handler  the deletion handler
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    protected abstract void delete(IMObjectDeletionHandler<T> handler, Context context, HelpContext help,
                                   IMObjectDeletionListener<T> listener);

    /**
     * Invoked to deactivate an object.
     *
     * @param handler  the deletion handler
     * @param listener the listener
     * @param help     the help context
     */
    protected abstract void deactivate(IMObjectDeletionHandler<T> handler, IMObjectDeletionListener<T> listener,
                                       HelpContext help);

    /**
     * Invoked when an object cannot be de deleted, and has already been deactivated.
     *
     * @param object the object
     * @param help   the help context
     */
    protected abstract void deactivated(T object, HelpContext help);

    /**
     * Invoked when deletion and deactivation of an object is not supported.
     *
     * @param object the object
     * @param help   the help context
     */
    protected abstract void unsupported(T object, HelpContext help);

    /**
     * Performs deletion.
     *
     * @param handler  the deletion handler
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    protected void doDelete(IMObjectDeletionHandler<T> handler, Context context, HelpContext help,
                            final IMObjectDeletionListener<T> listener) {
        T object = handler.getObject();
        try {
            handler.delete(context, help);
            listener.deleted(object);
        } catch (Throwable exception) {
            log.error("Failed to delete object=" + object.getObjectReference() + ": " + exception.getMessage(),
                      exception);
            listener.failed(object, exception);
        }
    }

    /**
     * Performs deactivation.
     *
     * @param handler  the deletion handler
     * @param listener the listener to notify
     */
    protected void doDeactivate(IMObjectDeletionHandler<T> handler, IMObjectDeletionListener<T> listener) {
        T object = handler.getObject();
        try {
            handler.deactivate();
            listener.deactivated(object);
        } catch (Throwable exception) {
            log.error("Failed to deactivate object=" + object.getObjectReference() + ": " + exception.getMessage(),
                      exception);
            listener.failed(object, exception);
        }
    }

}
