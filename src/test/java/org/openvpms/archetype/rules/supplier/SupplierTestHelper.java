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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.rules.supplier;

import org.openvpms.archetype.rules.stock.StockArchetypes;
import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.save;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


/**
 * Supplier test case helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SupplierTestHelper {

    /**
     * Package units for act items.
     */
    protected static final String PACKAGE_UNITS = "BOX";

    /**
     * Creates and saves a new stock location.
     *
     * @return a new stock location
     */
    public static Party createStockLocation() {
        Party stockLocation = (Party) create(StockArchetypes.STOCK_LOCATION);
        stockLocation.setName("STOCK-LOCATION-" + stockLocation.hashCode());
        save(stockLocation);
        return stockLocation;
    }

    /**
     * Helper to create a POSTED <em>act.supplierOrder</em> and corresponding <em>act.supplierOrderItem</em>.
     *
     * @param amount        the act total
     * @param supplier      the supplier
     * @param stockLocation the stockLocation
     * @param product       the product
     * @return a list containing the invoice act and its item
     */
    public static List<FinancialAct> createOrder(BigDecimal amount,
                                                 Party supplier,
                                                 Party stockLocation,
                                                 Product product) {
        FinancialAct act = (FinancialAct) create(SupplierArchetypes.ORDER);
        act.setStatus(OrderStatus.POSTED);
        FinancialAct item = createItem(SupplierArchetypes.ORDER_ITEM, product, BigDecimal.ONE,
                1, PACKAGE_UNITS, amount, amount);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("supplier", supplier);
        bean.addNodeParticipation("stockLocation", stockLocation);
        bean.addRelationship(SupplierArchetypes.ORDER_ITEM_RELATIONSHIP, item);
        bean.setValue("amount", item.getTotal());
        return Arrays.asList(act, item);
    }

    /**
     * Creates a new supplier act item.
     *
     * @param shortName    the act short name
     * @param product      the product
     * @param quantity     the quantity
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @param unitPrice    the unit price
     * @param listPrice    the list price
     * @return a new act
     */
    protected static FinancialAct createItem(String shortName, Product product, BigDecimal quantity, int packageSize,
                                             String packageUnits, BigDecimal unitPrice, BigDecimal listPrice) {
        FinancialAct item = (FinancialAct) create(shortName);
        ActBean bean = new ActBean(item);
        bean.addParticipation(StockArchetypes.STOCK_PARTICIPATION, product);
        item.setQuantity(quantity);
        bean.setValue("packageSize", packageSize);
        bean.setValue("packageUnits", packageUnits);
        bean.setValue("unitPrice", unitPrice);
        bean.setValue("listPrice", listPrice);
        ArchetypeServiceHelper.getArchetypeService().deriveValues(item);
        return item;
    }

}
