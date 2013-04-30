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
 *  $Id:SupplierInvoiceItemEditor.java 2287 2007-08-13 07:56:33Z tanderson $
 */

package org.openvpms.web.app.supplier.charge;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.supplier.SupplierActItemEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.supplierAccountInvoiceItem</em>, or
 * <em>act.supplierAccountCreditItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class SupplierChargeActItemEditor extends SupplierActItemEditor {

    /**
     * Construct a new <tt>SupplierChargeActItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public SupplierChargeActItemEditor(FinancialAct act, Act parent,
                                       LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.supplierAccountInvoiceItem",
                            "act.supplierAccountCreditItem")) {
            throw new IllegalArgumentException("Invalid act type:"
                                               + act.getArchetypeId().getShortName());
        }
    }

    /**
     * Invoked when the product is changed to update prices.
     *
     * @param product the product. May be <tt>null</tt>
     */
    @Override
    protected void productModified(Product product) {
        Property unitCost = getProperty("unitPrice");
        ProductPrice unit = getProductPrice(ProductArchetypes.UNIT_PRICE, product);
        if (unit != null) {
            unitCost.setValue(unit.getPrice());
        }
        notifyProductListener(product);
    }
}
