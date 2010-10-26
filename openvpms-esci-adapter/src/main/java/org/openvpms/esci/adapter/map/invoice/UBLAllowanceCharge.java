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

import org.oasis.ubl.common.aggregate.AllowanceChargeType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.map.UBLFinancialType;

import java.math.BigDecimal;


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
        super(currency, service);
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
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the amount is incorrectly specified
     */
    public BigDecimal getAmount() {
        return getAmount(allowanceCharge.getAmount(), "AllowanceCharge/Amount", "Invoice", invoice.getID());
    }

    /**
     * Returns the total tax in the allowance/charge.
     *
     * @return the total tax, or <tt>BigDecimal.ZERO</tt> if it wasn't specified
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the tax is incorrectly specified
     */
    public BigDecimal getTaxTotal() {
        BigDecimal result = BigDecimal.ZERO;
        TaxTotalType taxTotal = allowanceCharge.getTaxTotal();
        if (taxTotal != null) {
            result = getAmount(taxTotal.getTaxAmount(), "AllowanceCharge/TaxTotal/TaxAmount", "Invoice",
                               invoice.getID());
        }
        return result;
    }
}
