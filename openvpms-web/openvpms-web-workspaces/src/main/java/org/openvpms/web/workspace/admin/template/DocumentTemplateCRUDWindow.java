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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.template;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.EditResultSetDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.workspace.DocumentActActions;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;

/**
 * A CRUD window for document templates.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateCRUDWindow extends ResultSetCRUDWindow<Entity> {

    /**
     * The document act actions.
     */
    private final DocumentActActions documentActions = new DocumentActActions();

    /**
     * External edit button identifier.
     */
    static final String EXTERNAL_EDIT_ID = "button.externaledit";

    /**
     * Constructs a {@link DocumentTemplateCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public DocumentTemplateCRUDWindow(Archetypes<Entity> archetypes, Query<Entity> query, ResultSet<Entity> set,
                                      Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Creates a new result set dialog for editing.
     *
     * @param object the first object to edit
     * @param title  the dialog title
     * @return a new dialog
     */
    @Override
    protected EditResultSetDialog<Entity> createEditResultSetDialog(Entity object, String title) {
        return new DocumentTemplateEditDialog(title, object, getResultSet(), getActions(), getContext(),
                                              getHelpContext());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(ButtonFactory.create(EXTERNAL_EDIT_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onExternalEdit();
            }
        }));
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(EXTERNAL_EDIT_ID, enable && documentActions.canExternalEdit(getObject()));
    }

    /**
     * Invoked when the External Edit button is pressed.
     * <p/>
     * This launches OpenOffice if the selected document may be edited in OpenOffice.
     */
    private void onExternalEdit() {
        documentActions.externalEdit(getObject());
    }
}
