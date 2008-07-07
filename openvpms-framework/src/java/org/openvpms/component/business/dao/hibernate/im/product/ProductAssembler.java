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
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityAssembler;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductAssembler extends EntityAssembler<Product, ProductDO> {

    private SetAssembler<ProductPrice, ProductPriceDO> PRICES
            = SetAssembler.create(ProductPrice.class, ProductPriceDO.class);

    public ProductAssembler() {
        super(Product.class, ProductDO.class);
    }

    @Override
    protected void assembleDO(ProductDO result, Product source,
                              Context context) {
        super.assembleDO(result, source, context);
        PRICES.assemble(result.getProductPrices(), source.getProductPrices(),
                        context);
    }

    @Override
    protected void assembleObject(Product result, ProductDO source,
                                  Context context) {
        super.assembleObject(result, source, context);
    }

    protected Product create(ProductDO object) {
        return new Product();
    }

    protected ProductDO create(Product object) {
        return new ProductDO();
    }
}
