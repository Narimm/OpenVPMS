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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.IMTableModel;


/**
 * Patient browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientBrowser extends IMObjectTableBrowser<Party> {

    /**
     * Construct a new <code>PatientBrowser</code> that queries IMObjects
     * using the specified query, displaying them in the table.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     */
    public PatientBrowser(Query<Party> query, SortConstraint[] sort) {
        super(query, sort, new PatientTableModel(false));
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void query() {
        IMTableModel tableModel = getTableModel();
        PatientTableModel model = (PatientTableModel) tableModel;
        PatientQuery query = (PatientQuery) getQuery();
        model.setShowOwner(query.isAllPatientsSelected());
        super.query();
    }

}
