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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectDeletorListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A popup window that displays an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EditDialog extends PopupDialog {

    /**
     * The editor.
     */
    private final IMObjectEditor editor;

    /**
     * Determines if the dialog should save when apply and OK are pressed.
     */
    private final boolean save;

    /**
     * Edit window style name.
     */
    private static final String STYLE = "EditDialog";


    /**
     * Constructs a new <code>EditDialog</code>.
     *
     * @param editor the editor
     */
    public EditDialog(IMObjectEditor editor) {
        this(editor, true);
    }

    /**
     * Construct a new <code>EditDialog</code>.
     *
     * @param editor the editor
     * @param save   if <code>true</code>, display an 'apply' and 'OK' button
     *               that save the editor when pressed. If <code>false</code>
     *               display an 'OK' and 'CANCEL' button that simply close the
     *               dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save) {
        super(editor.getTitle(), STYLE, getButtons(save));
        this.editor = editor;
        this.save = save;
        setModal(true);

        getLayout().add(editor.getComponent());
        this.editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onComponentChange(event);
                    }
                });
        getFocusGroup().add(0, editor.getFocusGroup());
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    protected IMObjectEditor getEditor() {
        return editor;
    }

    /**
     * Saves the current object, if saving is enabled.
     */
    @Override
    protected void onApply() {
        if (save) {
            editor.save();
        }
    }

    /**
     * Saves the current object, if saving is enabled, and closes the editor.
     */
    @Override
    protected void onOK() {
        if (save) {
            if (editor.save()) {
                close(OK_ID);
            }
        } else {
            close(OK_ID);
        }
    }

    /**
     * Delete the current object, and close the editor.
     */
    @Override
    protected void onDelete() {
        IMObjectDeletorListener<IMObject> listener
                = new IMObjectDeletorListener<IMObject>() {
            public void deleted(IMObject object) {
                close(DELETE_ID);
            }

            public void deactivated(IMObject object) {
                close(OK_ID);
            }
        };
        IMObjectDeletor.delete(editor, listener);
    }

    /**
     * Close the editor, discarding any unsaved changes.
     */
    protected void onCancel() {
        editor.cancel();
        close(CANCEL_ID);
    }

    /**
     * Invoked when the component changes.
     *
     * @param event the component change event
     */
    protected void onComponentChange(PropertyChangeEvent event) {
        getLayout().remove((Component) event.getOldValue());
        getLayout().add((Component) event.getNewValue());
    }

    /**
     * Determines which buttons should be displayed.
     *
     * @param save if <code>true</code> provide apply, OK, delete and cancel
     *             buttons, otherwise provide OK and cancel buttons
     * @return the button identifiers
     */
    private static String[] getButtons(boolean save) {
        return (save) ? APPLY_OK_DELETE_CANCEL : OK_CANCEL;
    }

}
