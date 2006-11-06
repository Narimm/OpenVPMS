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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>party.customer*</em> parties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerEditor extends AbstractIMObjectEditor {

    /**
     * Construct a new <code>CustomerEditor</code>.
     *
     * @param customer the object to edit
     * @param parent   the parent object. May be <code>null</code>
     * @param context  the layout context. May be <code>null</code>.
     */
    public CustomerEditor(Party customer, IMObject parent,
                          LayoutContext context) {
        super(customer, parent, context);

        context.getContext().setCustomer(customer);
    }
}
