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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.AbstractWorkspace;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.workspace.product.batch.BatchWorkspace;
import org.openvpms.web.workspace.product.stock.StockWorkspace;


/**
 * Product workspaces.
 *
 * @author Tim Anderson
 */
public class ProductWorkspaces extends AbstractWorkspaces {

    /**
     * Constructs a {@code ProductWorkspaces}.
     *
     * @param context the context
     */
    public ProductWorkspaces(Context context) {
        super("product");
        PracticeMailContext mailContext = new PracticeMailContext(context);

        addWorkspace(new InformationWorkspace(context), mailContext);
        addWorkspace(new StockWorkspace(context), mailContext);
        addWorkspace(new BatchWorkspace(context), mailContext);
    }

    private void addWorkspace(AbstractWorkspace workspace, MailContext context) {
        workspace.setMailContext(context);
        addWorkspace(workspace);
    }

}
