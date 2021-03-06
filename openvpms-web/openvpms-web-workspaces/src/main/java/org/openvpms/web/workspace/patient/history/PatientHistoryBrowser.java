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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;


/**
 * Patient history record browser.
 *
 * @author Tim Anderson
 */
public class PatientHistoryBrowser extends AbstractPatientHistoryBrowser {

    /**
     * The table model that wraps the underlying model, to filter acts.
     */
    private PagedPatientHistoryTableModel pagedModel;

    /**
     * The cell renderer.
     */
    private static final TableCellRenderer RENDERER = new PatientHistoryTableCellRenderer("MedicalRecordSummary");

    /**
     * Constructs a {@link PatientHistoryBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public PatientHistoryBrowser(PatientHistoryQuery query, LayoutContext context) {
        this(query, newTableModel(context), context);
    }

    /**
     * Constructs a {@link PatientHistoryBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param model   the table model
     * @param context the layout context
     */
    public PatientHistoryBrowser(PatientHistoryQuery query, PatientHistoryTableModel model, LayoutContext context) {
        super(query, model, context);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        if (pagedModel != null) {
            // ensure the table model has the selected child act short names prior to performing the query
            PatientHistoryQuery query = getQuery();
            pagedModel.setShortNames(query.getSelectedItemShortNames());
            pagedModel.setTextSearch(query.getValue());
            pagedModel.setProductTypes(query.getProductTypes());
            pagedModel.setSortAscending(query.isSortAscending());
        }
        super.query();
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    @Override
    public PatientHistoryQuery getQuery() {
        return (PatientHistoryQuery) super.getQuery();
    }

    /**
     * Returns the event associated with the supplied act.
     *
     * @param act the act. May be {@code null}
     * @return the event, or {@code null} if none is found
     */
    @Override
    public Act getEvent(Act act) {
        return getTableModel().getParent(act);
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<Act> createTable(IMTableModel<Act> model) {
        PatientHistoryQuery query = getQuery();
        pagedModel = createPagedModel((IMObjectTableModel<Act>) model, query);
        pagedModel.setSortAscending(query.isSortAscending());
        return super.createTable(pagedModel);
    }

    /**
     * Creates a new paged history table model.
     *
     * @param model the underlying table model
     * @param query the history query
     * @return a new paged table model
     */
    protected PagedPatientHistoryTableModel createPagedModel(IMObjectTableModel<Act> model, PatientHistoryQuery query) {
        return new PagedPatientHistoryTableModel((PatientHistoryTableModel) model, query.getSelectedItemShortNames());
    }

    /**
     * Initialises a paged table.
     *
     * @param table the table
     */
    @Override
    protected void initTable(PagedIMTable<Act> table) {
        super.initTable(table);
        table.getTable().setDefaultRenderer(Object.class, RENDERER);
    }

    /**
     * Determines the page that an object appears on.
     *
     * @param object the object
     * @return the page
     */
    protected int getPage(Act object) {
        return getQuery().getPage(object);
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    private static PatientHistoryTableModel newTableModel(LayoutContext context) {
        IMObjectTableModel model = IMObjectTableModelFactory.create(PatientHistoryTableModel.class, context);
        return (PatientHistoryTableModel) model;
    }

}
