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
 */

package org.openvpms.web.component.im.customer;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractBrowserState;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserState;
import org.openvpms.web.component.im.query.CustomerQuery;
import org.openvpms.web.component.im.query.CustomerResultSet;
import org.openvpms.web.component.im.query.CustomerResultSetAdapter;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserAdapter;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.TableBrowser;


/**
 * Customer browser.
 *
 * @author Tim Anderson
 */
public class CustomerBrowser extends QueryBrowserAdapter<ObjectSet, Party> {

    /**
     * The query.
     */
    private final CustomerQuery query;

    /**
     * Constructs a {@code CustomerBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public CustomerBrowser(CustomerQuery query, LayoutContext context) {
        this.query = query;
        setBrowser(createBrowser(query, context));
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    public Query<Party> getQuery() {
        return query;
    }

    /**
     * Returns the selected patient.
     *
     * @return the patient, or {@code null} if no patient is selected
     */
    public Party getPatient() {
        Party result = null;
        ObjectSet set = getBrowser().getSelected();
        if (set != null && set.exists("patient")) {
            result = (Party) set.get("patient");
        }
        return result;
    }

    /**
     * Returns the result set.
     * <p/>
     * Note that this is a snapshot of the browser's result set. Iterating over it will not affect the browser.
     *
     * @return the result set, or {@code null} if the query hasn't been executed
     */
    public ResultSet<Party> getResultSet() {
        return new CustomerResultSetAdapter((CustomerResultSet) getBrowser().getResultSet());
    }

    /**
     * Returns the browser state.
     *
     * @return the browser state
     */
    @Override
    public BrowserState getBrowserState() {
        return new Memento(this);
    }

    /**
     * Sets the browser state.
     *
     * @param state the state
     */
    @Override
    public void setBrowserState(BrowserState state) {
        Memento memento = (Memento) state;
        if (memento.getBrowserState() != null) {
            getBrowser().setBrowserState(memento.getBrowserState());
        }
    }

    /**
     * Converts an object.
     *
     * @param set the object to convert
     * @return the converted object
     */
    protected Party convert(ObjectSet set) {
        return (Party) set.get("customer");
    }

    /**
     * Creates a table browser that changes the model depending on what columns have been queried on.
     *
     * @param query   the query
     * @param context the layout context
     * @return a new browser
     */
    private static Browser<ObjectSet> createBrowser(final CustomerQuery query, LayoutContext context) {
        final CustomerTableModel model = new CustomerTableModel();
        Query<ObjectSet> delegate = query.getQuery();
        return new TableBrowser<ObjectSet>(delegate, delegate.getDefaultSortConstraint(), model, context) {
            /**
             * Performs the query.
             *
             * @return the query result set
             */
            @Override
            protected ResultSet<ObjectSet> doQuery() {
                ResultSet<ObjectSet> result = super.doQuery();
                if (result instanceof CustomerResultSet) {
                    CustomerResultSet set = (CustomerResultSet) result;
                    model.showColumns(set.isSearchingOnPatient(), set.isSearchingOnContact(),
                                      set.isSearchingIdentities());
                }
                return result;
            }
        };
    }

    private static class Memento extends AbstractBrowserState {

        /**
         * The underlying browser's state.
         */
        private final BrowserState browserState;

        /**
         * Constructs a {@code Memento}.
         *
         * @param browser the customer browser
         */
        public Memento(CustomerBrowser browser) {
            super(browser.getQuery());
            browserState = browser.getBrowser().getBrowserState();
        }

        /**
         * Returns the underlying browser's state.
         *
         * @return the underlying browser's state. May be {@code null}
         */
        public BrowserState getBrowserState() {
            return browserState;
        }

        /**
         * Determines if this state is supported by the specified browser.
         *
         * @param browser the browser
         * @return {@code true} if the state is supported by the browser; otherwise {@code false}
         */
        public boolean supports(Browser browser) {
            return browser instanceof CustomerBrowser;
        }

        /**
         * Determines if this state is supports the specified archetypes and type.
         *
         * @param shortNames the archetype short names
         * @param type       the type returned by the underlying query
         * @return {@code true} if the state supports the specified archetypes and type
         */
        @Override
        public boolean supports(String[] shortNames, Class type) {
            return type.equals(Party.class) && getQueryState().supports(shortNames);
        }
    }

}
