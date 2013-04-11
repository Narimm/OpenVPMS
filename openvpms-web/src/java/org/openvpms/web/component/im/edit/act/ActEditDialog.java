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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import nextapp.echo2.app.Button;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * A edit dialog for acts that disables the Apply button for
 * <em>POSTED<em> acts, as a workaround for OVPMS-733.
 *
 * @author Tim Anderson
 */
public class ActEditDialog extends EditDialog {

    /**
     * Determines if the act has been POSTED. If so, it can no longer be saved.
     */
    private boolean posted;

    /**
     * Constructs a new {@code ActEditDialog}.
     *
     * @param editor the editor
     * @param help   the help context
     */
    public ActEditDialog(IMObjectEditor editor, HelpContext help) {
        super(editor, help);
        init(editor);
    }

    /**
     * Constructs a new {@code ActEditDialog}.
     *
     * @param editor the editor
     * @param save   if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param help   the help context
     */
    public ActEditDialog(IMObjectEditor editor, boolean save, HelpContext help) {
        super(editor, save, help);
        init(editor);
    }

    /**
     * Constructs a new {@code EditDialog}.
     *
     * @param editor the editor
     * @param save   if {@code true}, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param skip   if {@code true} display a 'Skip' button that simply
     *               closes the dialog
     * @param help   the help context
     */
    public ActEditDialog(IMObjectEditor editor, boolean save, boolean skip, HelpContext help) {
        super(editor, save, skip, help);
        init(editor);

        posted = getPosted();
    }

    /**
     * Determines if the act has been saved with POSTED status.
     *
     * @return {@code true} if the act has been saved
     */
    protected boolean isPosted() {
        return posted;
    }

    /**
     * Saves the current object.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    protected boolean doSave() {
        boolean result = super.doSave();
        if (!posted && result) {
            posted = getPosted();
        }
        return result;
    }

    /**
     * Initialises this.
     *
     * @param editor the editor
     */
    private void init(IMObjectEditor editor) {
        final Property status = editor.getProperty("status");
        if (status != null) {
            onStatusChanged(status);
            status.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onStatusChanged(status);
                }
            });
        }
    }

    /**
     * Disables the apply button if the act status is <em>POSTED</em>, otherwise enables it.
     *
     * @param status the act status property
     */
    private void onStatusChanged(Property status) {
        String value = (String) status.getValue();
        Button apply = getButtons().getButton(APPLY_ID);
        if (apply != null) {
            if (ActStatus.POSTED.equals(value)) {
                apply.setEnabled(false);
            } else if (!isSaveDisabled()) {
                apply.setEnabled(true);
            }
        }
    }

    /**
     * Determines if the act is posted.
     *
     * @return {@code true} if the act is posted
     */
    private boolean getPosted() {
        Act act = (Act) getEditor().getObject();
        String status = act.getStatus();
        return ActStatus.POSTED.equals(status);
    }

}
