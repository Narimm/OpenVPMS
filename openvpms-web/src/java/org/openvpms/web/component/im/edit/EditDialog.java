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
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.dialog.PopupWindow;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A popup window that displays an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EditDialog extends PopupWindow {

    /**
     * The editor.
     */
    private final IMObjectEditor _editor;

    /**
     * Apply button identifier.
     */
    private static final String APPLY_ID = "apply";

    /**
     * OK button identifier.
     */
    private static final String OK_ID = "ok";

    /**
     * Delete button identifier.
     */
    private static final String DELETE_ID = "delete";

    /**
     * Cancel button identifier.
     */
    private static final String CANCEL_ID = "cancel";

    /**
     * Edit window style name.
     */
    private static final String STYLE = "EditDialog";


    /**
     * Construct a new <code>EditDialog</code>.
     *
     * @param editor  the editor
     * @param context the layout context
     */
    public EditDialog(IMObjectEditor editor, LayoutContext context) {
        super(editor.getTitle(), STYLE, context.getFocusTree());
        _editor = editor;
        setModal(true);

        getLayout().add(_editor.getComponent());
        _editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onComponentChange(event);
                    }
                });

        addButton(APPLY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onApply();
            }
        });
        addButton(OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onOK();
            }
        });
        addButton(DELETE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        addButton(CANCEL_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCancel();
            }
        });
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    protected IMObjectEditor getEditor() {
        return _editor;
    }

    /**
     * Save the current object.
     */
    protected void onApply() {
        _editor.save();
    }

    /**
     * Save the current object, and close the editor.
     */
    protected void onOK() {
        if (_editor.save()) {
            close();
        }
    }

    /**
     * Delete the current object, and close the editor.
     */
    protected void onDelete() {
        if (_editor.delete()) {
            close();
        }
    }

    /**
     * Close the editor, discarding any unsaved changes.
     */
    protected void onCancel() {
        _editor.cancel();
        close();
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

}
