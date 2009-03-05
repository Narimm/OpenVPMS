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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.product;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;


/**
 * Assembles {@link ProductPrice} instances from {@link ProductPriceDO}
 * instances and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductPriceAssembler
        extends IMObjectAssembler<ProductPrice, ProductPriceDO> {

    /**
     * Assembles sets of lookups.
     */
    private SetAssembler<Lookup, LookupDO> LOOKUPS
            = SetAssembler.create(Lookup.class, LookupDO.class);


    /**
     * Creates a new <tt>ProductPriceAssembler</tt>.
     */
    public ProductPriceAssembler() {
        super(ProductPrice.class, ProductPriceDO.class,
              ProductPriceDOImpl.class);
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
    protected void assembleDO(ProductPriceDO target, ProductPrice source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setFixed(source.isFixed());
        target.setFromDate(source.getFromDate());
        target.setPrice(source.getPrice());
        target.setToDate(source.getToDate());

        ProductDO product = null;
        DOState productState = getDO(source.getProduct(),
                                     context);
        if (productState != null) {
            product = (ProductDO) productState.getObject();
            state.addState(productState);
        }
        target.setProduct(product);

        LOOKUPS.assembleDO(target.getClassifications(),
                           source.getClassifications(),
                           state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(ProductPrice target, ProductPriceDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setFixed(source.isFixed());
        target.setFromDate(source.getFromDate());
        target.setPrice(source.getPrice());
        target.setProduct(
                getObject(source.getProduct(), Product.class, context));
        target.setToDate(source.getToDate());

        LOOKUPS.assembleObject(target.getClassifications(),
                               source.getClassifications(),
                               context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected ProductPrice create(ProductPriceDO object) {
        return new ProductPrice();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ProductPriceDO create(ProductPrice object) {
        return new ProductPriceDOImpl();
    }
}
