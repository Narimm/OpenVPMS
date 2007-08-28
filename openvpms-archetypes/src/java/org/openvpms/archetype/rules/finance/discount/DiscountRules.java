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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectRefConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Percentage discount type.
     */
    private static final String PERCENTAGE = "PERCENTAGE";


    /**
     * Constructs a new <tt>DiscountRules</tt>.
     */
    public DiscountRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>DiscountRules</tt>.
     *
     * @param service the archetype service
     */
    public DiscountRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Calculates the discount amount for a customer, patient and product.
     * The discount rates are determined by:
     * <ol>
     * <li>finding all the discountType entities for the product and
     * product's productType</li>
     * <li>finding all the discountType entities for the customer and
     * patient</li>
     * <li>removing any discountType entities that are not active for the
     * specified date; and </li>
     * <li>removing any discountType entities from the combined
     * product/productType entities that are not also in the combined
     * customer/patient entities</li>
     * </ol>
     * The rates associated with the remaining discountTypes are used to
     * calculate the discount amount. The discount amount is the sum of:
     * <tt>(fixedPrice * discountRate/100) + qty * (unitPrice * discountRate/100)</tt>
     * for each rate.
     *
     * @param date       the date, used to determine if a discount applies
     * @param customer   the customer
     * @param patient    the patient. May be <tt>null</tt>
     * @param product    the product
     * @param fixedPrice the fixed amount
     * @param unitPrice  the unit price
     * @param quantity   the quantity
     * @return the discount amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateDiscountAmount(Date date, Party customer,
                                              Party patient,
                                              Product product,
                                              BigDecimal fixedPrice,
                                              BigDecimal unitPrice,
                                              BigDecimal quantity) {
        BigDecimal discount;
        if (fixedPrice.compareTo(BigDecimal.ZERO) == 0
                && (unitPrice.compareTo(BigDecimal.ZERO) == 0
                || quantity.compareTo(BigDecimal.ZERO) == 0)) {
            discount = BigDecimal.ZERO;
        } else {
            List<Entity> discounts = getDiscounts(date, customer, patient,
                                                  product);
            if (discounts.isEmpty()) {
                discount = BigDecimal.ZERO;
            } else {
                discount = calculateDiscountAmount(fixedPrice, unitPrice,
                                                   quantity, discounts);
            }
        }
        return MathRules.round(discount);
    }

    /**
     * Calculates the discount amount for an act, given a list of discount
     * classifications.
     * The discount amount is the sum of:
     * <tt>(fixedPrice * discountRate/100) + qty * (unitPrice * discountRate/100)</tt>
     * for each rate.
     *
     * @param fixedPrice the fixed price
     * @param unitPrice  the unit price
     * @param quantity   the quantity
     * @param discounts  a set of <em>entity.discountType</em>s
     * @return the discount amount for the act
     */
    private BigDecimal calculateDiscountAmount(
            BigDecimal fixedPrice, BigDecimal unitPrice, BigDecimal quantity,
            List<Entity> discounts) {
        BigDecimal result = BigDecimal.ZERO;

        for (Entity discount : discounts) {
            IMObjectBean discountBean = new IMObjectBean(discount, service);
            String discountType = discountBean.getString("type");
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
            if (PERCENTAGE.equals(discountType)) {
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
     * @param amount       the amount
     * @param rate         the rate
     * @param discountType the discount type
     * @return amount * discountRate/100
     */
    private BigDecimal calcDiscount(BigDecimal amount, BigDecimal rate,
                                    String discountType) {
        if (PERCENTAGE.equals(discountType)) {
            final BigDecimal hundred = new BigDecimal(100);
            return amount.multiply(rate).divide(hundred, 3,
                                                RoundingMode.HALF_UP);
        } else {
            return rate;
        }
    }

    /**
     * Returns the <em>entity.discountType</em> entities for a customer, patient
     * and product, active for the specified date.
     *
     * @param date     the date, used to determine if a discount applies
     * @param customer the customer
     * @param patient  the patient. May be <tt>null</tt>
     * @param product  the product
     * @return the discount entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Entity> getDiscounts(Date date, Party customer, Party patient,
                                      Product product) {
        List<Entity> result = Collections.emptyList();
        Set<IMObjectReference> productSet = getProductDiscounts(product, date);
        List<IMObjectReference> customerSet = getPartyDiscounts(customer, date);
        List<IMObjectReference> patientSet = getPartyDiscounts(patient, date);
        Set<IMObjectReference> partySet
                = new HashSet<IMObjectReference>(customerSet);
        partySet.addAll(patientSet);

        Set<IMObjectReference> refs
                = new HashSet<IMObjectReference>(productSet);
        refs.retainAll(partySet);
        if (!refs.isEmpty()) {
            result = new ArrayList<Entity>();
            for (IMObjectReference ref : refs) {
                Entity discount
                        = (Entity) ArchetypeQueryHelper.getByObjectReference(
                        service, ref);
                if (discount != null && discount.isActive()) {
                    result.add(discount);
                }
            }
        }
        return result;
    }

    /**
     * Returns a set of references to <em>entity.disccountType</em> entities
     * for a party.
     *
     * @param party the party. May be <tt>null</tt>
     * @return a set of <em>entity.discountType</em> for the party
     */
    private List<IMObjectReference> getPartyDiscounts(Party party, Date date) {
        List<IMObjectReference> result = Collections.emptyList();
        if (party != null) {
            EntityBean bean = new EntityBean(party, service);
            if (bean.hasNode("discounts")) {
                result = bean.getNodeTargetEntityRefs("discounts", date);
            }
        }
        return result;
    }

    /**
     * Returns a set of references to <em>entity.discountType</em> entities
     * for a product.
     *
     * @param product the product
     * @param date    the date
     * @return a set of <em>entity.discountType</em> for the product
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObjectReference> getProductDiscounts(Product product,
                                                       Date date) {
        EntityBean bean = new EntityBean(product, service);
        Set<IMObjectReference> discounts = new HashSet<IMObjectReference>();
        discounts.addAll(bean.getNodeTargetEntityRefs("discounts", date));
        if (bean.hasNode("type")) {
            List<EntityRelationship> types = bean.getValues(
                    "type", EntityRelationship.class);
            for (EntityRelationship relationship : types) {
                IMObjectReference srcRef = relationship.getSource();
                if (srcRef != null) {
                    discounts.addAll(getProductTypeDiscounts(srcRef, date));
                }
            }
        }
        return discounts;
    }

    /**
     * Returns discounts associated with an <em>entity.productType</em>.
     *
     * @param ref the product type reference
     * @return the discounts associated with the reference
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<IMObjectReference> getProductTypeDiscounts(
            IMObjectReference ref, Date date) {
        ArchetypeQuery query = new ArchetypeQuery(
                new ObjectRefConstraint(ref));
        Iterator<Entity> iter = new IMObjectQueryIterator<Entity>(
                query, Arrays.asList("discounts"));
        if (iter.hasNext()) {
            EntityBean bean = new EntityBean(iter.next(), service);
            return bean.getNodeTargetEntityRefs("discounts", date);
        }
        return Collections.emptyList();
    }

}
