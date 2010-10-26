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
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.exception.ESCIException;

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
     * Constructs an <tt>AbstractUBLFinancialType</tt>.
     *
     * @param currency the expected currency for all amounts
     * @param service  the archetype service
     */
    public UBLFinancialType(String currency, IArchetypeService service) {
        super(service);
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
     * @throws ESCIException if the tax is incorrectly specified
     */
    protected BigDecimal getTax(List<TaxTotalType> tax) {
        return getTax(tax, getType(), getID());
    }

    /**
     * Returns the tax amount.
     * <p/>
     * This implementation expects that the supplied <tt>tax</tt> contains 0..1 elements.
     *
     * @param tax    the tax totals
     * @param parent the name of the parent element for error reporting
     * @param id     the parent element identifier
     * @return the tax amount
     * @throws ESCIException if the tax is incorrectly specified
     */
    protected BigDecimal getTax(List<TaxTotalType> tax, String parent, String id) {
        BigDecimal result = BigDecimal.ZERO;
        if (tax != null && !tax.isEmpty()) {
            if (tax.size() != 1) {
                Message message = ESCIAdapterMessages.ublInvalidCardinality("TaxTotal", parent, id, "1", tax.size());
                throw new ESCIException(message.toString());
            }
            TaxTotalType total = tax.get(0);
            result = getAmount(total.getTaxAmount(), "TaxTotal/TaxAmount", parent, id);
        }
        return result;
    }

    /**
     * Gets the value from an amount, verifying the currency.
     *
     * @param amount the amount
     * @param path   the path to the element for error reporting
     * @return the amount value
     * @throws ESCIException if the amount isn't present, is invalid, or has a currency the doesn't match that expected
     */
    protected BigDecimal getAmount(AmountType amount, String path) {
        return getAmount(amount, path, getType(), getID());
    }

    /**
     * Gets the value from an amount, verifying the currency.
     *
     * @param amount the amount
     * @param path   the path to the element for error reporting
     * @param parent the parent element
     * @param id     the parent element identfier
     * @return the amount value
     * @throws ESCIException if the amount isn't present, is invalid, or has a currency the doesn't match that expected
     */
    protected BigDecimal getAmount(AmountType amount, String path, String parent, String id) {
        checkRequired(amount, path, parent, id);
        checkRequired(amount.getValue(), path, parent, id);
        CurrencyCodeContentType code = getRequired(amount.getCurrencyID(), path + "@currencyID", parent, id);
        if (!ObjectUtils.equals(currency, code.value())) {
            Message message = ESCIAdapterMessages.invalidCurrency(path, parent, id, currency, code.value());
            throw new ESCIException(message.toString());
        }
        BigDecimal result = amount.getValue();
        if (result.signum() == -1) {
            Message message = ESCIAdapterMessages.invalidAmount(path, parent, id, result);
            throw new ESCIException(message.toString());
        }
        return amount.getValue();
    }

    /**
     * Returns the value for a quantity, verifying thhat it is greater than zero.
     *
     * @param quantity the quantity
     * @param path     the path to the element for error reporting
     * @return the quantity value
     * @throws ESCIException if the quantity doesn't exist or is &lt;= zero
     */
    protected BigDecimal getQuantity(QuantityType quantity, String path) {
        return getQuantity(quantity, path, getType(), getID());
    }

    /**
     * Returns the value for a quantity, verifying thhat it is greater than zero.
     *
     * @param quantity the quantity
     * @param path     the path to the element for error reporting
     * @param parent   the parent element
     * @param id       the parent element identfier
     * @return the quantity value
     * @throws ESCIException if the quantity doesn't exist or is &lt;= zero
     */
    protected BigDecimal getQuantity(QuantityType quantity, String path, String parent, String id) {
        checkRequired(quantity, path, parent, id);
        checkRequired(quantity.getValue(), path, parent, id);
        BigDecimal result = quantity.getValue();
        if (result.compareTo(BigDecimal.ZERO) <= 0) {
            Message message = ESCIAdapterMessages.invalidQuantity(path, parent, id, result);
            throw new ESCIException(message.toString());
        }
        return result;
    }

}