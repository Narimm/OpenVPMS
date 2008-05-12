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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.act;

import nextapp.echo2.app.Button;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * A edit dialog for acts that disables the Apply button for
 * <em>POSTED<em> acts, as a workaround for OVPMS-733.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActEditDialog extends EditDialog {

    /**
     * Constructs a new <tt>ActEditDialog</tt>.
     *
     * @param editor the editor
     */
    public ActEditDialog(IMObjectEditor editor) {
        super(editor);
        init(editor);
    }

    /**
     * Constructs a new <tt>ActEditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     */
    public ActEditDialog(IMObjectEditor editor, boolean save) {
        super(editor, save);
        init(editor);
    }

    /**
     * Constructs a new <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param skip   if <tt>true</tt> display a 'Skip' button that simply
     *               closes the dialog
     */
    public ActEditDialog(IMObjectEditor editor, boolean save,
                         boolean skip) {
        super(editor, save, skip);
        init(editor);
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
     * Disables the apply button if the act status is <em>POSTED</em>,
     * otherwise enables it.
     *
     * @param status the act status property
     */
    private void onStatusChanged(Property status) {
        String value = (String) status.getValue();
        Button apply = getButtons().getButton(APPLY_ID);
        if (apply != null) {
            if (ActStatus.POSTED.equals(value)) {
                apply.setEnabled(false);
            } else {
                apply.setEnabled(true);
            }
        }
    }

}
