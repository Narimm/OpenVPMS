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
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.OrConstraint;
import static org.openvpms.component.system.common.query.RelationalOp.*;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * 100% discount.
     */
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);


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
     * <p/>
     * <tt>(fixedPrice * discountRate/100) + qty * (unitPrice * discountRate/100)</tt>
     * </p>
     * <br/>
     * for each rate. If the discount amount exceeds the maximum discount
     * calculated by:
     * <p/>
     * <tt>(fixedPrice * maxFixedPriceDiscount/100) + qty * (unitPrice * maxUnitPriceDiscount/100)</tt>
     * <p/>
     * then the maximum discount amount will be returned.
     *
     * @param date                  the date, used to determine if a discount
     *                              applies
     * @param customer              the customer
     * @param patient               the patient. May be <tt>null</tt>
     * @param product               the product
     * @param fixedPrice            the fixed amount
     * @param unitPrice             the unit price
     * @param quantity              the quantity
     * @param maxFixedPriceDiscount the maximum fixed price discount percentage
     * @param maxUnitPriceDiscount  the maximum unit price discount percentage
     * @return the discount amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateDiscount(Date date, Party customer,
                                        Party patient, Product product,
                                        BigDecimal fixedPrice,
                                        BigDecimal unitPrice,
                                        BigDecimal quantity,
                                        BigDecimal maxFixedPriceDiscount,
                                        BigDecimal maxUnitPriceDiscount) {
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
                BigDecimal maxDiscount = calculateMaxDiscount(
                        fixedPrice, unitPrice, quantity, maxFixedPriceDiscount,
                        maxUnitPriceDiscount);
                if (discount.compareTo(maxDiscount) > 0) {
                    discount = maxDiscount;
                }
            }
        }
        return MathRules.round(discount);
    }

    /**
     * Returns the discounts for a customer, patient and product.
     * <p/>
     * This is the union of all <em>entity.discountType</em> entities associated
     * with the customer and patient that are also associated with the product.
     * <p/>
     * If a customer, patient or product has <em>entity.discountGroupType</em>
     * entities, the associated <em>entity.discountType</em> entitieswill be
     * included.
     *
     * @param date     the date, used to determine if a discount applies
     * @param customer the customer
     * @param patient  the patient. May be <tt>null</tt>
     * @param product  the product
     * @return the discount entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getDiscounts(Date date, Party customer, Party patient,
                                     Product product) {
        List<Entity> result = Collections.emptyList();
        DiscountGroups groups = new DiscountGroups(date);
        Set<IMObjectReference> productSet = getProductDiscounts(product, date,
                                                                groups);
        Set<IMObjectReference> customerSet = getPartyDiscounts(customer, date,
                                                               groups);
        Set<IMObjectReference> patientSet = getPartyDiscounts(patient, date,
                                                              groups);
        Set<IMObjectReference> partySet
                = new HashSet<IMObjectReference>(customerSet);
        partySet.addAll(patientSet);

        Set<IMObjectReference> refs
                = new HashSet<IMObjectReference>(productSet);
        refs.retainAll(partySet);
        if (!refs.isEmpty()) {
            result = new ArrayList<Entity>();
            for (IMObjectReference ref : refs) {
                Entity discount = (Entity) service.get(ref);
                if (discount != null && discount.isActive()) {
                    result.add(discount);
                }
            }
        }
        return result;
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
     * @param fixedPrice
     * @param unitPrice
     * @param quantity
     * @param fixedDiscountRate
     * @param unitDiscountRate
     * @return the maximum discount
     */
    private BigDecimal calculateMaxDiscount(BigDecimal fixedPrice,
                                            BigDecimal unitPrice,
                                            BigDecimal quantity,
                                            BigDecimal fixedDiscountRate,
                                            BigDecimal unitDiscountRate) {
        BigDecimal dFixedPrice = calcDiscount(fixedPrice, fixedDiscountRate);
        BigDecimal dUnitPrice = calcDiscount(unitPrice, unitDiscountRate);
        return quantity.multiply(dUnitPrice).add(dFixedPrice);
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
        BigDecimal result;
        if (PERCENTAGE.equals(discountType)) {
            return calcDiscount(amount, rate);
        } else {
            result = rate;
        }
        return result;
    }

    /**
     * Helper to calculates amount * rate/100, to 3 decimal places.
     *
     * @param amount the amount
     * @param rate   the rate
     * @return amount * rate/100
     */
    private BigDecimal calcDiscount(BigDecimal amount, BigDecimal rate) {
        BigDecimal result = amount.multiply(rate);
        result = MathRules.divide(result, HUNDRED, 3);
        return result;
    }

    /**
     * Returns a set of references to <em>entity.disccountType</em> references
     * for a party.
     *
     * @param party          the party. May be <tt>null</tt>
     * @param discountGroups the discount group cache
     * @return a set of <em>entity.discountType</em> references for the party
     */
    private Set<IMObjectReference> getPartyDiscounts(
            Party party, Date date, DiscountGroups discountGroups) {
        List<IMObjectReference> result = Collections.emptyList();
        if (party != null) {
            EntityBean bean = new EntityBean(party, service);
            if (bean.hasNode("discounts")) {
                result = bean.getNodeTargetEntityRefs("discounts", date);
            }
        }
        return expandGroups(result, discountGroups);
    }

    /**
     * Returns a set of references to <em>entity.discountType</em> entities
     * for a product.
     *
     * @param product        the product
     * @param date           the date
     * @param discountGroups the discount group cache
     * @return a set of <em>entity.discountType</em> references for the product
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObjectReference> getProductDiscounts(
            Product product, Date date, DiscountGroups discountGroups) {
        EntityBean bean = new EntityBean(product, service);
        Set<IMObjectReference> discounts = new HashSet<IMObjectReference>();
        discounts.addAll(bean.getNodeTargetEntityRefs("discounts", date));
        if (bean.hasNode("type")) {
            List<EntityRelationship> types = bean.getValues(
                    "type", EntityRelationship.class);
            for (EntityRelationship relationship : types) {
                IMObjectReference srcRef = relationship.getSource();
                if (srcRef != null) {
                    discounts.addAll(getProductTypeDiscounts(srcRef, date,
                                                             discountGroups));
                }
            }
        }
        return expandGroups(discounts, discountGroups);
    }

    /**
     * Returns discounts associated with an <em>entity.productType</em>,
     * active for the specified date.
     *
     * @param ref            the product type reference
     * @param date           the date
     * @param discountGroups the discount group cache
     * @return the discounts associated with the reference
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObjectReference> getProductTypeDiscounts(
            IMObjectReference ref, Date date, DiscountGroups discountGroups) {
        List<IMObjectReference> discounts
                = getRelatedEntityReferences(
                ref, "entityRelationship.discountProductType", "discounts",
                date);
        return expandGroups(discounts, discountGroups);
    }

    private Set<IMObjectReference> expandGroups(
            Collection<IMObjectReference> discounts,
            DiscountGroups groups) {
        if (discounts.isEmpty()) {
            return Collections.emptySet();
        }
        Set<IMObjectReference> result = new HashSet<IMObjectReference>();
        for (IMObjectReference ref : discounts) {
            if (TypeHelper.isA(ref, "entity.discountGroupType")) {
                result.addAll(groups.getDiscountTypes(ref));
            } else {
                result.add(ref);
            }
        }
        return result;
    }

    private List<IMObjectReference> getRelatedEntityReferences(
            IMObjectReference ref, String relationshipShortName,
            String collectionNodeName, Date date) {
        List<IMObjectReference> result;
        ShortNameConstraint rel = new ShortNameConstraint(
                "rel", relationshipShortName, true, false);
        ArchetypeQuery query = new ArchetypeQuery(
                new ObjectRefConstraint(ref));
        query.add(new CollectionNodeConstraint(collectionNodeName, rel));
        query.add(new ObjectRefSelectConstraint("rel.target"));

        OrConstraint startTime = new OrConstraint();
        startTime.add(new NodeConstraint("activeStartTime", IsNULL));
        startTime.add(new NodeConstraint("activeStartTime", LTE, date));

        OrConstraint endTime = new OrConstraint();
        endTime.add(new NodeConstraint("activeEndTime", IsNULL));
        endTime.add(new NodeConstraint("activeEndTime", GTE, date));

        rel.add(startTime);
        rel.add(endTime);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
        if (iter.hasNext()) {
            result = new ArrayList<IMObjectReference>();
            while (iter.hasNext()) {
                ObjectSet set = iter.next();
                result.add(set.getReference("rel.target.reference"));
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    private class DiscountGroups {
        private final Date date;
        private Map<IMObjectReference, List<IMObjectReference>> groups
                = new HashMap<IMObjectReference, List<IMObjectReference>>();


        public DiscountGroups(Date date) {
            this.date = date;
        }

        public List<IMObjectReference> getDiscountTypes(IMObjectReference ref) {
            List<IMObjectReference> result = groups.get(ref);
            if (result == null) {
                result = getRelatedEntityReferences(ref,
                                                    "entityRelationship.discountType",
                                                    "discounts", date);
                groups.put(ref, result);
            }
            return result;
        }

    }
}
