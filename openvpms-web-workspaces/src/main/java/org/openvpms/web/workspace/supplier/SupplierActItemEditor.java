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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier;

import org.openvpms.archetype.rules.act.ActStatusHelper;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;


/**
 * An editor for supplier act items, providing tax calculation support.
 *
 * @author Tim Anderson
 */
public abstract class SupplierActItemEditor extends ActItemEditor {

    /**
     * Constructs an {@link SupplierActItemEditor}.
     *
     * @param act     the act
     * @param parent  the parent act
     * @param context the layout context
     * @throws ArchetypeServiceException for any archetype service error
     */
    public SupplierActItemEditor(FinancialAct act, Act parent, LayoutContext context) {
        super(act, parent, context);

        if (!ActStatusHelper.isPosted(parent, ServiceHelper.getArchetypeService())) {
            calculateTax();
        }

        // add a listener to update the tax amount when the quantity or
        // unit price changes
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateTaxAmount();
            }
        };
        getProperty("quantity").addModifiableListener(listener);
        getProperty("unitPrice").addModifiableListener(listener);
    }

    /**
     * Calculates the tax amount.
     */
    protected void updateTaxAmount() {
        try {
            calculateTax();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Calculates the tax amount.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void calculateTax() {
        FinancialAct act = (FinancialAct) getObject();
        BigDecimal quantity = act.getQuantity();
        BigDecimal unitPrice = act.getUnitAmount();
        if (quantity != null && unitPrice != null) {
            Context context = getLayoutContext().getContext();
            Party practice = context.getPractice();
            Product product = (Product) getObject(getProductRef());
            if (product != null && practice != null) {
                BigDecimal amount = quantity.multiply(unitPrice);
                BigDecimal previousTax = act.getTaxAmount();
                TaxRules rules = new TaxRules(practice);
                BigDecimal tax = rules.calculateTax(amount, product, false);
                if (tax.compareTo(previousTax) != 0) {
                    Property property = getProperty("tax");
                    property.setValue(tax);
                }
            }
        }
    }

}
