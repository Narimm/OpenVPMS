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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import nextapp.echo2.app.Button;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
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
     * Monitors act status changes.
     */
    private ModifiableListener statusListener;

    /**
     * Constructs an {@link ActEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public ActEditDialog(IMObjectEditor editor, Context context) {
        this(editor, true, context);
    }

    /**
     * Constructs an {@link ActEditDialog}.
     *
     * @param editor  the editor
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     */
    public ActEditDialog(IMObjectEditor editor, boolean save, Context context) {
        this(editor, save, false, context);
    }

    /**
     * Constructs an {@link ActEditDialog}.
     *
     * @param editor  the editor
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param skip    if {@code true} display a 'Skip' button that simply closes the dialog
     * @param context the context
     */
    public ActEditDialog(IMObjectEditor editor, boolean save, boolean skip, Context context) {
        super(editor, getButtons(true, true, skip), save, context);
    }

    /**
     * Determines if the current object can be saved.
     *
     * @return {@code true} if the current object can be saved
     */
    @Override
    protected boolean canSave() {
        return super.canSave() && (getEditor().getObject().isNew() || !posted);
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
     * @param editor the editor
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave(IMObjectEditor editor) {
        super.doSave(editor);
        if (!posted) {
            posted = getPosted();
        }
    }

    /**
     * Sets the editor.
     * <p>
     * If there is an existing editor, its selection path will be set on the editor.
     *
     * @param editor the editor. May be {@code null}
     */
    @Override
    protected void setEditor(IMObjectEditor editor) {
        if (editor != null) {
            if (statusListener != null) {
                // remove the old listener
                IMObjectEditor old = getEditor();
                if (old != null) {
                    Property status = editor.getProperty("status");
                    if (status != null) {
                        status.removeModifiableListener(statusListener);
                    }
                }
                statusListener = null;
            }
            final Property status = editor.getProperty("status");
            if (status != null) {
                onStatusChanged(status);
                statusListener = new ModifiableListener() {
                    public void modified(Modifiable modifiable) {
                        onStatusChanged(status);
                    }
                };
                status.addModifiableListener(statusListener);
            }
        }
        super.setEditor(editor);
        posted = getPosted();
    }

    /**
     * Invoked to reload the object being edited when save fails.
     * <p/>
     * This implementation reloads the editor, but returns {@code false} if the act has been POSTED.
     *
     * @param editor the editor
     * @return a {@code true} if the editor was reloaded and the act is not now POSTED.
     */
    @Override
    protected boolean reload(IMObjectEditor editor) {
        return super.reload(editor) && !getPosted();
    }

    /**
     * Disables the apply button if the act status is <em>POSTED</em>, otherwise enables it.
     *
     * @param status the act status property
     */
    private void onStatusChanged(Property status) {
        Button apply = getButtons().getButton(APPLY_ID);
        if (apply != null) {
            if (ActStatus.POSTED.equals(status.getString())) {
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
        IMObjectEditor editor = getEditor();
        if (editor != null) {
            Act act = (Act) editor.getObject();
            return ActStatus.POSTED.equals(act.getStatus());
        }
        return false;
    }

}
