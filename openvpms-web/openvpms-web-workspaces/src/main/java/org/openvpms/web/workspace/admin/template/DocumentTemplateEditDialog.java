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

package org.openvpms.web.workspace.admin.template;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditResultSetDialog;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.workspace.DocumentActActions;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;

/**
 * An {@link EditResultSetDialog} for <em>entity.documentTemplate</em> instances.
 * <p/>
 * This supports launching OpenOffice for templates that may be edited by OpenOffice.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateEditDialog extends EditResultSetDialog<Entity> {

    /**
     * The actions that may be performed on document acts.
     */
    private static final DocumentActActions actions = new DocumentActActions();

    /**
     * Constructs a {@link DocumentTemplateEditDialog}.
     *
     * @param title   the window title
     * @param first   the first object to edit
     * @param set     the set of results to edit
     * @param actions determines if an object may be edited
     * @param context the context
     * @param help    the help context
     */
    public DocumentTemplateEditDialog(String title, Entity first, ResultSet<Entity> set,
                                      IMObjectActions<Entity> actions, Context context, HelpContext help) {
        super(title, first, set, actions, context, help);
        addButton(DocumentTemplateCRUDWindow.EXTERNAL_EDIT_ID, new ActionListener() {
            public void onAction(ActionEvent e) {
                onExternalEdit();
            }
        });
        enableButtons();
    }

    /**
     * Enables/disables the buttons.
     */
    @Override
    protected void enableButtons() {
        super.enableButtons();
        boolean enabled = actions.canExternalEdit(getObject());
        getButtons().setEnabled(DocumentTemplateCRUDWindow.EXTERNAL_EDIT_ID, enabled);
    }

    /**
     * Returns the object being edited.
     *
     * @return the object being edited, or {@code null} if no object is being edited
     */
    private Entity getObject() {
        IMObjectEditor editor = getEditor();
        return (editor != null) ? (Entity) editor.getObject() : null;
    }

    /**
     * Invoked when the External Edit button is pressed.
     * <p/>
     * This launches OpenOffice if the selected document may be edited in OpenOffice.
     */
    private void onExternalEdit() {
        Entity object = getObject();
        if (object != null) {
            actions.externalEdit(object);
        }
    }
}

