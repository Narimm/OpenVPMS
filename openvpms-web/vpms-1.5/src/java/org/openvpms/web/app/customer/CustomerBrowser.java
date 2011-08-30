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
 *
 *  $Id$
 */

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserAdapter;
import org.openvpms.web.component.im.query.CustomerQuery;
import org.openvpms.web.component.im.query.CustomerResultSet;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.TableBrowser;


/**
 * Customer browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBrowser extends BrowserAdapter<ObjectSet, Party> {

    /**
     * Creates a new <tt>CustomerBrowser</tt>.
     *
     * @param query the query
     */
    public CustomerBrowser(CustomerQuery query) {
        setBrowser(createBrowser(query));
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
     * @param query the query
     * @return a new browser
     */
    private static Browser<ObjectSet> createBrowser(final CustomerQuery query) {
        final CustomerTableModel model = new CustomerTableModel();
        Query<ObjectSet> delegate = query.getQuery();
        return new TableBrowser<ObjectSet>(delegate, delegate.getDefaultSortConstraint(), model) {
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

}
