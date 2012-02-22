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

package org.openvpms.web.app.customer.charge;

import org.openvpms.web.component.im.edit.EditDialog;


/**
 * Manages edit dialogs displayed during charging.
*
* @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
* @version $LastChangedDate: $
*/
public class ChargePopupEditorManager extends PopupEditorManager {

    /**
     * The current edit dialog.
     */
    private EditDialog current;

    /**
     * Returns the current popup dialog.
     *
     * @return the current popup dialog. May be <tt>null</tt>
     */
    public EditDialog getCurrent() {
        return current;
    }

    /**
     * Displays an edit dialog.
     *
     * @param dialog the dialog
     */
    @Override
    protected void edit(EditDialog dialog) {
        super.edit(dialog);
        current = dialog;
    }

    /**
     * Invoked when the edit is completed.
     */
    @Override
    protected void editCompleted() {
        super.editCompleted();
        current = null;
    }
}