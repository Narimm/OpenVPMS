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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.map.invoice;

import org.oasis.ubl.common.aggregate.TaxCategoryType;
import org.oasis.ubl.common.aggregate.TaxSubtotalType;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.map.UBLFinancialType;
import org.openvpms.esci.adapter.map.UBLType;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import java.math.BigDecimal;


/**
 * Wrapper for the <tt>TaxSubtotalType</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLTaxSubtotal extends UBLFinancialType {

    /**
     * The tax subtotal.
     */
    private final TaxSubtotalType subtotal;


    /**
     * Constructs an <tt>UBLTaxSubtotal</tt>.
     *
     * @param subtotal the subtotal
     * @param parent   the parent element
     * @param currency the expected currency on all amounts
     * @param service  the archetype service
     */
    public UBLTaxSubtotal(TaxSubtotalType subtotal, UBLType parent, String currency, IArchetypeService service) {
        super(parent, currency, service);
        this.subtotal = subtotal;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return "TaxSubtotal";
    }

    /**
     * Returns the path to the element, relative to the parent element used for error reporting.
     *
     * @return the path
     */
    @Override
    public String getPath() {
        return "TaxTotal/" + getType();
    }

    /**
     * Returns the type identifier.
     *
     * @return null
     */
    public String getID() {
        return null;
    }

    /**
     * Returns the tax amount.
     *
     * @return the tax amount
     * @throws ESCIAdapterException if the tax amount is incorrectly specified
     */
    public BigDecimal getTaxAmount() {
        return getAmount(subtotal.getTaxAmount(), "TaxAmount");
    }

    /**
     * Returns the tax category.
     *
     * @return the tax category
     * @throws ESCIAdapterException if the tax category is not present
     */
    public UBLTaxCategory getTaxCategory() {
        TaxCategoryType category = getRequired(subtotal.getTaxCategory(), "TaxCategory");
        return new UBLTaxCategory(category, this, getCurrency(), getArchetypeService());
    }

}
