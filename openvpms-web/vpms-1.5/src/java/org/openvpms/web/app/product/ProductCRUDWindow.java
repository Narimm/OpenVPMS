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
 *
 *  $Id$
 */

package org.openvpms.web.app.product;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for products.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-11-15 17:59:45 +1100 (Thu, 15 Nov 2007) $
 */
public class ProductCRUDWindow extends AbstractViewCRUDWindow<Product> {

    /**
     * The copy button.
     */
    private Button copy;

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";


    /**
     * Creates a new <tt>ProductCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that thi may create
     */
    public ProductCRUDWindow(Archetypes<Product> archetypes) {
        super(archetypes);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        // If the logged in user is an admin, show the copy button
        if (UserHelper.isAdmin(GlobalContext.getInstance().getUser())) {
            if (copy == null) {
                copy = ButtonFactory.create(COPY_ID, new ActionListener() {
                    public void onAction(ActionEvent event) {
                        onCopy();
                    }
                });
            }
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
    	buttons.removeAll();
    	if (enable) {
    		buttons.add(getCreateButton());
    		if (UserHelper.isAdmin(GlobalContext.getInstance().getUser())) {
    			buttons.add(getEditButton());
    			buttons.add(getDeleteButton());
    		}
            if (copy != null) {
            	buttons.add(copy);
            }
    	}
    	else {
    		buttons.add(getCreateButton());    		
    	}
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        final Product product = getObject();
        if (product != null) {
            String name = getArchetypeDescriptor().getDisplayName();
            String title = Messages.get("product.information.copy.title", name);
            String message = Messages.get("product.information.copy.message",
                                          name);
            final ConfirmationDialog dialog
                    = new ConfirmationDialog(title, message);
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
     * Copy the product.
     *
     * @param product the product to copy
     */
    private void copy(Product product) {
        Product copy = null;
        try {
            ProductRules rules = new ProductRules();
            String name = Messages.get("product.copy.name", product.getName());
            copy = rules.copy(product, name);
        } catch (OpenVPMSException exception) {
            String title = Messages.get(
                    "product.information.copy.failed",
                    getArchetypeDescriptor().getDisplayName());
            ErrorHelper.show(title, exception);
        }
        onRefresh(copy);
    }

}
