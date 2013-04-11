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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting.till;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.DateHelper;

import java.util.Date;


/**
 * Displays acts associated with a till balance.
 *
 * @author Tim Anderson
 */
public class TillActTableModel extends ActAmountTableModel<FinancialAct> {

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The customer model index.
     */
    private int customerIndex;

    /**
     * Constructs a {@code TillActTableModel}.
     *
     * @param help the help context
     */
    public TillActTableModel(HelpContext help) {
        super(true, false, true, true);
        this.help = help;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the object the object
     * @param column the table column
     * @param row    the table row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(FinancialAct act, TableColumn column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        if (index == DATE_INDEX) {
            Date date = act.getActivityStartTime();
            if (date != null) {
                result = DateHelper.formatDateTime(date, false);
            }
        } else if (index == customerIndex) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("customer")) {
                NodeDescriptor descriptor = bean.getDescriptor("customer");
                LayoutContext context = new DefaultLayoutContext(help);
                context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
                TableComponentFactory factory = new TableComponentFactory(context);
                context.setComponentFactory(factory);
                Property property = new IMObjectProperty(act, descriptor);
                result = factory.create(property, act).getComponent();
            }
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }

    /**
     * Helper to create a column model.
     * Adds a customer column before the amount index.
     *
     * @param showArchetype determines if the archetype column should be displayed
     * @param showStatus    determines if the status colunn should be displayed
     * @param showAmount    determines if the credit/debit amount should be displayed
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(boolean showArchetype, boolean showStatus, boolean showAmount) {
        DefaultTableColumnModel model
                = (DefaultTableColumnModel) super.createColumnModel(showArchetype, showStatus,
                                                                    showAmount);
        customerIndex = getNextModelIndex(model);
        TableColumn column = createTableColumn(
                customerIndex, "tillacttablemodel.customer");
        model.addColumn(column);
        if (showAmount) {
            model.moveColumn(model.getColumnCount() - 1,
                             getColumnOffset(model, AMOUNT_INDEX));
        }
        return model;
    }

}
