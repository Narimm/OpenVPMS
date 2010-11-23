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
 *
 *  $Id$
 */

package org.openvpms.web.app.patient;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserAdapter;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.PatientResultSet;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.TableBrowser;


/**
 * Patient browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientBrowser extends BrowserAdapter<ObjectSet, Party> {

    /**
     * Constructs a <tt>PatientBrowser</tt>.
     *
     * @param query the query
     */
    public PatientBrowser(PatientQuery query) {
        setBrowser(createBrowser(query));
    }

    /**
     * Returns the customer associated with the selected patient.
     *
     * @return the customer, or <tt>null</tt> if no patient is selected or has no current owner
     */
    public Party getCustomer() {
        Party result = null;
        Party patient = getSelected();
        if (patient != null) {
            PatientRules rules = new PatientRules();
            result = rules.getOwner(patient);
        }
        return result;
    }

    /**
     * Converts an object.
     *
     * @param set the object to convert
     * @return the converted object
     */
    protected Party convert(ObjectSet set) {
        return (Party) set.get("patient");
    }

    /**
     * Creates a table browser that changes the model depending on what
     * columns have been queried on.
     *
     * @param query the query
     * @return a new browser
     */
    private static Browser<ObjectSet> createBrowser(final PatientQuery query) {
        final PatientTableModel model = new PatientTableModel();
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
                if (result instanceof PatientResultSet) {
                    PatientResultSet set = (PatientResultSet) result;
                    model.showColumns(set.isSearchingAllPatients(), set.isSearchingIdentities());
                }
                return result;
            }
        };
    }

}
