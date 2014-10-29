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

package org.openvpms.web.workspace.customer.order;

import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.EditBrowserDialog;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Browser dialog for orders requiring charging.
 *
 * @author Tim Anderson
 */
public class PendingOrderDialog extends EditBrowserDialog<Act> {

    /**
     * The dialog buttons.
     */
    private static String[] BUTTONS = {OK_ID, EDIT_ID, CANCEL_ID};


    /**
     * Constructs a {@link PendingOrderDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public PendingOrderDialog(String title, PendingOrderBrowser browser, Context context, HelpContext help) {
        super(title, BUTTONS, browser, new Actions(), context, help);
    }

    private static class Actions extends ActActions<Act> {

        private static final String[] SHORT_NAMES = {OrderArchetypes.PHARMACY_ORDER, OrderArchetypes.PHARMACY_RETURN};

        /**
         * Determines if an act can be edited.
         *
         * @param act the act to check
         * @return {@code true} if the act is a pharmacy order or return and the status isn't {@code POSTED}
         */
        @Override
        public boolean canEdit(Act act) {
            return TypeHelper.isA(act, SHORT_NAMES) && super.canEdit(act);
        }

        /**
         * Determines if an act can be deleted.
         *
         * @param act the act to check
         * @return {@code false}
         */
        @Override
        public boolean canDelete(Act act) {
            return false;
        }
    }
}
