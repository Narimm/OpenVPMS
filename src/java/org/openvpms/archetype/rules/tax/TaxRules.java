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

package org.openvpms.archetype.rules.tax;

import static org.openvpms.archetype.rules.tax.TaxRuleException.ErrorCode.InvalidActForTax;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.IPage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Tax Rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TaxRules {

    /**
     * Charge act types.
     */
    private static final String[] CHARGE_TYPES =
            {"act.customerAccountChargesCounter",
             "act.customerAccountChargesInvoice",
             "act.customerAccountChargesCredit"};

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
     * Calculate the amount of tax for the passed financial act using tax
     * type information for the products, product type, organisation and
     * customer associated with the act and any related child acts.
     * The tax amount will be calculated and stored in the tax node for the
     * act and related child acts.
     *
     * @param act     the financial act to calculate tax for
     * @param service the archetype service
     */
    public static void calculateTax(FinancialAct act,
                                    IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        Party customer = (Party) bean.getParticipant("participation.customer");
        if (customer == null) {
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        if (bean.isA(CHARGE_TYPES)) {
            List<Act> items = bean.getActs();
            for (Act item : items) {
                FinancialAct child = (FinancialAct) item;
                BigDecimal current = child.getTaxAmount();
                BigDecimal amount = calculateTax(child, customer, service);
                if (current == null || current.compareTo(amount) != 0) {
                    service.save(act);
                }
                total = total.add(amount);
            }
        } else if (bean.isA(ADJUSTMENT_TYPES)) {
            List<IMObject> taxRates = getTaxRates(act, customer, service);
            calculateTaxAmount(act, taxRates, service);
        } else {
            throw new TaxRuleException(InvalidActForTax,
                                       act.getArchetypeId().getShortName());
        }
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
     * @param service  the archetype service
     * @return the amount of tax for the act
     */
    public static BigDecimal calculateTax(FinancialAct act, Party customer,
                                          IArchetypeService service) {
        if (!TypeHelper.isA(act, CHARGE_ITEM_TYPES)) {
            throw new TaxRuleException(InvalidActForTax,
                                       act.getArchetypeId().getShortName());
        }
        List<IMObject> taxRates = getTaxRates(act, customer, service);
        return calculateTaxAmount(act, taxRates, service);
    }

    /**
     * Returns the tax rate classifications for an act, excluding any classifiations
     * associated with the customer.
     *
     * @param act      the act
     * @param customer the customer
     * @param service  the archetype service
     * @return a list of tax rate classifications for the act
     */
    private static List<IMObject> getTaxRates(FinancialAct act, Party customer,
                                              IArchetypeService service) {
        ActBean bean = new ActBean(act);
        Collection<IMObject> taxRates;
        if (bean.isA(CHARGE_ITEM_TYPES)) {
            Product product
                    = (Product) bean.getParticipant("participation.product");
            if (product == null) {
                taxRates = Collections.emptyList();
            } else {
                taxRates = getChargeTaxRates(product, service);
            }
        } else if (bean.isA(ADJUSTMENT_TYPES)) {
            taxRates = getAdjustmentTaxRates(service);
        } else {
            throw new TaxRuleException(InvalidActForTax,
                                       act.getArchetypeId().getShortName());
        }
        List<IMObject> result = new ArrayList<IMObject>(taxRates);
        if (!result.isEmpty()) {
            List<IMObject> exclusions = getCustomerTaxRates(customer, service);
            for (IMObject excluded : exclusions) {
                result.remove(excluded);
            }
        }
        return result;
    }

    /**
     * Returns the tax rate classifications for a charge act item (i.e, one of
     * <em>act.customerAccountInvoiceItem</em>,
     * <em>act.customerAccountCreditItem</em> or
     * <em>act.customerAccountCounterItem</em>).
     *
     * @param product the act product
     * @param service the archetype service
     */
    private static Collection<IMObject> getChargeTaxRates(Product product,
                                                          IArchetypeService service) {
        Collection<IMObject> taxRates = getProductTaxRates(product, service);
        if (taxRates.isEmpty()) {
            taxRates = getPracticeTaxRates(service);
        }
        return taxRates;
    }

    /**
     * Returns the tax rate classifications for a adjustment act (i.e,
     * one of <em>act.customerAccountBadDebt</em>,
     * <em>act.customerAccountCreditAdjust</em>, or
     * <em>act.customerAccountDebitAdjust</em>.
     */
    private static Collection<IMObject> getAdjustmentTaxRates(
            IArchetypeService service) {
        return getPracticeTaxRates(service);
    }

    /**
     * Calculates the tax amount for an act, given a list of tax rate classifications.
     *
     * @param act      the act
     * @param taxRates the tax rate classifications
     * @param service  the archetype service
     * @return the tax amount for the act
     */
    private static BigDecimal calculateTaxAmount(FinancialAct act,
                                                 List<IMObject> taxRates,
                                                 IArchetypeService service) {
        BigDecimal total = act.getTotal();
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (IMObject taxRate : taxRates) {
            IMObjectBean taxBean = new IMObjectBean(taxRate, service);
            BigDecimal rate = taxBean.getBigDecimal("rate", BigDecimal.ZERO);
            BigDecimal divisor = new BigDecimal(100).add(rate);
            BigDecimal tax = total.multiply(rate).divide(divisor, 3,
                                                         RoundingMode.HALF_UP);
            taxTotal = taxTotal.add(tax);
        }
        act.setTaxAmount(new Money(taxTotal));
        return taxTotal;
    }

    /**
     * Returns the tax rate classifications for a customer.
     *
     * @param customer the customer
     * @param service  the archetype service
     * @return a list fo tax rate classifications for the customer
     */
    private static List<IMObject> getCustomerTaxRates(Party customer,
                                                      IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(customer, service);
        return bean.getValues("taxes");
    }

    /**
     * Returns a list of taxes for a product. If the product has no taxType
     * classifications, it returns any taxType classifications for the
     * entity.productTypes associated with the product.
     *
     * @param product the product
     * @param service the archetype service
     * @return a list of taxes for the product
     */
    private static Collection<IMObject> getProductTaxRates(
            Product product,
            IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(product);
        Set<IMObject> taxes = new HashSet<IMObject>();
        taxes.addAll(bean.getValues("taxes"));
        if (taxes.isEmpty()) {
            List<IMObject> types = bean.getValues("type");
            for (IMObject object : types) {
                EntityRelationship relationship = (EntityRelationship) object;
                IMObjectReference srcRef = relationship.getSource();
                if (srcRef != null) {
                    IMObject src = ArchetypeQueryHelper.getByObjectReference(
                            service, srcRef);
                    if (src != null) {
                        IMObjectBean productType = new IMObjectBean(src,
                                                                    service);
                        taxes.addAll(productType.getValues("taxes"));
                    }
                }
            }
        }
        return taxes;
    }

    /**
     * Returns a list of taxes for the first practice found.
     *
     * @param service the archetype service
     * @return a list of taxes for the first practice found
     */
    private static List<IMObject> getPracticeTaxRates(
            IArchetypeService service) {
        List<IMObject> taxes = Collections.emptyList();
        IPage<IMObject> page = ArchetypeQueryHelper.get(
                service, new String[]{"party.organisationPractice"}, true, 0,
                1);
        List<IMObject> practices = page.getRows();
        if (!practices.isEmpty()) {
            IMObjectBean bean = new IMObjectBean(practices.get(0));
            taxes = bean.getValues("taxes");
        }
        return taxes;
    }


}
