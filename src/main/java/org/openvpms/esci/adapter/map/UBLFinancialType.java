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
package org.openvpms.esci.adapter.map;

import org.apache.commons.lang.ObjectUtils;
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.QuantityType;
import org.oasis.ubl.common.aggregate.TaxSubtotalType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.invoice.UBLTaxSubtotal;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import java.math.BigDecimal;
import java.util.List;


/**
 * Wrapper for UBL financial types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class UBLFinancialType extends UBLType {

    /**
     * The expected currency for all amounts.
     */
    private final String currency;


    /**
     * Constructs an <tt>UBLFinancialType</tt>.
     *
     * @param parent   the parent element. May be <tt>null</tt>
     * @param currency the expected currency for all amounts
     * @param service  the archetype service
     */
    public UBLFinancialType(UBLType parent, String currency, IArchetypeService service) {
        super(parent, service);
        this.currency = currency;
    }

    /**
     * Returns the currency.
     * <p/>
     * All amounts must be expressed in this currency
     *
     * @return the currency
     */
    protected String getCurrency() {
        return currency;
    }

    /**
     * Returns the tax amount.
     * <p/>
     * This implementation expects that the supplied <tt>tax</tt> contains 0..1 elements.
     *
     * @param tax the tax totals
     * @return the tax amount
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    protected BigDecimal getTaxAmount(List<TaxTotalType> tax) {
        BigDecimal result = BigDecimal.ZERO;
        TaxTotalType total = getTaxTotal(tax);
        if (total != null) {
            result = getAmount(total.getTaxAmount(), "TaxTotal/TaxAmount");
        }
        return result;
    }

    /**
     * Returns the tax total type.
     *
     * @param tax a list of tax totals. Must contain 0..1 elements
     * @return the tax total type, or <tt>null</tt> if none is present
     * @throws ESCIAdapterException if too many elements are supplied
     */
    protected TaxTotalType getTaxTotal(List<TaxTotalType> tax) {
        TaxTotalType result = null;
        if (tax != null && !tax.isEmpty()) {
            if (tax.size() != 1) {
                ErrorContext context = new ErrorContext(this, "TaxTotal");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                        context.getPath(), context.getType(), context.getID(), "1", tax.size()));
            }
            result = tax.get(0);
        }
        return result;
    }

    /**
     * Returns the tax subtotal type.
     *
     * @param tax a list of tax totals. Must contain 0..1 elements
     * @return the tax sub total, or <tt>null</tt> if no tax is specified
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    protected UBLTaxSubtotal getTaxSubtotal(List<TaxTotalType> tax) {
        UBLTaxSubtotal result = null;
        TaxTotalType total = getTaxTotal(tax);
        if (total != null) {
            result = getTaxSubtotal(total);
        }
        return result;
    }

    /**
     * Returns the tax subtotal.
     *
     * @param tax the tax total. May be <tt>null</tt>
     * @return the tax subtotal,  or <tt>null</tt> if no tax total is supplied
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    protected UBLTaxSubtotal getTaxSubtotal(TaxTotalType tax) {
        UBLTaxSubtotal result = null;
        if (tax != null) {
            List<TaxSubtotalType> taxSubtotal = tax.getTaxSubtotal();
            if (taxSubtotal.size() != 1) {
                ErrorContext context = new ErrorContext(this, "TaxTotal/TaxSubtotal");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                        context.getPath(), context.getType(), context.getID(), "1", taxSubtotal.size()));
            }
            result = new UBLTaxSubtotal(taxSubtotal.get(0), this, getCurrency(), getArchetypeService());
        }
        return result;
    }

    /**
     * Gets the value from an amount, verifying the currency.
     *
     * @param amount the amount
     * @param path   the path to the element for error reporting
     * @return the amount value
     * @throws ESCIAdapterException if the amount isn't present, is invalid, or has a currency the doesn't match that
     *                              expected
     */
    protected BigDecimal getAmount(AmountType amount, String path) {
        checkRequired(amount, path);
        checkRequired(amount.getValue(), path);
        CurrencyCodeContentType code = getRequired(amount.getCurrencyID(), path + "@currencyID");
        if (!ObjectUtils.equals(currency, code.value())) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidCurrency(
                    context.getPath(), context.getType(), context.getID(), currency, code.value()));
        }
        BigDecimal result = amount.getValue();
        if (result.signum() == -1) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidAmount(context.getPath(), context.getType(),
                                                                             context.getID(), result));
        }
        return amount.getValue();
    }

    /**
     * Returns the value for a quantity, verifying thhat it is greater than zero.
     *
     * @param quantity the quantity
     * @param path     the path to the element for error reporting
     * @return the quantity value
     * @throws ESCIAdapterException if the quantity doesn't exist or is &lt;= zero
     */
    protected BigDecimal getQuantity(QuantityType quantity, String path) {
        checkRequired(quantity, path);
        checkRequired(quantity.getValue(), path);
        BigDecimal result = quantity.getValue();
        if (result.compareTo(BigDecimal.ZERO) <= 0) {
            ErrorContext context = new ErrorContext(this, path);
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidQuantity(context.getPath(), context.getType(),
                                                                               context.getID(), result));
        }
        return result;
    }

}