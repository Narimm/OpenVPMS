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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.component.error.ExceptionHelper;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Collection;

/**
 * Abstract implementation of the {@link IMObjectSaveListener} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectSaveListener implements IMObjectSaveListener {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractIMObjectSaveListener.class);

    /**
     * Invoked when a collection of objects are saved.
     *
     * @param objects the saved objects
     */
    @Override
    public void saved(Collection<? extends IMObject> objects) {
    }

    /**
     * Invoked when a collection of objects fail to save.
     *
     * @param objects   the objects
     * @param exception the error
     */
    @Override
    public void error(Collection<? extends IMObject> objects, Throwable exception) {
        // use the first object's display name when displaying the exception
        IMObject object = objects.toArray(new IMObject[objects.size()])[0];
        error(object, exception);
    }

    /**
     * Invoked when an error dialog is closed.
     * <p/>
     * Subclasses should override this if they need to block until any error dialog is closed.
     */
    protected void onErrorClosed() {

    }

    /**
     * Displays a save error.
     *
     * @param object    the object that failed to save
     * @param exception the cause
     */
    private void error(IMObject object, Throwable exception) {
        String displayName = DescriptorHelper.getDisplayName(object);
        String context = Messages.format("imobject.save.failed", object.getObjectReference());
        error(displayName, context, exception);
    }

    /**
     * Displays an error.
     *
     * @param displayName the display name of the object that failed to save
     * @param context     the context message. May be {@code null}
     * @param exception   the cause
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void error(String displayName, String context, Throwable exception) {
        Throwable cause = ExceptionHelper.getRootCause(exception);
        String title = Messages.format("imobject.save.failed", displayName);
        WindowPaneListener listener = new PopupDialogListener() {
            @Override
            protected void onAction(PopupDialog dialog) {
                onErrorClosed();
            }
        };
        if (cause instanceof ObjectNotFoundException) {
            // Don't propagate the exception
            String message = Messages.format("imobject.notfound", displayName);
            log.error(message, exception);
            ErrorHandler.getInstance().error(title, message, null, listener);
        } else {
            String message = ErrorFormatter.format(exception, displayName);
            String logerror = message;
            if (context != null) {
                logerror = Messages.format("logging.error.messageandcontext", message, context);
            }
            log.error(logerror, exception);
            ErrorHandler.getInstance().error(title, message, exception, listener);
        }
    }
}
