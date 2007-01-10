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

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;


/**
 * Appointment browser. Renders blocks of appointments in different hours a
 * different colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentBrowser extends TableBrowser<ObjectSet> {

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     * @param model the table model
     */
    public AppointmentBrowser(Query<ObjectSet> query,
                              IMTableModel<ObjectSet> model) {
        super(query, null, model);
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<ObjectSet> createTable(
            IMTableModel<ObjectSet> model) {
        PagedIMTable<ObjectSet> result = new PagedIMTable<ObjectSet>(model);
        IMTable<ObjectSet> table = result.getTable();
        table.setDefaultRenderer(Object.class,
                                 new AppointmentTableCellRenderer());
        return result;
    }
}
