/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.discount;

import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
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

import static org.openvpms.component.system.common.query.RelationalOp.GTE;
import static org.openvpms.component.system.common.query.RelationalOp.IS_NULL;
import static org.openvpms.component.system.common.query.RelationalOp.LTE;


/**
 * Discount rules.
 *
 * @author Tim Anderson
 */
public class DiscountRules {

    /**
     * Percentage discount type.
     */
    public static final String PERCENTAGE = "PERCENTAGE";

    /**
     * Fixed discount type.
     */
    public static final String FIXED = "FIXED";

    /**
     * At-cost with rate discount type.
     */
    public static final String COST_RATE = "COST_RATE";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a {@link DiscountRules}.
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
     * calculate the discount amount.
     * <p/>
     * If the discount amount exceeds the maximum discount calculated by:
     * <p/>
     * <code>(fixedPrice * maxFixedPriceDiscount/100) + qty * (unitPrice * maxUnitPriceDiscount/100)</code>
     * <p/>
     * then the maximum discount amount will be returned.
     *
     * @param date                  the date, used to determine if a discount applies
     * @param practice              the practice. May be {@code null}
     * @param customer              the customer
     * @param patient               the patient. May be {@code null}
     * @param product               the product
     * @param fixedCost             the fixed cost
     * @param unitCost              the unit cost
     * @param fixedPrice            the fixed amount
     * @param unitPrice             the unit price
     * @param quantity              the quantity
     * @param maxFixedPriceDiscount the maximum fixed price discount percentage
     * @param maxUnitPriceDiscount  the maximum unit price discount percentage
     * @return the discount amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateDiscount(Date date, Party practice, Party customer, Party patient, Product product,
                                        BigDecimal fixedCost, BigDecimal unitCost,
                                        BigDecimal fixedPrice, BigDecimal unitPrice,
                                        BigDecimal quantity,
                                        BigDecimal maxFixedPriceDiscount, BigDecimal maxUnitPriceDiscount) {
        BigDecimal discount;
        BigDecimal taxRate = BigDecimal.ZERO;
        if (practice != null) {
            CustomerTaxRules taxRules = new CustomerTaxRules(practice, service);
            taxRate = taxRules.getTaxRate(product, customer);
        }

        if (fixedPrice.compareTo(BigDecimal.ZERO) == 0
            && (unitPrice.compareTo(BigDecimal.ZERO) == 0 || quantity.compareTo(BigDecimal.ZERO) == 0)) {
            discount = BigDecimal.ZERO;
        } else {
            List<Entity> discounts = getDiscounts(date, customer, patient, product);
            if (discounts.isEmpty()) {
                discount = BigDecimal.ZERO;
            } else {
                discount = calculateDiscountAmount(fixedPrice, unitPrice, fixedCost, unitCost, quantity,
                                                   taxRate, discounts);
                BigDecimal maxDiscount = calculateMaxDiscount(fixedPrice, unitPrice, quantity, maxFixedPriceDiscount,
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
     * This is the union of all <em>entity.discountType</em> entities associated with the customer and patient that are
     * also associated with the product.
     * <p/>
     * If a customer, patient or product has <em>entity.discountGroupType</em> entities, the
     * associated <em>entity.discountType</em> entities will be included.
     *
     * @param date     the date, used to determine if a discount applies
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @param product  the product
     * @return the discount entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getDiscounts(Date date, Party customer, Party patient, Product product) {
        List<Entity> result = Collections.emptyList();
        DiscountGroups groups = new DiscountGroups(date);
        Set<IMObjectReference> productSet = getProductDiscounts(product, date, groups);

        if (!productSet.isEmpty()) {
            Set<IMObjectReference> customerSet = getPartyDiscounts(customer, date, groups);
            Set<IMObjectReference> patientSet = getPartyDiscounts(patient, date, groups);
            Set<IMObjectReference> partySet = new HashSet<>(customerSet);
            partySet.addAll(patientSet);
            Set<IMObjectReference> refs = new HashSet<>(productSet);
            refs.retainAll(partySet);
            if (!refs.isEmpty()) {
                result = new ArrayList<>();
                for (IMObjectReference ref : refs) {
                    Entity discount = (Entity) service.get(ref);
                    if (discount != null && discount.isActive()) {
                        result.add(discount);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Calculates the discount amount for an act, given a list of discounts.
     * <p/>
     * If there are multiple COST_RATE discounts, all bar the one that gives the best discount will be excluded.
     *
     * @param fixedPrice the fixed price
     * @param unitPrice  the unit price
     * @param fixedCost  the fixed cost price
     * @param unitCost   the unit cost price
     * @param quantity   the quantity
     * @param taxRate    the taxRate expressed as a percentage. Only applicable to COST_RATE discounts
     * @param discounts  a set of <em>entity.discountType</em>s. This list is modified if there are multiple
     *                   COST_RATE discounts
     * @return the discount amount for the act
     */
    private BigDecimal calculateDiscountAmount(BigDecimal fixedPrice, BigDecimal unitPrice, BigDecimal fixedCost,
                                               BigDecimal unitCost, BigDecimal quantity, BigDecimal taxRate,
                                               List<Entity> discounts) {
        BigDecimal result = BigDecimal.ZERO;
        Entity lastCostDiscount = null;
        BigDecimal lastCostRate = null;
        boolean lastCostDiscountFixed = false;

        // ensure there is only one COST_RATE discount present. This selects the one with the lowest rate hence giving
        // the greatest discount
        for (Entity discount : discounts.toArray(new Entity[discounts.size()])) {
            IMObjectBean discountBean = new IMObjectBean(discount, service);
            String discountType = discountBean.getString("type");
            if (COST_RATE.equals(discountType)) {
                BigDecimal rate = discountBean.getBigDecimal("rate", BigDecimal.ZERO);
                boolean discountFixed = discountBean.getBoolean("discountFixed");
                if (lastCostDiscount == null || lessThan(rate, lastCostRate, discountFixed, lastCostDiscountFixed)) {
                    if (lastCostDiscount != null) {
                        discounts.remove(lastCostDiscount);
                    }
                    lastCostDiscount = discount;
                    lastCostRate = rate;
                    lastCostDiscountFixed = discountFixed;
                } else {
                    discounts.remove(discount);
                }
            }
        }
        for (Entity discount : discounts) {
            IMObjectBean discountBean = new IMObjectBean(discount, service);
            String discountType = discountBean.getString("type");
            BigDecimal rate = discountBean.getBigDecimal("rate", BigDecimal.ZERO);
            boolean discountFixed = discountBean.getBoolean("discountFixed");
            BigDecimal dFixedPrice;
            if (discountFixed) {
                BigDecimal fixedQty = new BigDecimal(quantity.compareTo(BigDecimal.ZERO)).abs();
                dFixedPrice = fixedQty.multiply(calcDiscount(fixedCost, fixedPrice, rate, taxRate, discountType));
            } else {
                dFixedPrice = BigDecimal.ZERO;
            }
            BigDecimal dUnitPrice = calcDiscount(unitCost, unitPrice, rate, taxRate, discountType);
            BigDecimal amount;
            if (PERCENTAGE.equals(discountType) || COST_RATE.equals(discountType)) {
                if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                    amount = BigDecimal.ZERO;
                } else {
                    amount = quantity.abs().multiply(dUnitPrice).add(dFixedPrice);
                }
            } else {
                amount = dUnitPrice.add(dFixedPrice);
            }
            result = result.add(amount);
        }
        return result;
    }

    /**
     * Determines if a cost rate discount is less than another.
     * If the rates are the same, the discount flags are used.
     *
     * @param costRate1      the first cost rate
     * @param costRate2      the second cost rate
     * @param discountFixed1 the first cost rate "discount fixed" flag
     * @param discountFixed2 the second cost rate "discount fixed" flag
     * @return <ul>
     * <li>{@code true} if {@code costRate1 < codeRate2}; or</li>
     * <li>{@code true} if {@code costRate1 == codeRate2} && discountFixed1 == false && discountFixed2 == true;
     * </li>
     * <li>{@code false} otherwise</li>
     * </ul>
     */
    private boolean lessThan(BigDecimal costRate1, BigDecimal costRate2,
                             boolean discountFixed1, boolean discountFixed2) {
        int comp = costRate1.compareTo(costRate2);
        return comp < 0 || comp == 0 && (!discountFixed1 && discountFixed2);
    }

    /**
     * Calculates the maximum discount.
     *
     * @param fixedPrice        the fixed price
     * @param unitPrice         the unit price
     * @param quantity          the quantity
     * @param fixedDiscountRate the fixed price discount rate
     * @param unitDiscountRate  the unit price discount rate
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
     * Calculates the discount for the given {@code discountType}.
     * <ul>
     * <li>PERCENTAGE - {@code result = price * rate / 100} </li>
     * <li>COST_RATE  - {@code discountedCostPrice = costPrice + (costPrice * rate / 100)} <br/>
     * {@code result = price - (discountedCostPrice + (discountedCostPrice * taxRate / 100)}</li>
     * <li>FIXED - {@code result = rate}</li>
     * </ul>
     *
     * @param costPrice    the cost price
     * @param price        the price
     * @param rate         the rate
     * @param taxRate      the taxRate expressed as a percentage
     * @param discountType the discount type
     * @return amount * discountRate/100
     */
    private BigDecimal calcDiscount(BigDecimal costPrice, BigDecimal price, BigDecimal rate, BigDecimal taxRate,
                                    String discountType) {
        BigDecimal result;
        if (PERCENTAGE.equals(discountType)) {
            result = calcDiscount(price, rate);
        } else if (COST_RATE.equals(discountType)) {
            BigDecimal discountedCostPrice = costPrice.add(calcDiscount(costPrice, rate));
            // subtract cost+rate+tax to generate discount
            result = price.subtract(discountedCostPrice.add(calcDiscount(discountedCostPrice, taxRate)));
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
        result = MathRules.divide(result, MathRules.ONE_HUNDRED, 3);
        return result;
    }

    /**
     * Returns a set of references to <em>entity.discountType</em> references
     * for a party.
     *
     * @param party          the party. May be {@code null}
     * @param date           the date that the discounts apply to
     * @param discountGroups the discount group cache
     * @return a set of <em>entity.discountType</em> references for the party
     */
    private Set<IMObjectReference> getPartyDiscounts(Party party, Date date, DiscountGroups discountGroups) {
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
    private Set<IMObjectReference> getProductDiscounts(Product product, Date date, DiscountGroups discountGroups) {
        EntityBean bean = new EntityBean(product, service);
        Set<IMObjectReference> discounts = new HashSet<>();
        if (bean.hasNode("discounts")) {
            discounts.addAll(bean.getNodeTargetEntityRefs("discounts", date));
            if (bean.hasNode("type")) {
                IMObjectReference type = bean.getNodeTargetObjectRef("type");
                if (type != null) {
                    discounts.addAll(getProductTypeDiscounts(type, date, discountGroups));
                }
            }
        }
        return expandGroups(discounts, discountGroups);
    }

    /**
     * Returns discounts associated with an <em>entity.productType</em>, active for the specified date.
     *
     * @param ref            the product type reference
     * @param date           the date
     * @param discountGroups the discount group cache
     * @return the discounts associated with the reference
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObjectReference> getProductTypeDiscounts(IMObjectReference ref, Date date,
                                                           DiscountGroups discountGroups) {
        List<IMObjectReference> discounts = getRelatedEntityReferences(
                ref, "entityLink.productTypeDiscount", "discounts", date);
        return expandGroups(discounts, discountGroups);
    }

    private Set<IMObjectReference> expandGroups(Collection<IMObjectReference> discounts, DiscountGroups groups) {
        if (discounts.isEmpty()) {
            return Collections.emptySet();
        }
        Set<IMObjectReference> result = new HashSet<>();
        for (IMObjectReference ref : discounts) {
            if (TypeHelper.isA(ref, "entity.discountGroupType")) {
                result.addAll(groups.getDiscountTypes(ref));
            } else {
                result.add(ref);
            }
        }
        return result;
    }

    private List<IMObjectReference> getRelatedEntityReferences(IMObjectReference ref, String relationshipShortName,
                                                               String collectionNodeName, Date date) {
        List<IMObjectReference> result;
        ShortNameConstraint rel = new ShortNameConstraint("rel", relationshipShortName, true, false);
        ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint(ref));
        query.add(new CollectionNodeConstraint(collectionNodeName, rel));
        query.add(new ObjectRefSelectConstraint("rel.target"));

        OrConstraint startTime = new OrConstraint();
        startTime.add(new NodeConstraint("activeStartTime", IS_NULL));
        startTime.add(new NodeConstraint("activeStartTime", LTE, date));

        OrConstraint endTime = new OrConstraint();
        endTime.add(new NodeConstraint("activeEndTime", IS_NULL));
        endTime.add(new NodeConstraint("activeEndTime", GTE, date));

        rel.add(startTime);
        rel.add(endTime);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
        if (iter.hasNext()) {
            result = new ArrayList<>();
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

        private Map<IMObjectReference, List<IMObjectReference>> groups = new HashMap<>();

        public DiscountGroups(Date date) {
            this.date = date;
        }

        public List<IMObjectReference> getDiscountTypes(IMObjectReference ref) {
            List<IMObjectReference> result = groups.get(ref);
            if (result == null) {
                result = getRelatedEntityReferences(ref, "entityLink.discountType", "discounts", date);
                groups.put(ref, result);
            }
            return result;
        }

    }
}
