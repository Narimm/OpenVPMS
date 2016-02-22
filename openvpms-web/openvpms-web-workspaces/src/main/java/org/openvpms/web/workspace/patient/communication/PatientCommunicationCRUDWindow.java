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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.communication;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.communication.AbstractCommunicationEditor;
import org.openvpms.web.workspace.customer.communication.CommunicationCRUDWindow;

/**
 * Communication CRUD window for patients.
 *
 * @author Tim Anderson
 */
public class PatientCommunicationCRUDWindow extends CommunicationCRUDWindow {

    /**
     * Constructs an {@link PatientCommunicationCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public PatientCommunicationCRUDWindow(Context context, HelpContext help) {
        super(context, help);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(Act object, LayoutContext context) {
        IMObjectEditor editor = super.createEditor(object, context);
        if (editor instanceof AbstractCommunicationEditor) {
            ((AbstractCommunicationEditor) editor).setShowPatient(false);
        }
        return editor;
    }
}
