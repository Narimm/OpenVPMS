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
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTable;


/**
 * Patient medical record browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryTableBrowser extends TableBrowser<Act> {

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     */
    public SummaryTableBrowser(Query<Act> query) {
        super(query, null, new SummaryTableModel());
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMObjectTable<Act> createTable(
            IMObjectTableModel<Act> model) {
        PagedIMObjectTable<Act> result = new PagedIMObjectTable<Act>(
                new PagedSummaryTableModel(model));
        IMObjectTable<Act> table = result.getTable();
        table.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
        table.setHeaderVisible(false);
        table.setStyleName("plain");
        table.setSelectionEnabled(false);
        return result;
    }
}
