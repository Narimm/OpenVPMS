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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.WorkflowQuery;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;

import java.text.DateFormat;


/**
 * Appointment browser. Renders blocks of appointments in different hours a
 * different colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentBrowser extends TableBrowser<ObjectSet> {

    /**
     * Displays the selected date above the appointments.
     */
    private final Label selectedDate;


    /**
     * Construct a new <tt>AppointmentBrowser</tt> that queries ObjectSets using
     * the specified query.
     *
     * @param query the query
     * @param model the table model
     */
    public AppointmentBrowser(WorkflowQuery<ObjectSet> query,
                              IMTableModel<ObjectSet> model) {
        super(query, null, model);
        selectedDate = LabelFactory.create(null, "bold");
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        selectedDate.setLayoutData(layout);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        super.query();
        WorkflowQuery<ObjectSet> query = (WorkflowQuery<ObjectSet>) getQuery();
        DateFormat format = DateHelper.getFullDateFormat();
        selectedDate.setText(format.format(query.getDate()));
    }

    /**
     * Lays out the container to display results.
     *
     * @param container the container
     */
    @Override
    protected void doLayoutForResults(Component container) {
        super.doLayoutForResults(container);
        int index = container.indexOf(getTable());

        // add the label before the table
        container.add(selectedDate, index);
    }

    /**
     * Lays out the container when there are no results to display.
     *
     * @param container the container
     */
    protected void doLayoutForNoResults(Component container) {
        container.add(selectedDate);
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
        PagedIMTable<ObjectSet> result = super.createTable(model);
        IMTable<ObjectSet> table = result.getTable();
        table.setDefaultRenderer(Object.class,
                                 new AppointmentTableCellRenderer());
        return result;
    }
}
