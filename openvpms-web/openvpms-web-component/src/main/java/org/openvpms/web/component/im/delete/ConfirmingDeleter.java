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
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * An {@link IMObjectDeleter} that prompts for confirmation to delete objects.
 *
 * @author Tim Anderson
 */
public class ConfirmingDeleter<T extends IMObject> extends AbstractIMObjectDeleter<T> {

    /**
     * Constructs a {@link ConfirmingDeleter}.
     *
     * @param factory the deletion handler factory
     */
    public ConfirmingDeleter(IMObjectDeletionHandlerFactory factory) {
        super(factory);
    }

    /**
     * Invoked to remove an object.
     * <p>
     * Pops up a dialog prompting if deletion of an object should proceed, deleting it if OK is selected.
     *
     * @param handler  the deletion handler
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    @Override
    protected void delete(final IMObjectDeletionHandler<T> handler, final Context context,
                          final HelpContext help, final IMObjectDeletionListener<T> listener) {
        T object = handler.getObject();
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.format("imobject.delete.title", type);
        String name = (object.getName() != null) ? object.getName() : type;
        String message = Messages.format("imobject.delete.message", name);
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, true, help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doDelete(handler, context, help, listener);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when an object cannot be deleted, and must therefore be deactivated.
     * <p>
     * This implementation prompts the user to deactivate the object, or cancel.
     *
     * @param handler  the deletion handler
     * @param listener the listener
     * @param help     the help context
     */
    @Override
    protected void deactivate(final IMObjectDeletionHandler<T> handler, final IMObjectDeletionListener<T> listener,
                              HelpContext help) {
        T object = handler.getObject();
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.format("imobject.deactivate.title", type);
        String name = (object.getName() != null) ? object.getName() : type;
        String message = Messages.format("imobject.deactivate.message", name);
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, true, help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doDeactivate(handler, listener);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when an object cannot be de deleted, and has already been deactivated.
     *
     * @param object the object
     * @param help   the help context
     */
    protected void deactivated(T object, HelpContext help) {
        String message = Messages.format("imobject.delete.deactivated", DescriptorHelper.getDisplayName(object),
                                         object.getName());
        ErrorDialog.show(message, help);
    }

    /**
     * Invoked when deletion and deactivation of an object is not supported.
     *
     * @param object the deletion handler
     * @param help   the help context
     */
    @Override
    protected void unsupported(T object, HelpContext help) {
        String message = Messages.format("imobject.delete.unsupported", DescriptorHelper.getDisplayName(object));
        ErrorDialog.show(message, help);
    }
}
