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

package org.openvpms.archetype.rules.workflow;

import org.joda.time.LocalTime;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;
import java.util.HashMap;

/**
 * Wrapper around <em>entity.cageType</em>.
 *
 * @author Tim Anderson
 */
public class CageType {

    /**
     * The cage type bean.
     */
    private final IMObjectBean bean;

    /**
     * Constructs a {@link CageType}.
     *
     * @param entity  the <em>entity.cageType</em>
     * @param service the archetype service
     */
    public CageType(Entity entity, IArchetypeService service) {
        bean = new IMObjectBean(entity, service);
    }

    /**
     * Returns the first pet product, for single day boarding.
     *
     * @return the first pet product
     */
    public Product getFirstPetProductDay() {
        return (Product) bean.getNodeTargetObject("firstPetProductDay");
    }

    /**
     * Returns the second pet product, for single day boarding.
     *
     * @return the second pet product. May be {@code null}
     */
    public Product getSecondPetProductDay() {
        return (Product) bean.getNodeTargetObject("secondPetProductDay");
    }

    /**
     * Returns the first pet product, for overnight boarding.
     *
     * @return the first pet product. May be {@code null}
     */
    public Product getFirstPetProductOvernight() {
        return (Product) bean.getNodeTargetObject("firstPetProductNight");
    }

    /**
     * Returns the second pet product, for overnight boarding.
     *
     * @return the second pet product. May be {@code null}
     */
    public Product getSecondPetProductOvernight() {
        return (Product) bean.getNodeTargetObject("secondPetProductNight");
    }

    /**
     * Returns the product to charge.
     *
     * @param days      the no. of days being stayed
     * @param overnight determines if the pet is staying overnight, when {@code days == 1}
     * @param firstPet  if {@code true}, the first-pet product should be used, otherwise the second-pet product should
     *                  be used
     * @return the product
     */
    public Product getProduct(int days, boolean overnight, boolean firstPet) {
        Product result;
        if (firstPet) {
            result = getFirstPetProduct(days, overnight);
        } else {
            result = getSecondPetProduct(days, overnight);
        }
        return result;
    }

    /**
     * Determines if a time indicates a late checkout.
     *
     * @param time the time
     * @return {@code true} if the time is a late checkout
     */
    public boolean isLateCheckout(Date time) {
        Date lateCheckoutTime = bean.getDate("lateCheckoutTime");
        if (lateCheckoutTime != null) {
            LocalTime l1 = new LocalTime(lateCheckoutTime);
            LocalTime l2 = new LocalTime(time);
            if (l1.compareTo(l2) <= 0) {
                return true;
            }
        }
        return false;
    }

    public Product getLateCheckoutProduct() {
        return (Product) bean.getNodeTargetObject("lateCheckoutProduct");
    }

    /**
     * Determines if the cage type charges different products for second pets.
     *
     * @return {@code true} if the cage type charges different products
     */
    public boolean hasSecondPetProducts() {
        return bean.getNodeTargetObjectRef("secondPetProductDay") != null
               || bean.getNodeTargetObjectRef("secondPetProductNight") != null;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CageType && bean.getObject().equals(((CageType) obj).bean.getObject());
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return bean.getObject().hashCode();
    }

    /**
     * Returns the product to charge the first pet.
     * <p/>
     * If the pet is staying multiple days or {@code overnight == true}, the {@link #getFirstPetProductOvernight()} is
     * used.
     * If this is not specified, then the {@link #getFirstPetProductDay()} is used.
     *
     * @param days       the no. of days the pet is staying
     * @param overnight determines if the pet is staying overnight, when {@code days == 1}
     * @return the first pet product
     */
    protected Product getFirstPetProduct(int days, boolean overnight) {
        Product result = null;
        if (days > 1 || overnight) {
            result = getFirstPetProductOvernight();
        }
        if (result == null) {
            result = getFirstPetProductDay();
        }
        return result;
    }

    /**
     * Returns the product to charge the second pet.
     * <p/>
     * If the second pet is staying multiple days or {@code overnight == true}, the
     * {@link #getSecondPetProductOvernight()} is used.
     * If this is not specified, then the {@link #getSecondPetProductDay()} is used.
     * If neither are present, then the {@link #getFirstPetProduct(int, boolean)} product is used.
     *
     * @param days       the no. of days the pet is staying
     * @param overnight determines if the pet is staying overnight, when {@code days == 1}
     * @return the second pet product
     */
    protected Product getSecondPetProduct(int days, boolean overnight) {
        Product result = null;
        if (days > 1 || overnight) {
            result = getSecondPetProductOvernight();
        }
        if (result == null) {
            result = getSecondPetProductDay();
        }
        if (result == null) {
            result = getFirstPetProduct(days, overnight);
        }
        return result;
    }

}
