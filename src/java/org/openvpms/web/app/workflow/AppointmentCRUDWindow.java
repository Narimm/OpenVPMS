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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.AbstractCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.resource.util.Messages;


/**
 * Appointment CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentCRUDWindow extends AbstractCRUDWindow {

    /**
     * Constructs a new <code>AbstractCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public AppointmentCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor  the editor
     * @param context the layout context
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor,
                                          LayoutContext context) {
        return new AppointmentEditDialog(editor, context);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        super.enableButtons(enable);
        Row buttons = getButtons();
        Button print = getPrintButton();
        if (enable) {
            if (buttons.indexOf(print) == -1) {
                buttons.add(print);
            }
        } else {
            buttons.remove(print);
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Act act = (Act) getObject();
        if (!"Completed".equals(act.getStatus())) {
            super.onDelete();
        } else {
            String name = getArchetypeDescriptor().getDisplayName();
            String status = act.getStatus();
            String title = Messages.get("act.nodelete.title", name);
            String message = Messages.get("act.nodelete.message", name, status);
            ErrorDialog.show(title, message);
        }

        super.onDelete();
    }
}
