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

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.workspace.customer.StockOnHand;

import java.math.BigDecimal;

/**
 * A table model for <em>act.supplierOrderItem</em> acts.
 * <p/>
 * This displays the stock on hand for a product.
 *
 * @author Tim Anderson
 */
public class OrderItemTableModel extends DescriptorTableModel<IMObject> {

    /**
     * The stock location. May be {@code null}
     */
    private final IMObjectReference stockLocation;

    /**
     * Used to display stock on hand. May be {@code null}
     */
    private final StockOnHand stock;

    /**
     * On hand table model index.
     */
    private int onHandIndex = -1;

    /**
     * Constructs a {@link DescriptorTableModel}.
     * <p/>
     * The column model must be set using {@link #setTableColumnModel}.
     *
     * @param stockLocation the stock location
     * @param stock         used to display a stock-on-hand column
     * @param context       the layout context
     */
    public OrderItemTableModel(IMObjectReference stockLocation, StockOnHand stock, LayoutContext context) {
        super(context);
        this.stockLocation = stockLocation;
        this.stock = stock;
        setTableColumnModel(createColumnModel(new String[]{SupplierArchetypes.ORDER_ITEM}, context));
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, TableColumn column, int row) {
        if (column.getModelIndex() == onHandIndex) {
            return getOnHand(object);
        }
        return super.getValue(object, column, row);
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        TableColumnModel model = super.createColumnModel(shortNames, context);
        TableColumn quantity = getColumn(model, "quantity");
        onHandIndex = getNextModelIndex(model);
        TableColumn onHand = new TableColumn(onHandIndex);
        onHand.setHeaderValue(Messages.get("product.stock.onhand"));
        addColumnAfter(onHand, quantity.getModelIndex(), model);
        return model;
    }

    /**
     * Returns a component representing the stock on hand.
     *
     * @param object the act
     * @return a component representing the stock on
     */
    private Component getOnHand(IMObject object) {
        FinancialAct act = (FinancialAct) object;
        ActBean bean = new ActBean(act);
        IMObjectReference product = bean.getNodeParticipantRef("product");
        BigDecimal value = stock.getStock(product, stockLocation);
        return TableHelper.rightAlign(NumberFormatter.format(value));
    }

}
