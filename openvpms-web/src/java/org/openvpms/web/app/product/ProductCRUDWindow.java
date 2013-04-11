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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.product;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ResultSetCRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for products.
 *
 * @author Tim Anderson
 */
public class ProductCRUDWindow extends ResultSetCRUDWindow<Product> {

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";


    /**
     * Constructs a {@code ProductCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
   ` * @param help       the help context
     */
    public ProductCRUDWindow(Archetypes<Product> archetypes, Query<Product> query, ResultSet<Product> set,
                             HelpContext help) {
        super(archetypes, query, set, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createNewButton());
        buttons.add(createViewButton());
        // If the logged in user is an admin, show the copy, edit and delete buttons
        boolean admin = UserHelper.isAdmin(GlobalContext.getInstance().getUser());
        if (admin) {
            buttons.add(createEditButton());
            buttons.add(createDeleteButton());
            Button copy = ButtonFactory.create(COPY_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onCopy();
                }
            });
            buttons.add(copy);
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(VIEW_ID, getResultSet() != null && enable);
        buttons.setEnabled(EDIT_ID, enable);
        buttons.setEnabled(DELETE_ID, enable);
        buttons.setEnabled(COPY_ID, enable);
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        final Product product = getObject();
        if (product != null) {
            String displayName = getArchetypeDescriptor().getDisplayName();
            String name = product.getName();
            if (StringUtils.isEmpty(name)) {
                name = displayName;
            }
            String title = Messages.get("product.information.copy.title", displayName);
            String message = Messages.get("product.information.copy.message", name);
            final ConfirmationDialog dialog = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    copy(product);
                }
            });
            dialog.show();
        }
    }

    /**
     * Copy the product, and edit it.
     *
     * @param product the product to copy
     */
    private void copy(Product product) {
        try {
            ProductRules rules = new ProductRules();
            String name = Messages.get("product.copy.name", product.getName());
            Product copy = rules.copy(product, name);

            // NOTE: can't use the parent edit(IMObject) method as it relies on the object being edited
            // being in the current result set.
            LayoutContext context = createLayoutContext();
            IMObjectEditor editor = createEditor(copy, context);
            edit(editor);
        } catch (OpenVPMSException exception) {
            String title = Messages.get("product.information.copy.failed", getArchetypeDescriptor().getDisplayName());
            ErrorHelper.show(title, exception);
        }
    }

}
