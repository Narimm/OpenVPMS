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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;

/**
 * Edit dialog for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditDialog extends EditDialog {

    /**
     * Generate attachments button identifier.
     */
    private static final String GENERATE_ID = "button.generateattachments";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {APPLY_ID, OK_ID, CANCEL_ID, GENERATE_ID};

    /**
     * Constructs a {@link ClaimEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public ClaimEditDialog(ClaimEditor editor, Context context) {
        super(editor, BUTTONS, true, context);
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public ClaimEditor getEditor() {
        return (ClaimEditor) super.getEditor();
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (GENERATE_ID.equals(button)) {
            onGenerate();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Generates attachments.
     */
    protected void onGenerate() {
        if (save()) {
            ClaimEditor editor = getEditor();
            if (!editor.generateAttachments()) {
                editor.checkAttachments();
            }
        }
    }
}
