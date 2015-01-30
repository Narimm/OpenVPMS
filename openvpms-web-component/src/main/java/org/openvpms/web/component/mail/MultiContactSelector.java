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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.MultiIMObjectSelector;
import org.openvpms.web.component.im.select.SelectedObjects;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Multiple contact selector.
 *
 * @author Tim Anderson
 */
class MultiContactSelector extends MultiIMObjectSelector<Contact> {

    /**
     * The address formatter.
     */
    private final AddressFormatter formatter;

    /**
     * Constructs a {@link MultiContactSelector}.
     *
     * @param formatter the address formatter
     * @param context   the layout context
     */
    public MultiContactSelector(AddressFormatter formatter, LayoutContext context) {
        super(DescriptorHelper.getDisplayName(ContactArchetypes.EMAIL), new Contacts(formatter), false, context);
        this.formatter = formatter;
    }

    /**
     * Creates a query to select objects.
     *
     * @param value a value to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Contact> createQuery(String value) {
        EmailQuery query = new EmailQuery();
        query.setValue(value);
        return query;
    }

    /**
     * Creates a new browser.
     *
     * @param query   the query
     * @param context the layout context
     * @return a return a new browser
     */
    @Override
    protected Browser<Contact> createBrowser(final Query<Contact> query, LayoutContext context) {
        return new DefaultIMObjectTableBrowser<Contact>(query, context) {
            @Override
            protected IMTableModel<Contact> createTableModel(LayoutContext context) {
                return new EmailTableModel(query, formatter);
            }
        };
    }

    /**
     * Creates a browser dialog.
     *
     * @param browser the browser
     * @param context the layout context
     * @return a new browser dialog
     */
    @Override
    protected BrowserDialog<Contact> createBrowserDialog(Browser<Contact> browser, LayoutContext context) {
        BrowserDialog<Contact> dialog = new BrowserDialog<Contact>(Messages.get("mail.addressbook"), browser, false,
                                                                   context.getHelpContext());
        dialog.setStyleName("AddressBook");
        return dialog;
    }

    /**
     * Queries the supplied text.
     * <p/>
     * If there is a single match, the selected object is updated at the specified index.
     * If there are no matches, or multiple matches, then a browser is displayed.
     *
     * @param text  the text to query
     * @param index the index to store the selection
     * @return {@code true} if querying is complete, {@code false} if a browser was displayed
     */
    @Override
    protected boolean query(String text, int index) {
        int less = text.indexOf('<');
        if (less != -1) {
            text = text.substring(0, less);
            text = text.trim();
        }
        if (text.indexOf('@') != -1) {
            // have an email address. Just create a dummy contact
            Contact contact = (Contact) ServiceHelper.getArchetypeService().create(ContactArchetypes.EMAIL);
            IMObjectBean bean = new IMObjectBean(contact);
            bean.setValue("emailAddress", text);
            setObject(index, contact);
        }
        return super.query(text, index);
    }

    private static class Contacts extends SelectedObjects<Contact> {

        private final AddressFormatter formatter;

        public Contacts(AddressFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        protected String getName(Contact object) {
            return formatter.getNameAddress(object);
        }

        /**
         * Determines if a name matches an object.
         *
         * @param name       the name
         * @param object     the object
         * @param ignoreCase if {@code true}, ignore case
         * @return {@code true} if they match
         */
        @Override
        protected boolean matches(String name, Contact object, boolean ignoreCase) {
            String formatted = formatter.format(object);
            return ignoreCase ? StringUtils.equalsIgnoreCase(name, formatted) : StringUtils.equals(name, formatted);
        }
    }

    private static class EmailTableModel extends AbstractIMTableModel<Contact> {

        /**
         * The address formatter.
         */
        private final AddressFormatter formatter;

        /**
         * The name column index.
         */
        private static final int NAME_INDEX = 0;

        /**
         * The email column index.
         */
        private static final int EMAIL_INDEX = 1;

        /**
         * The archetype column index.
         */
        private static final int TYPE_INDEX = 2;

        /**
         * The active column index.
         */
        private static final int ACTIVE_INDEX = 3;

        public EmailTableModel(Query<Contact> query, AddressFormatter formatter) {
            this.formatter = formatter;
            boolean active = query.getActive() == BaseArchetypeConstraint.State.BOTH;
            DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
            columnModel.addColumn(createTableColumn(NAME_INDEX, NAME));
            columnModel.addColumn(createTableColumn(EMAIL_INDEX, "mail.email"));
            columnModel.addColumn(createTableColumn(TYPE_INDEX, "mail.type"));
            if (active) {
                columnModel.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
            }
            setTableColumnModel(columnModel);
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if <tt>true</tt> sort in ascending order; otherwise
         *                  sort in <tt>descending</tt> order
         * @return the sort criteria, or <tt>null</tt> if the column isn't
         *         sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            if (column == NAME_INDEX) {
                return new SortConstraint[]{sort("party", "name", ascending), sort("party", "id")};
            }
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Contact object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case NAME_INDEX:
                    result = formatter.getName(object);
                    if (object == null) {
                        result = formatter.getAddress(object);
                    }
                    break;
                case EMAIL_INDEX:
                    result = formatter.getAddress(object);
                    break;
                case TYPE_INDEX:
                    result = formatter.getType(object);
                    break;
                case ACTIVE_INDEX:
                    if (object.getParty() != null) {
                        result = getActive(object.getParty());
                    }
                    break;
            }

            return result;
        }
    }

}
