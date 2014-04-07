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

package org.openvpms.web.workspace.product.io;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Table model for product price import and export.
 *
 * @author Tim Anderson
 */
abstract class ProductImportExportTableModel<T> extends AbstractIMTableModel<T> {

    /**
     * The product id column.
     */
    protected static final int ID = 0;

    /**
     * The product name column.
     */
    protected static final int NAME = 1;

    /**
     * The product printed name column.
     */
    protected static final int PRINTED_NAME = 2;

    /**
     * The fixed price column.
     */
    protected static final int FIXED_PRICE = 3;

    /**
     * The fixed cost column.
     */
    protected static final int FIXED_COST = 4;

    /**
     * The fixed price max discount column.
     */
    protected static final int FIXED_MAX_DISCOUNT = 5;

    /**
     * The fixed price start date column.
     */
    protected static final int FIXED_START_DATE = 6;

    /**
     * The fixed price end date column.
     */
    protected static final int FIXED_END_DATE = 7;

    /**
     * The fixed price pricing locations column.
     */
    protected static final int FIXED_PRICING_GROUPS = 8;

    /**
     * The unit price column.
     */
    protected static final int UNIT_PRICE = 9;

    /**
     * The unit cost column.
     */
    protected static final int UNIT_COST = 10;

    /**
     * The unit price max discount column.
     */
    protected static final int UNIT_MAX_DISCOUNT = 11;

    /**
     * The unit price start date column.
     */
    protected static final int UNIT_START_DATE = 12;

    /**
     * The unit price end date column.
     */
    protected static final int UNIT_END_DATE = 13;

    /**
     * The unit price pricing locations column.
     */
    protected static final int UNIT_PRICING_GROUPS = 14;

    /**
     * Top-right alignment.
     */
    private static final Alignment TOP_RIGHT = new Alignment(Alignment.RIGHT, Alignment.TOP);

    /**
     * Constructs a {@link ProductImportExportTableModel}.
     */
    public ProductImportExportTableModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID, "product.import.id"));
        model.addColumn(createTableColumn(NAME, "product.import.name"));
        model.addColumn(createTableColumn(PRINTED_NAME, "product.import.printedName"));
        model.addColumn(createTableColumn(FIXED_PRICE, "product.import.fixedPrice"));
        model.addColumn(createTableColumn(FIXED_COST, "product.import.fixedCost"));
        model.addColumn(createTableColumn(FIXED_MAX_DISCOUNT, "product.import.fixedPriceMaxDiscount"));
        model.addColumn(createTableColumn(FIXED_START_DATE, "product.import.fixedPriceStartDate"));
        model.addColumn(createTableColumn(FIXED_END_DATE, "product.import.fixedPriceEndDate"));
        model.addColumn(createTableColumn(FIXED_PRICING_GROUPS, "product.import.unitPricingGroups"));
        model.addColumn(createTableColumn(UNIT_PRICE, "product.import.unitPrice"));
        model.addColumn(createTableColumn(UNIT_COST, "product.import.unitCost"));
        model.addColumn(createTableColumn(UNIT_MAX_DISCOUNT, "product.import.unitPriceMaxDiscount"));
        model.addColumn(createTableColumn(UNIT_START_DATE, "product.import.unitPriceStartDate"));
        model.addColumn(createTableColumn(UNIT_END_DATE, "product.import.unitPriceEndDate"));
        model.addColumn(createTableColumn(UNIT_PRICING_GROUPS, "product.import.unitPricingGroups"));
        setTableColumnModel(model);
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Formats a date.
     *
     * @param date the date. May be {@code null}
     * @return the formatted date, or {@code null} if {@code date} is {@code null}
     */
    protected String formatDate(Date date) {
        return date != null ? DateFormatter.formatDate(date, false) : null;
    }

    /**
     * Formats a right-aligned date.
     *
     * @param value the value. May be {@code null}
     * @return the formatted value, or {@code null} if {@code value} is {@code null}
     */
    protected Label rightAlign(Date value) {
        return rightAlign(formatDate(value));
    }

    /**
     * Formats a right aligned number.
     *
     * @param value the value. May be {@code null}
     * @return the formatted value, or {@code null} if {@code value} is {@code null}
     */
    protected Label rightAlign(BigDecimal value) {
        Label result = null;
        if (value != null) {
            String formatted = NumberFormatter.format(value);
            result = rightAlign(formatted);
        }
        return result;
    }

    /**
     * Formats a right-aligned value.
     *
     * @param value the value. May be {@code null}
     * @return the formatted value, or {@code null} if {@code value} is {@code null}
     */
    protected Label rightAlign(Object value) {
        Label result = null;
        if (value != null) {
            result = LabelFactory.create();
            result.setText(value.toString());
            TableLayoutData layoutData = new TableLayoutData();
            layoutData.setAlignment(TOP_RIGHT);
            result.setLayoutData(layoutData);
        }
        return result;
    }

}
