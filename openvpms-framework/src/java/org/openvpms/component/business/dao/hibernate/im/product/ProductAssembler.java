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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.product;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityAssembler;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.util.Set;


/**
 * Assembles {@link Product}s from {@link ProductDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class ProductAssembler extends EntityAssembler<Product, ProductDO> {

    /**
     * Assembles sets of prices.
     */
    private SetAssembler<ProductPrice, ProductPriceDO> PRICES
            = SetAssembler.create(ProductPrice.class, ProductPriceDO.class);

    /**
     * Constructs a {@link ProductAssembler}.
     */
    public ProductAssembler() {
        super(org.openvpms.component.model.product.Product.class, Product.class, ProductDO.class, ProductDOImpl.class);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleDO(ProductDO target, Product source, DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        PRICES.assembleDO(target.getProductPrices(), (Set<ProductPrice>) (Set) source.getProductPrices(), state,
                          context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleObject(Product target, ProductDO source, Context context) {
        super.assembleObject(target, source, context);
        PRICES.assembleObject((Set<ProductPrice>) (Set) target.getProductPrices(), source.getProductPrices(), context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected Product create(ProductDO object) {
        return new Product();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ProductDO create(Product object) {
        return new ProductDOImpl();
    }
}
