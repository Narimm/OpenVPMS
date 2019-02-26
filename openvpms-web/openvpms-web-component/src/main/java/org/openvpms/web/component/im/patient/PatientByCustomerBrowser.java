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

package org.openvpms.web.component.im.patient;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractQueryBrowser;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserAdapter;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.system.ServiceHelper;


/**
 * Patient browser that supports querying by customer and contact.
 *
 * @author Tim Anderson
 */
public class PatientByCustomerBrowser extends BrowserAdapter<ObjectSet, Party> {

    /**
     * Constructs a {@link PatientByCustomerBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public PatientByCustomerBrowser(PatientByCustomerQuery query, LayoutContext context) {
        setBrowser(createBrowser(query, context));
    }

    /**
     * Returns the current customer associated with the selected patient.
     *
     * @return the customer, or {@code null} if no patient is selected or has no current owner
     */
    public Party getCustomer() {
        Party result = null;
        Party patient = getSelected();
        if (patient != null) {
            PatientRules rules = ServiceHelper.getBean(PatientRules.class);
            result = (Party) rules.getOwner(patient);
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
     * @param query   the query
     * @param context the layout context
     * @return a new browser
     */
    private static Browser<ObjectSet> createBrowser(PatientByCustomerQuery query, LayoutContext context) {
        PatientByCustomerTableModel model = new PatientByCustomerTableModel();
        Query<ObjectSet> delegate = query.getQuery();
        return new AbstractQueryBrowser<ObjectSet>(delegate, delegate.getDefaultSortConstraint(), model, context) {
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
                    boolean active = query.getActive() == BaseArchetypeConstraint.State.BOTH;
                    model.showColumns(set.isSearchingAllPatients(), true, set.isSearchingByContact(),
                                      set.isSearchingIdentities(), active);
                }
                return result;
            }
        };
    }

}
