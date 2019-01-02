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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.RemoveConfirmationHandler;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Implementation if {@link RemoveConfirmationHandler} for {@link ChargeItemRelationshipCollectionEditor}.
 * <p/>
 * If the charge item being removed has minimum quantities, this displays a confirmation. If not, it falls back
 * to the default remove confirmation.
 *
 * @author Tim Anderson
 */
public class DefaultChargeRemoveConfirmationHandler extends ChargeRemoveConfirmationHandler {

    /**
     * Constructs a {@link DefaultChargeRemoveConfirmationHandler}.
     *
     * @param context the context
     * @param help    the help context
     */
    public DefaultChargeRemoveConfirmationHandler(Context context, HelpContext help) {
        super(context, help);
    }
}
