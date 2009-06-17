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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.history;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.SelectionHistory;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.resource.util.Messages;


/**
 * Browser of customer and patient selection history.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerPatientHistoryBrowser extends TableBrowser<CustomerPatient> {

    /**
     * Construct a new <code>TableBrowser</code> that queries objects using the
     * specified query, displaying them in the table.
     */
    public CustomerPatientHistoryBrowser() {
        super(createQuery(), null, new HistoryModel());
    }

    /**
     * Returns the selected party (i.e customer or patient).
     *
     * @return the selected party. May be <tt>null</tt>
     */
    public Party getSelectedParty() {
        return ((HistoryModel) getTableModel()).getSelectedParty();
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<CustomerPatient> createTable(
            IMTableModel<CustomerPatient> model) {
        PagedIMTable<CustomerPatient> table = super.createTable(model);
        ((HistoryModel) model).setBrowser(this);
        return table;
    }

    /**
     * Creates the query.
     *
     * @return a new query
     */
    private static CustomerPatientHistoryQuery createQuery() {
        GlobalContext context = GlobalContext.getInstance();
        SelectionHistory customers = context.getHistory(Context.CUSTOMER_SHORTNAME);
        SelectionHistory patients = context.getHistory(Context.PATIENT_SHORTNAME);
        return new CustomerPatientHistoryQuery(customers, patients);
    }

    /**
     * Table model for {@link CustomerPatient} instances.
     */
    private static class HistoryModel extends AbstractIMTableModel<CustomerPatient> {

        /**
         * The owning browser.
         */
        private CustomerPatientHistoryBrowser browser;

        /**
         * The selected party. May be <tt>null</tt>
         */
        private Party party;

        /**
         * The customer column index.
         */
        private static final int CUSTOMER_INDEX = 0;

        /**
         * The patient column index.
         */
        private static final int PATIENT_INDEX = 1;


        /**
         * Creates a new <tt>HistoryModel</tt>.
         */
        public HistoryModel() {
            TableColumnModel columns = new DefaultTableColumnModel();
            columns.addColumn(createTableColumn(CUSTOMER_INDEX, "history.customer"));
            columns.addColumn(createTableColumn(PATIENT_INDEX, "history.patient"));
            setTableColumnModel(columns);
        }

        /**
         * Sets the owning browser.
         *
         * @param browser the browser
         */
        public void setBrowser(CustomerPatientHistoryBrowser browser) {
            this.browser = browser;
        }

        /**
         * Returns the selected party.
         *
         * @return the selected party. May be <tt>null</tt>
         */
        public Party getSelectedParty() {
            return party;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        protected Object getValue(CustomerPatient object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case CUSTOMER_INDEX:
                    result = getViewer(object, object.getCustomer());
                    break;
                case PATIENT_INDEX:
                    result = getViewer(object, object.getPatient());
                    break;
            }
            return result;
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if <tt>true</tt> sort in ascending order; otherwise
         *                  sort in <tt>descending</tt> order
         * @return the sort criteria, or <tt>null</tt> if the column isn't sortable
         */
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            if (column == CUSTOMER_INDEX) {
                return new SortConstraint[]{new NodeSortConstraint("customer", ascending)};
            } else if (column == PATIENT_INDEX) {
                return new SortConstraint[]{new NodeSortConstraint("patient", ascending)};
            }
            return null;
        }

        /**
         * Returns a viewer for the specified customer/patient pair.
         *
         * @param pair  the customer/patient pair
         * @param party the party in the pair being displayed. May be <tt>null</tt>
         * @return a new component, or <tt>null</tt> if <tt>party</tt> is <tt>null</tt>
         */
        private Component getViewer(final CustomerPatient pair, final Party party) {
            if (party != null) {
                String text;
                String name = party.getName();
                String description = party.getDescription();
                if (name == null && description == null) {
                    text = Messages.get("imobject.none");
                } else if (description == null) {
                    text = Messages.get("imobject.name", name);
                } else if (name == null) {
                    text = Messages.get("imobject.description", description);
                } else {
                    text = Messages.get("imobject.summary", name, description);
                }

                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        HistoryModel.this.party = party;
                        browser.setSelected(pair);
                        browser.notifySelected(pair);
                    }
                };
                IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(party.getObjectReference(), text,
                                                                             listener);
                return viewer.getComponent();
            }
            return null;
        }
    }

}
