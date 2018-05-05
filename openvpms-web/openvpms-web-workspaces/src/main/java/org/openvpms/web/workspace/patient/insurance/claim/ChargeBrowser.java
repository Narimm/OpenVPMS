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

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractBrowserListener;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.exists;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * Enables selection of invoice items for insurance claims.
 *
 * @author Tim Anderson
 */
public class ChargeBrowser {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The patient reference.
     */
    private final IMObjectReference patientRef;

    /**
     * The selected items.
     */
    private final Map<Act, Boolean> selected = new HashMap<>();

    /**
     * The existing selected charges.
     */
    private final Charges charges;

    /**
     * The item table.
     */
    private final PagedIMTable<Act> table;

    /**
     * The browser component.
     */
    private SplitPane component;

    /**
     * The item container.
     */
    private Column container = ColumnFactory.create(Styles.INSET);

    /**
     * The invoice statuses to query.
     */
    private static String[] STATUSES = {ActStatus.POSTED};

    /**
     * The archetypes to query.
     */
    private static final String[] ARCHETYPES = {INVOICE};

    /**
     * Constructs a {@link ChargeBrowser}.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param charges  the existing charges
     * @param from     the date to query invoices from. May be {@code null}
     * @param to       the date to query invoices to. May be {@code null}
     * @param context  the layout context
     */
    public ChargeBrowser(Party customer, Party patient, Charges charges, Date from, Date to, LayoutContext context) {
        this.patient = patient;
        this.patientRef = patient.getObjectReference();
        this.charges = charges;
        DateRangeActQuery<FinancialAct> query = new DefaultActQuery<>(customer, "customer",
                                                                      CustomerArchetypes.CUSTOMER_PARTICIPATION,
                                                                      ARCHETYPES, STATUSES);
        query.getComponent();
        query.setAllDates(false);
        query.setFrom(from);
        query.setTo(to);
        query.setConstraints(exists(
                subQuery(CustomerAccountArchetypes.INVOICE_ITEM, "i")
                        .add(join("invoice").add(idEq("source", "act")))
                        .add(join("patient").add(eq("entity", patient)))));
        Browser<FinancialAct> browser = BrowserFactory.create(query, context);
        browser.addBrowserListener(new AbstractBrowserListener<FinancialAct>() {
            @Override
            public void selected(FinancialAct object) {
                setSelectedInvoice(object);
            }
        });
        table = new PagedIMTable<>(new ItemTableModel(selected, context));
        component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "BrowserCRUDWorkspace.Layout",
                                            browser.getComponent(), container);
        browser.query();
    }

    /**
     * Returns the selected invoices.
     *
     * @return the selected invoices
     */
    public List<FinancialAct> getSelectedInvoices() {
        Map<IMObjectReference, FinancialAct> invoices = new HashMap<>();
        for (Act item : getSelectedItems()) {
            ActBean bean = new ActBean(item);
            IMObjectReference invoiceRef = bean.getNodeSourceObjectRef("invoice");
            if (invoiceRef != null && !invoices.containsKey(invoiceRef)) {
                FinancialAct invoice = (FinancialAct) IMObjectHelper.getObject(invoiceRef);
                if (invoice != null) {
                    invoices.put(invoiceRef, invoice);
                }
            }
        }
        return new ArrayList<>(invoices.values());
    }

    /**
     * Returns the selected invoice items.
     *
     * @return the selected invoice items
     */
    public List<Act> getSelectedItems() {
        List<Act> result = new ArrayList<>();
        for (Map.Entry<Act, Boolean> entry : selected.entrySet()) {
            if (entry.getValue()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Sets the selected invoice.
     *
     * @param invoice the invoice
     */
    private void setSelectedInvoice(FinancialAct invoice) {
        List<Act> matches = new ArrayList<>();
        ActBean bean = new ActBean(invoice);
        boolean claimed = false;
        boolean reversed = charges.isReversed(invoice);
        boolean allocated = false;
        if (!reversed) {
            allocated = charges.isPaid(invoice);
            if (allocated) {
                for (Reference itemRef : bean.getTargetRefs("items")) {
                    if (!charges.contains(itemRef)) {
                        Act item = charges.getItem(itemRef, patientRef);
                        if (item != null) {
                            if (!charges.isClaimed(item)) {
                                matches.add(item);
                                selected.put(item, Boolean.TRUE);
                            } else {
                                claimed = true;
                            }
                        }
                    } else {
                        claimed = true;
                    }
                }
            }
        }
        container.removeAll();
        if (!matches.isEmpty()) {
            table.setResultSet(new ListResultSet<>(matches, 20));
            container.add(table.getComponent());
        } else {
            Label label = LabelFactory.create(null, Styles.BOLD);
            String displayName = DescriptorHelper.getDisplayName(CustomerAccountArchetypes.INVOICE);
            String message;
            if (claimed) {
                message = Messages.format("patient.insurance.charge.allclaimed", displayName);
            } else if (allocated) {
                message = Messages.format("patient.insurance.charge.nocharges", displayName, patient.getName());
            } else if (reversed) {
                message = Messages.format("patient.insurance.charge.reversed", displayName);
            } else {
                message = Messages.format("patient.insurance.charge.unpaid", displayName);
            }
            label.setText(message);
            label.setLayoutData(ColumnFactory.layout(Alignment.ALIGN_CENTER));
            Column wrapper = ColumnFactory.create(Styles.LARGE_INSET, label);
            wrapper.setLayoutData(ColumnFactory.layout(Alignment.ALIGN_CENTER));

            container.add(wrapper);
        }
    }

    private static class ItemTableModel extends AbstractActTableModel {

        private final Map<Act, Boolean> selected;

        private int selectedIndex;

        /**
         * Constructs a {@code AbstractActTableModel}.
         *
         * @param selected the selections
         * @param context  the layout context
         */
        public ItemTableModel(Map<Act, Boolean> selected, LayoutContext context) {
            super(CustomerAccountArchetypes.INVOICE_ITEM, context);
            this.selected = selected;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the table column
         * @param row    the table row
         */
        @Override
        protected Object getValue(final Act object, TableColumn column, int row) {
            Object result;
            if (column.getModelIndex() == selectedIndex) {
                Boolean value = selected.get(object);
                final CheckBox box = new CheckBox();
                box.setSelected(value != null && value);
                box.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        selected.put(object, box.isSelected());
                    }
                });
                result = box;
            } else {
                result = super.getValue(object, column, row);
            }
            return result;
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
            DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(shortNames, context);
            selectedIndex = getNextModelIndex(model);
            model.addColumn(new TableColumn(selectedIndex));
            model.moveColumn(model.getColumnCount() - 1, 0);
            return model;
        }
    }
}
