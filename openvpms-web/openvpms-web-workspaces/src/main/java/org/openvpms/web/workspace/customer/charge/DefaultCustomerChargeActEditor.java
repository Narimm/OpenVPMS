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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>,
 * <em>act.customerAccountChargesCredit</em>
 * or <em>act.customerAccountChargesCounter</em>.
 *
 * @author Tim Anderson
 */
public class DefaultCustomerChargeActEditor extends CustomerChargeActEditor {

    /**
     * Constructs a {@link DefaultCustomerChargeActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public DefaultCustomerChargeActEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Constructs a {@link DefaultCustomerChargeActEditor}.
     *
     * @param act            the act to edit
     * @param parent         the parent object. May be {@code null}
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     */
    public DefaultCustomerChargeActEditor(FinancialAct act, IMObject parent, LayoutContext context,
                                          boolean addDefaultItem) {
        super(act, parent, context, addDefaultItem);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        return new DefaultCustomerChargeActEditor(reload(getObject()), getParent(), getLayoutContext(),
                                                  getAddDefaultIem());
    }
}