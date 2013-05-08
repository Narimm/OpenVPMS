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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.i18n.Messages;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Abstract implementation of the {@link IMObjectDeletionListener} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractIMObjectDeletionListener<T extends IMObject> implements IMObjectDeletionListener<T> {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractIMObjectDeletionListener.class);


    /**
     * Notifies that an object has been deleted.
     * <p/>
     * This implementation does nothing.
     *
     * @param object the deleted object
     */
    public void deleted(T object) {
    }

    /**
     * Notifies that an object has been deactivated.
     * <p/>
     * This implementation does nothing.
     *
     * @param object the deactivated object
     */
    public void deactivated(T object) {
    }

    /**
     * Notifies that an object has failed to be deleted.
     * <p/>
     * This implementation displays an error dialog.
     *
     * @param object the object that failed to be deleted
     * @param cause  the reason for the failure
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored ")
    public void failed(T object, Throwable cause) {
        String displayName = DescriptorHelper.getDisplayName(object);
        Throwable rootCause = ErrorHelper.getRootCause(cause);
        String title = Messages.get("imobject.delete.failed.title");
        if (rootCause instanceof ObjectNotFoundException) {
            // delete failed as the object (or a related object) has already been deleted
            String message = Messages.get("imobject.notfound", displayName);
            log.error(message, cause);
            ErrorHelper.show(title, message);
        } else {
            String context = Messages.get("imobject.delete.failed", object.getObjectReference());
            ErrorHelper.show(title, context, cause);
        }
    }

    /**
     * Notifies that an object has failed to be deleted.
     * <p/>
     * This implementation displays an error dialog.
     *
     * @param object the object that failed to be deleted
     * @param cause  the reason for the failure
     * @param editor the editor that performed the deletion
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void failed(T object, Throwable cause, IMObjectEditor editor) {
        String title = Messages.get("imobject.delete.failed.title");
        Throwable rootCause = ErrorHelper.getRootCause(cause);
        if (rootCause instanceof ObjectNotFoundException) {
            // delete failed as the object (or a related object) has already been deleted
            String message = Messages.get("imobject.notfound", editor.getDisplayName());
            log.error(message, cause);
            ErrorHelper.show(title, message);
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String user = (authentication != null) ? authentication.getName() : null;
            String context = Messages.get("logging.error.editcontext", object.getObjectReference(),
                                          editor.getClass().getName(), user);
            ErrorHelper.show(title, editor.getDisplayName(), context, cause);
        }
    }
}
