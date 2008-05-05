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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.tax;

import static org.openvpms.archetype.rules.finance.tax.TaxRuleException.ErrorCode.InvalidActForTax;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Customer Tax Rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CustomerTaxRules extends TaxRules {

    /**
     * Charge item act types.
     */
    private static final String[] CHARGE_ITEM_TYPES =
            {"act.customerAccountInvoiceItem", "act.customerAccountCreditItem",
             "act.customerAccountCounterItem"};

    /**
     * Adjustment act types.
     */
    private static final String[] ADJUSTMENT_TYPES =
            {"act.customerAccountBadDebt", "act.customerAccountCreditAdjust",
             "act.customerAccountDebitAdjust"};


    /**
     * Constructs a new <tt>CustomerTaxRules</tt>.
     *
     * @param practice the practice, for default tax classifications.
     *                 May be <tt>null</tt>
     */
    public CustomerTaxRules(Party practice) {
        this(practice, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs  a new <tt>CustomerTaxRules</tt>.
     *
     * @param practice the practice, for default tax classifications.
     * @param service  the archetype service
     */
    public CustomerTaxRules(Party practice, IArchetypeService service) {
        super(practice, service);
    }

    /**
     * Calculate the amount of tax for the passed financial act using tax
     * type information for the products, product type, organisation and
     * customer associated with the act and any related child acts.
     * The tax amount will be calculated and stored in the tax node of the act.
     *
     * @param act the financial act to calculate tax for
     * @return the amount of tax for the act
     * @throws TaxRuleException          if the act is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateTax(FinancialAct act) {
        BigDecimal tax = BigDecimal.ZERO;
        ActBean bean = new ActBean(act, getService());
        Party customer = (Party) bean.getParticipant("participation.customer");
        if (customer == null) {
            act.setTaxAmount(Money.ZERO);
        } else {
            List<Lookup> taxRates = getTaxRates(act, customer);
            tax = calculateTaxAmount(act, taxRates);
        }
        return tax;
    }

    /**
     * Calculate the amount of tax for the passed financial act using tax
     * type information for the products, product type, organisation and
     * customer associated with the act.
     * The tax amount will be calculated and stored in the tax node for the
     * act.
     *
     * @param act      the act
     * @param customer the customer
     * @return the amount of tax for the act
     * @throws TaxRuleException          if the act is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateTax(FinancialAct act, Party customer) {
        List<Lookup> taxRates = getTaxRates(act, customer);
        return calculateTaxAmount(act, taxRates);
    }

    /**
     * Calculates the tax for an act, given a list of tax rate classifications.
     * <p/>
     * The tax amount will be calculated and stored in the tax node of the act.
     *
     * @param act      the act
     * @param taxRates the tax rate classifications
     * @return the tax on the act
     * @throws ArchetypeServiceException for any archetype service error
     */
    private BigDecimal calculateTaxAmount(FinancialAct act,
                                          List<Lookup> taxRates) {
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = act.getTotal();
        if (total != null) {
            tax = calculateTax(total, taxRates);
        }
        act.setTaxAmount(new Money(tax));
        return tax;
    }

    /**
     * Returns the tax rate classifications for an act, excluding any
     * classifiations associated with the customer.
     *
     * @param act      the act
     * @param customer the customer
     * @return a list of tax rate classifications for the act
     * @throws TaxRuleException          if the act is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Lookup> getTaxRates(FinancialAct act, Party customer) {
        ActBean bean = new ActBean(act);
        Collection<Lookup> taxRates;
        if (bean.isA(CHARGE_ITEM_TYPES)) {
            Product product
                    = (Product) bean.getParticipant("participation.product");
            if (product == null) {
                taxRates = Collections.emptyList();
            } else {
                taxRates = getProductTaxRates(product);
            }
        } else if (bean.isA(ADJUSTMENT_TYPES)) {
            taxRates = getPracticeTaxRates();
        } else {
            throw new TaxRuleException(InvalidActForTax,
                                       act.getArchetypeId().getShortName());
        }
        List<Lookup> result = new ArrayList<Lookup>(taxRates);
        if (!result.isEmpty()) {
            List<Lookup> exclusions = getCustomerTaxRates(customer);
            result.removeAll(exclusions);
        }
        return result;
    }

    /**
     * Returns the tax rate classifications for a customer.
     *
     * @param customer the customer
     * @return a list fo tax rate classifications for the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Lookup> getCustomerTaxRates(Party customer) {
        IMObjectBean bean = new IMObjectBean(customer, getService());
        if (bean.hasNode("taxes")) {
            return bean.getValues("taxes", Lookup.class);
        }
        return Collections.emptyList();
    }

}
