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
 */

package org.openvpms.web.app.patient.history;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.event.ActionListener;

import java.util.List;


/**
 * Patient history record browser.
 *
 * @author Tim Anderson
 */
public class PatientHistoryBrowser extends IMObjectTableBrowser<Act> {

    /**
     * The table model that wraps the underlying model, to filter acts.
     */
    private PagedPatientHistoryTableModel pagedModel;


    /**
     * Constructs a {@code Browser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public PatientHistoryBrowser(PatientHistoryQuery query, LayoutContext context) {
        super(query, newTableModel(context), context);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        if (pagedModel != null) {
            // ensure the table model has the selected child act short names
            // prior to performing the query
            PatientHistoryQuery query = (PatientHistoryQuery) getQuery();
            pagedModel.setShortNames(query.getActItemShortNames());
            pagedModel.setSortAscending(query.isSortAscending());
        }
        super.query();
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    @Override
    public void setSelected(Act object) {
        super.setSelected(object);
        onSelected();
    }

    /**
     * Returns the <em>act.patientClinicalEvent</em> associated with the selected act.
     *
     * @return the event, or {@code null} if none is found
     */
    public Act getEvent() {
        return getEvent(getSelected());
    }

    /**
     * Returns the <em>act.patientClinicalEvent</em> associated with the supplied act.
     *
     * @param act the act. May be {@code null}
     * @return the event, or {@code null} if none is found
     */
    public Act getEvent(Act act) {
        boolean found = false;
        if (act != null) {
            List<Act> acts = getObjects();
            int index = acts.indexOf(act);
            while (!(found = TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) && index > 0) {
                act = acts.get(--index);
            }
        }
        return (found) ? act : null;
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<Act> createTable(IMTableModel<Act> model) {
        PatientHistoryQuery query = (PatientHistoryQuery) getQuery();
        pagedModel = new PagedPatientHistoryTableModel((IMObjectTableModel<Act>) model, getContext().getContext(),
                                                       query.getActItemShortNames());
        pagedModel.setSortAscending(query.isSortAscending());
        PagedIMTable<Act> result = super.createTable(pagedModel);
        IMTable<Act> table = result.getTable();
        table.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected();
            }
        });
        table.setDefaultRenderer(Object.class, new PatientHistoryTableCellRenderer());
        table.setHeaderVisible(false);
        table.setStyleName("MedicalRecordSummary");
        // table.setRolloverEnabled(false); // TODO - ideally set this in style, but it gets overridden by the model
        return result;
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    private static IMObjectTableModel<Act> newTableModel(LayoutContext context) {
        return IMObjectTableModelFactory.create(PatientHistoryTableModel.class, context);
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
        PatientHistoryTableModel model = (PatientHistoryTableModel) pagedModel.getModel();
        model.setSelectedVisit(index);
    }
}
