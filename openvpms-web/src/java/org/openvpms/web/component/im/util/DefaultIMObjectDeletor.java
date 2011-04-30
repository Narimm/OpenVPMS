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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.resource.util.Messages;


/**
 * An {@link IMObjectDeletor} that prompts for confirmation to delete objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DefaultIMObjectDeletor extends IMObjectDeletor {

    /**
     * Invoked to remove an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     */
    protected <T extends IMObject> void remove(T object, IMObjectDeletionListener<T> listener) {
        confirmDelete(object, listener, "imobject.delete.message");
    }

    /**
     * Invoked to remove anobject that has {@link EntityRelationship}s to other objects.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     */
    protected <T extends IMObject> void removeWithRelationships(T object, IMObjectDeletionListener<T> listener) {
        confirmDelete(object, listener, "imobject.delete.withrelationships.message");
    }

    /**
     * Invoked when an object has participations and so cannot be deleted. It must therefore be deactivated.
     * <p/>
     * This implementation prompts the user to deactivate the object, or cancel.
     *
     * @param object   the object
     * @param listener the listener
     */
    protected <T extends IMObject> void deactivate(final T object, final IMObjectDeletionListener<T> listener) {
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.deactivate.title", type);
        String message = Messages.get("imobject.deactivate.message", object.getName());
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, true);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doDeactivate(object, listener);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when an object cannot be de deleted, and has already been deactivated.
     *
     * @param object the object
     */
    protected <T extends IMObject> void deactivated(T object) {
        String message = Messages.get("imobject.delete.deactivated", DescriptorHelper.getDisplayName(object),
                                      object.getName());
        ErrorDialog.show(message);
    }

    /**
     * Pops up a dialog prompting if deletion of an object should proceed,
     * deleting it if OK is selected.
     *
     * @param object     the object to delete
     * @param listener   the listener to notify
     * @param messageKey the message resource bundle key
     */
    private <T extends IMObject> void confirmDelete(final T object, final IMObjectDeletionListener<T> listener,
                                                    String messageKey) {
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.delete.title", type);
        String name = (object.getName() != null) ? object.getName() : type;
        String message = Messages.get(messageKey, name);
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, true);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doRemove(object, listener);
            }
        });
        dialog.show();
    }

}
