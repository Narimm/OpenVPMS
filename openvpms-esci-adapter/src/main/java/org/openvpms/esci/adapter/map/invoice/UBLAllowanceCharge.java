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

import org.apache.commons.lang.StringUtils;
import org.oasis.ubl.common.aggregate.AllowanceChargeType;
import org.oasis.ubl.common.aggregate.TaxCategoryType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.basic.AllowanceChargeReasonType;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.UBLFinancialType;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import java.math.BigDecimal;
import java.util.List;


/**
 * Wrapper for the UBL <tt>AllowanceChargeType</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLAllowanceCharge extends UBLFinancialType {

    /**
     * The allowance/charge.
     */
    private final AllowanceChargeType allowanceCharge;

    /**
     * The parent invoice.
     */
    private final UBLInvoice invoice;


    /**
     * Constructs an <tt>UBLAllowanceCharge</tt>.
     *
     * @param allowanceCharge the allowance/charge
     * @param invoice         the parent invoice
     * @param currency        the expected currency for all amounts
     * @param service         the archetype service
     */
    public UBLAllowanceCharge(AllowanceChargeType allowanceCharge, UBLInvoice invoice, String currency,
                              IArchetypeService service) {
        super(invoice, currency, service);
        this.allowanceCharge = allowanceCharge;
        this.invoice = invoice;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return "AllowanceCharge";
    }

    /**
     * Returns the type identifier.
     *
     * @return the type identifier, or <tt>null</tt> if it is not set
     */
    public String getID() {
        return getId(allowanceCharge.getID());
    }

    /**
     * Determines if this is a charge.
     * <p/>
     * This corresponds to <em>AllowanceCharge/ChargeIndicator</em>.
     *
     * @return <tt>true</tt> if it is a charge, <tt>false</tt> if it is an allowance
     */
    public boolean isCharge() {
        return allowanceCharge.getChargeIndicator().isValue();
    }

    /**
     * Returns the amount.
     * <p/>
     * This corresponds to <em>AllowanceCharge/Amount</em>.
     *
     * @return the amount
     * @throws ESCIAdapterException if the amount is incorrectly specified
     */
    public BigDecimal getAmount() {
        return getAmount(allowanceCharge.getAmount(), "Amount");
    }

    /**
     * Returns the total tax in the allowance/charge.
     * <p/>
     * This corresponds to <em>AllowanceCharge/TaxTotal/TaxAmount</em>
     *
     * @return the total tax, or <tt>BigDecimal.ZERO</tt> if it wasn't specified
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    public BigDecimal getTaxAmount() {
        BigDecimal result = BigDecimal.ZERO;
        TaxTotalType taxTotal = allowanceCharge.getTaxTotal();
        if (taxTotal != null) {
            result = getAmount(taxTotal.getTaxAmount(), "TaxTotal/TaxAmount");
        }
        return result;
    }

    /**
     * Returns the allow/charge reason.
     *
     * @return the reason
     * @throws ESCIAdapterException if the reason is not specified or is not present
     */
    public String getAllowanceChargeReason() {
        AllowanceChargeReasonType reason = getRequired(allowanceCharge.getAllowanceChargeReason(),
                                                       "AllowanceCharge/AllowanceChargeReason");
        String result = StringUtils.trimToNull(reason.getValue());
        checkRequired(result, "AllowanceCharge/AllowanceChargeReason");
        return result;
    }

    /**
     * Returns the tax category.
     *
     * @return the tax category, or <tt>null</tt> if none is provided
     * @throws ESCIAdapterException if the tax category is incorrectly specified
     */
    public UBLTaxCategory getTaxCategory() {
        UBLTaxCategory result = null;
        List<TaxCategoryType> categories = allowanceCharge.getTaxCategory();
        if (!categories.isEmpty()) {
            if (categories.size() != 1) {
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                        "AllowanceCharge/TaxCategory", "Invoice", invoice.getID(), "1", categories.size()));
            }
            result = new UBLTaxCategory(categories.get(0), this, getCurrency(), getArchetypeService());
        }
        return result;
    }
}
