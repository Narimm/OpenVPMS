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

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;


/**
 * Patient medical record browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryTableBrowser extends IMObjectTableBrowser<Act> {

    /**
     * The table model that wraps the underlying model, to filter acts.
     */
    private PagedActHierarchyTableModel<Act> pagedModel;


    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     */
    public SummaryTableBrowser(PatientSummaryQuery query) {
        super(query, createTableModel());
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        if (pagedModel != null) {
            // ensure the table model has the selected child act short names
            // prior to performing the query
            PatientSummaryQuery query = (PatientSummaryQuery) getQuery();
            pagedModel.setShortNames(query.getActItemShortNames());
        }
        super.query();
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<Act> createTable(IMTableModel<Act> model) {
        PatientSummaryQuery query = (PatientSummaryQuery) getQuery();
        pagedModel = new PagedActHierarchyTableModel<Act>(
                (IMObjectTableModel<Act>) model, query.getActItemShortNames());
        PagedIMTable<Act> result = super.createTable(pagedModel);
        IMTable<Act> table = result.getTable();
        table.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
        table.setHeaderVisible(false);
        table.setStyleName("plain");
        return result;
    }

    private static IMObjectTableModel<Act> createTableModel() {
        return IMObjectTableModelFactory.create(SummaryTableModel.class, null);
    }
}
