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
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.discount;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Discount rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DiscountRules {

    /**
     * Calculates the discount amount for a customer, patient and product.
     * The discount rates are determined by:
     * <ol>
     * <li>finding all the discountType classifications for product and
     * product's productType; and<li>
     * <li>finding all the discountType classifications for the customer and
     * patient; and<li>
     * <li>removing any discountType classifications from the combined
     * product/productType classifications that are not also in the combined
     * customer/patient classifications</li>
     * </ol>
     * The rates associated with the remaining discountTypes are used to calculate
     * the discount amount. The discount amount is the sum of:
     * <code>(fixedPrice * discountRate/100) + qty * (unitPrice * discountRate/100)</code>
     * for each rate.
     *
     * @param customer   the customer
     * @param patient    the patient
     * @param product    the product
     * @param fixedPrice the fixed amount
     * @param unitPrice  the unit price
     * @param quantity   the quantity
     * @param service    the archetype service
     * @return the discount amount
     */
    public static BigDecimal calculateDiscountAmount(
            Party customer, Party patient, Product product,
            BigDecimal fixedPrice, BigDecimal unitPrice, BigDecimal quantity,
            IArchetypeService service) {
        BigDecimal discount;
        if (fixedPrice.compareTo(BigDecimal.ZERO) == 0
                && (unitPrice.compareTo(BigDecimal.ZERO) == 0
                || quantity.compareTo(BigDecimal.ZERO) == 0)) {
            discount = BigDecimal.ZERO;
        } else {
            Set<IMObject> discounts = getDiscounts(customer, patient, product,
                                                   service);
            if (discounts.isEmpty()) {
                discount = BigDecimal.ZERO;
            } else {
                discount = calculateDiscountAmount(fixedPrice, unitPrice,
                                                   quantity, discounts,
                                                   service);
            }
        }
        return MathRules.round(discount);
    }

    /**
     * Calculates the discount amount for an act, given a list of discount
     * classifications.
     * The discount amount is the sum of:
     * <code>(fixedPrice * discountRate/100) + qty * (unitPrice * discountRate/100)</code>
     * for each rate.
     *
     * @param fixedPrice the fixed price
     * @param unitPrice  the unit price
     * @param quantity   the quantity
     * @param discounts  the discount classifications
     * @param service    the archetype service
     * @return the discount amount for the act
     */
    private static BigDecimal calculateDiscountAmount(
            BigDecimal fixedPrice, BigDecimal unitPrice, BigDecimal quantity,
            Set<IMObject> discounts,
            IArchetypeService service) {
        BigDecimal result = BigDecimal.ZERO;

        for (IMObject discount : discounts) {
            IMObjectBean discountBean = new IMObjectBean(discount, service);
            String discountType = discountBean.getString("type", "");
            BigDecimal rate = discountBean.getBigDecimal("rate",
                                                         BigDecimal.ZERO);
            Boolean discountFixed = discountBean.getBoolean("discountFixed");
            BigDecimal dFixedPrice;
            if (discountFixed) {
                dFixedPrice = calcDiscount(fixedPrice, rate, discountType);
            } else {
                dFixedPrice = BigDecimal.ZERO;
            }
            BigDecimal dUnitPrice = calcDiscount(unitPrice, rate, discountType);
            BigDecimal amount;
            if (discountType.equalsIgnoreCase("Percentage")) {
                amount = quantity.multiply(dUnitPrice).add(dFixedPrice);
            } else {
                amount = dUnitPrice.add(dFixedPrice);
            }
            result = result.add(amount);
        }
        return result;
    }

    /**
     * Helper to calculates amount * discountRate/100, to 3 decimal places.
     *
     * @param amount the amount
     * @param rate   the rate
     * @return amount * discountRate/100
     */
    private static BigDecimal calcDiscount(BigDecimal amount, BigDecimal rate,
                                           String discountType) {
        if (discountType.equalsIgnoreCase("Percentage")) {
            final BigDecimal hundred = new BigDecimal(100);
            return amount.multiply(rate).divide(hundred, 3,
                                                RoundingMode.HALF_UP);
        } else {
            return rate;
        }
    }

    /**
     * Returns the discount classifications for a customer, patient and product.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param product  the product
     * @param service  the archetype service
     * @return the discount classifications
     */
    private static Set<IMObject> getDiscounts(Party customer,
                                              Party patient,
                                              Product product,
                                              IArchetypeService service) {
        Set<IMObject> discounts;
        Set<IMObject> productSet = getProductDiscounts(product, service);
        Set<IMObject> customerSet = getPartyDiscounts(customer, service);
        Set<IMObject> patientSet = getPartyDiscounts(patient, service);
        Set<IMObject> partySet = new HashSet<IMObject>(customerSet);
        partySet.addAll(patientSet);

        discounts = new HashSet<IMObject>(productSet);
        discounts.retainAll(partySet);
        return discounts;
    }

    /**
     * Returns a set of discounts for a party.
     *
     * @param party   the party
     * @param service the archetype service
     * @return a list fo tax rate classifications for the customer
     */
    private static Set<IMObject> getPartyDiscounts(Party party,
                                                   IArchetypeService service) {
        Set<IMObject> result = Collections.emptySet();
        if (party != null) {
            IMObjectBean bean = new IMObjectBean(party, service);
            if (bean.hasNode("discounts")) {
                List<IMObject> discounts = bean.getValues("discounts");
                result = new HashSet<IMObject>(discounts);
            }
        }
        return result;
    }

    /**
     * Returns a list of discounts for a product.
     *
     * @param product the product
     * @param service the archetype service
     * @return a list of taxes for the product
     */
    private static Set<IMObject> getProductDiscounts(
            Product product,
            IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(product, service);
        Set<IMObject> discounts = new HashSet<IMObject>();
        discounts.addAll(bean.getValues("discounts"));
        if (bean.hasNode("type")) {
            List<IMObject> types = bean.getValues("type");
            for (IMObject object : types) {
                EntityRelationship relationship = (EntityRelationship) object;
                IMObjectReference srcRef = relationship.getSource();
                if (srcRef != null) {
                    IMObject src = ArchetypeQueryHelper.getByObjectReference(
                            service, srcRef);
                    if (src != null) {
                        IMObjectBean productType
                                = new IMObjectBean(src, service);
                        discounts.addAll(productType.getValues("discounts"));
                    }
                }
            }
        }
        return discounts;
    }

}
