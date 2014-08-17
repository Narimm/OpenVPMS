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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.QueryAdapter;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetAdapter;
import org.openvpms.web.component.im.util.LookupNameHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Stock export query.
 * <p/>
 * Adapts the {@link ObjectSet}s returned by {@link  StockExportObjectSetQuery} to {@link StockData} instances.
 *
 * @author Tim Anderson
 */
public class StockExportQuery extends QueryAdapter<ObjectSet, StockData> {

    /**
     * Constructs a {@link StockExportQuery}.
     */
    public StockExportQuery(Party stockLocation) {
        super(new StockExportObjectSetQuery(stockLocation), StockData.class);
    }

    /**
     * Sets the stock location.
     *
     * @param stockLocation the stock location. May be {@code null}
     */
    public void setStockLocation(Party stockLocation) {
        getQuery().setStockLocation(stockLocation);
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location. May be {@code null}
     */
    public Party getStockLocation() {
        return getQuery().getStockLocation();
    }

    /**
     * Sets the product type.
     *
     * @param productType the product type. May be {@code null}
     */
    public void setProductType(Entity productType) {
        getQuery().setProductType(productType);
    }

    /**
     * Sets the product group.
     *
     * @param productGroup the product group classification code. May be {@code null}
     */
    public void setProductGroup(String productGroup) {
        getQuery().setProductGroup(productGroup);
    }

    /**
     * Sets the income type.
     *
     * @param incomeType the income type classification code. May be {@code null}
     */
    public void setIncomeType(String incomeType) {
        getQuery().setIncomeType(incomeType);
    }

    /**
     * Returns the underlying query.
     *
     * @return the underlying query
     */
    @Override
    public StockExportObjectSetQuery getQuery() {
        return (StockExportObjectSetQuery) super.getQuery();
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    @Override
    protected ResultSet<StockData> convert(ResultSet<ObjectSet> set) {
        return new ResultSetAdapter<ObjectSet, StockData>(set) {
            @Override
            protected IPage<StockData> convert(IPage<ObjectSet> page) {
                Entity stockLocation = getQuery().getStockLocation();
                boolean zeroNegativeQuantities = getQuery().getZeroNegativeQuantities();
                List<StockData> objects = new ArrayList<StockData>();
                for (ObjectSet set : page.getResults()) {
                    Product product = (Product) set.get("product");
                    EntityRelationship relationship = (EntityRelationship) set.get("relationship");
                    StockData data = createStockData(stockLocation, product, relationship, zeroNegativeQuantities);
                    objects.add(data);
                }
                return new Page<StockData>(objects, page.getFirstResult(), page.getPageSize(), page.getTotalResults());
            }
        };
    }

    /**
     * Creates a {@link StockData}.
     *
     * @param stockLocation          the stock location
     * @param product                the product
     * @param relationship           the product-stock location relationship
     * @param zeroNegativeQuantities if {@code true}, set the {@link StockData#getNewQuantity() new quantity} to zero
     *                               if the stock quantity is negative
     * @return a new {@link StockData}
     */
    private StockData createStockData(Entity stockLocation, Product product, EntityRelationship relationship,
                                      boolean zeroNegativeQuantities) {
        String sellingUnits = LookupNameHelper.getName(product, "sellingUnits");
        IMObjectBean bean = new IMObjectBean(relationship);
        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        BigDecimal newQuantity = (quantity.signum() == -1 && zeroNegativeQuantities) ? BigDecimal.ZERO : quantity;
        return new StockData(stockLocation.getId(), stockLocation.getName(), product.getId(), product.getName(),
                             sellingUnits, quantity, newQuantity);
    }

}
