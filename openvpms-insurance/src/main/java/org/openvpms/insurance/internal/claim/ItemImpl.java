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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.claim.Item;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Default implementation of the {@link Item} interface.
 *
 * @author Tim Anderson
 */
public class ItemImpl implements Item {

    /**
     * The invoice item.
     */
    private final ActBean item;

    /**
     * Constructs a {@link ItemImpl}.
     *
     * @param item    the invoice item
     * @param service the archetype service
     */
    public ItemImpl(Act item, IArchetypeService service) {
        this.item = new ActBean(item, service);
    }

    /**
     * Returns the invoice item identifier.
     *
     * @return the invoice item identifier
     */
    @Override
    public long getId() {
        return item.getObject().getId();
    }

    /**
     * Returns the date when the invoice item was charged.
     *
     * @return the date
     */
    @Override
    public Date getDate() {
        return item.getAct().getActivityStartTime();
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    @Override
    public Product getProduct() {
        return (Product) item.getNodeParticipant("product");
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    @Override
    public BigDecimal getQuantity() {
        return item.getBigDecimal("quantity", BigDecimal.ZERO);
    }

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    @Override
    public BigDecimal getDiscount() {
        return item.getBigDecimal("discount", BigDecimal.ZERO);
    }

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    @Override
    public BigDecimal getDiscountTax() {
        return BigDecimal.ZERO;
    }

    /**
     * Returns the total amount, including tax.
     *
     * @return the total amount
     */
    @Override
    public BigDecimal getTotal() {
        return item.getBigDecimal("total", BigDecimal.ZERO);
    }

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    @Override
    public BigDecimal getTotalTax() {
        return item.getBigDecimal("tax", BigDecimal.ZERO);
    }
}
