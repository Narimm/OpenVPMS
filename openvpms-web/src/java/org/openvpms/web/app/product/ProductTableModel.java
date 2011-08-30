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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.product;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;

import java.math.BigDecimal;
import java.text.NumberFormat;


/**
 * Table model for <em>product.*</em> objects. Displays the fixed and unit
 * prices if available.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductTableModel extends BaseIMObjectTableModel<Product> {

    /**
     * The fixed price model index.
     */
    private int fixedPriceIndex;

    /**
     * The unit price model index.
     */
    private int unitPriceIndex;


    /**
     * Constructs a new <code>ProductTableModel</code>.
     */
    public ProductTableModel() {
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param product the object
     * @param column  the column
     * @param row     the row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(Product product, TableColumn column, int row) {
        int index = column.getModelIndex();
        if (index == fixedPriceIndex) {
            return getPrice("productPrice.fixedPrice", product);
        } else if (index == unitPriceIndex) {
            return getPrice("productPrice.unitPrice", product);
        }
        return super.getValue(product, column, row);
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
        fixedPriceIndex = getNextModelIndex(model);
        unitPriceIndex = fixedPriceIndex + 1;
        TableColumn fixedPrice = createTableColumn(fixedPriceIndex, "producttablemodel.fixedPrice");
        TableColumn unitPrice = createTableColumn(unitPriceIndex, "producttablemodel.unitPrice");
        model.addColumn(fixedPrice);
        model.addColumn(unitPrice);
        return model;
    }

    /**
     * Returns a component for a product price.
     *
     * @param shortName the product price short name
     * @param product   the product
     * @return a component for the product price corresponding to
     *         <code>shortName</code> or <code>null</code> if none is found
     */
    private Component getPrice(String shortName, Product product) {
        Component result = null;
        ProductPrice price = IMObjectHelper.getObject(
                shortName, product.getProductPrices());
        if (price != null) {
            BigDecimal value = price.getPrice();
            if (value != null) {
                Label label = LabelFactory.create();
                String text = NumberFormatter.format(
                        value, NumberFormat.getCurrencyInstance());
                label.setText(text);
                TableLayoutData layout = new TableLayoutDataEx();
                Alignment right = new Alignment(Alignment.RIGHT,
                                                Alignment.DEFAULT);
                layout.setAlignment(right);
                label.setLayoutData(layout);
                result = label;
            }
        }
        return result;
    }
}
