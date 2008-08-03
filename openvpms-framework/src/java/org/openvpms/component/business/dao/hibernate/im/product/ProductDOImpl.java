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

import org.openvpms.component.business.dao.hibernate.im.entity.EntityDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link ProductDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-14 16:43:53 +1100 (Thu, 14 Dec 2006) $
 */
public class ProductDOImpl extends EntityDOImpl implements ProductDO {

    /**
     * The prices for this product.
     */
    private Set<ProductPriceDO> prices = new HashSet<ProductPriceDO>();


    /**
     * Default constructor.
     */
    public ProductDOImpl() {
        // do nothing
    }

    /**
     * Creates a new <tt>ProductDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public ProductDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the prices.
     *
     * @return the prices
     */
    public Set<ProductPriceDO> getProductPrices() {
        return prices;
    }

    /**
     * Adds a product price.
     *
     * @param price the price to add
     */
    public void addProductPrice(ProductPriceDO price) {
        price.setProduct(this);
        prices.add(price);
    }

    /**
     * Removes a product price.
     *
     * @param price the price to remove
     */
    public void removeProductPrice(ProductPriceDO price) {
        prices.remove(price);
    }

    /**
     * Sets the prices.
     *
     * @param prices the prices
     */
    protected void setProductPrices(Set<ProductPriceDO> prices) {
        this.prices = prices;
    }

}
