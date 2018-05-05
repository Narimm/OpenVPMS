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

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Abstract implementation of {@link RemoveConfirmationHandler}.
 * <p>
 * This only prompts for confirmation if the object is saved.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRemoveConfirmationHandler implements RemoveConfirmationHandler {

    /**
     * Confirms removal of an object from a collection.
     * <p>
     * If approved, it performs the removal.
     *
     * @param object     the object to remove
     * @param collection the collection to remove the object from, if approved
     */
    @Override
    public void remove(final IMObject object, final IMObjectCollectionEditor collection) {
        if (object.isNew()) {
            apply(object, collection);
        } else {
            confirmRemove(object, collection);
        }
    }

    /**
     * Removes the object from the collection.
     *
     * @param object     the object to remove
     * @param collection the collection
     */
    protected void apply(IMObject object, IMObjectCollectionEditor collection) {
        collection.remove(object);
    }

    /**
     * Displays a confirmation dialog to confirm removal of an object from a collection.
     * <p>
     * If approved, it performs the removal.
     *
     * @param object     the object to remove
     * @param collection the collection to remove the object from, if approved
     */
    protected void confirmRemove(final IMObject object, final IMObjectCollectionEditor collection) {
        String displayName = getDisplayName(object, collection);
        String title = Messages.format("imobject.collection.delete.title", displayName);
        String message = Messages.format("imobject.collection.delete.message", displayName);
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onYes() {
                apply(object, collection);
            }
        });
        dialog.show();
    }

    /**
     * Returns the display name for an object, used for display.
     *
     * @param object     the object
     * @param collection the collection the object is in
     * @return the display name
     */
    protected String getDisplayName(IMObject object, IMObjectCollectionEditor collection) {
        return DescriptorHelper.getDisplayName(object);
    }
}
