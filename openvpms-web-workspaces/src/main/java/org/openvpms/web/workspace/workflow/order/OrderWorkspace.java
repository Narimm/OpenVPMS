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

package org.openvpms.web.workspace.workflow.order;

import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;

/**
 * Customer order workspace.
 *
 * @author Tim Anderson
 */
public class OrderWorkspace extends ResultSetCRUDWorkspace<Act> {

    /**
     * The archetypes that this workspace operates on.
     */
    private static final String[] SHORT_NAMES = {OrderArchetypes.ORDERS, OrderArchetypes.RETURNS};

    /**
     * Constructs a {@link OrderWorkspace}.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param context     the context
     * @param mailContext the mail context
     */
    public OrderWorkspace(Context context, MailContext mailContext) {
        super("workflow", "order", context);
        setArchetypes(Archetypes.create(SHORT_NAMES, Act.class));
        setMailContext(mailContext);
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        QueryBrowser<Act> browser = getBrowser();
        return new OrderCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(), getContext(),
                                   getHelpContext());
    }
}
