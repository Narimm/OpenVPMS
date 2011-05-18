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

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.event.ActionListener;
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
        super(query, newTableModel());
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
        // maxDepth = 2 - display the events, and their immediate children 
        pagedModel = new PagedActHierarchyTableModel<Act>(
                (IMObjectTableModel<Act>) model, 2, query.getActItemShortNames());
        PagedIMTable<Act> result = super.createTable(pagedModel);
        IMTable<Act> table = result.getTable();
        table.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected();
            }
        });
        table.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
        table.setHeaderVisible(false);
        table.setStyleName("MedicalRecordSummary");
        // table.setRolloverEnabled(false); // TODO - ideally set this in style, but it gets overridden by the model
        return result;
    }

    /**
     * Creates a new table model.
     *
     * @return a new table model
     */
    private static IMObjectTableModel<Act> newTableModel() {
        return IMObjectTableModelFactory.create(SummaryTableModel.class, null);
    }

    /**
     * Invoked when an act is selected. Highlights the associated visit.
     */
    private void onSelected() {
        IMTable<Act> table = getTable().getTable();
        int index = table.getSelectionModel().getMinSelectedIndex();
        while (index >= 0) {
            Act act = table.getObjects().get(index);
            if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) {
                break;
            } else {
                --index;
            }
        }
        SummaryTableModel model = (SummaryTableModel) pagedModel.getModel();
        model.setSelectedVisit(index);
    }
}
