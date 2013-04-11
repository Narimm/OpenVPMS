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

package org.openvpms.web.app.product.stock;

import org.openvpms.archetype.rules.stock.StockUpdater;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.retry.Retryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.system.ServiceHelper;


/**
 * Stock CRUD window.
 *
 * @author Tim Anderson
 */
public class StockCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Constructs a {@code StockCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param help       the help context
     */
    public StockCRUDWindow(Archetypes<Act> archetypes, HelpContext help) {
        super(archetypes, DefaultActActions.getInstance(), help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPostButton());
        buttons.add(createPrintButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean enableEdit = false;
        boolean enableDeletePost = false;
        if (enable) {
            Act object = getObject();
            ActActions<Act> actions = getActions();
            enableEdit = actions.canEdit(object);
            enableDeletePost = actions.canDelete(object);
        }
        buttons.setEnabled(EDIT_ID, enableEdit);
        buttons.setEnabled(DELETE_ID, enableDeletePost);
        buttons.setEnabled(POST_ID, enableDeletePost);
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em>
     * acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new ActEditDialog(editor, getHelpContext());
    }

    /**
     * Updates stock when an <em>act.stockAdjust</em> or <em>act.stockTransfer</em> is posted.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(final Act act) {
        Retryer.run(new Retryable() {
            public boolean run() {
                StockUpdater updater = new StockUpdater(ServiceHelper.getArchetypeService());
                updater.update(act);
                return true;
            }
        });
    }
}
