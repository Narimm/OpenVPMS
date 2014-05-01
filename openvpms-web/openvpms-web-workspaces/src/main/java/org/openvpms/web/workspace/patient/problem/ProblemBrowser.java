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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryTableCellRenderer;


/**
 * Patient problem record browser.
 *
 * @author Tim Anderson
 */
public class ProblemBrowser extends AbstractPatientHistoryBrowser {

    /**
     * The table model that wraps the underlying model, to filter acts.
     */
    private PagedActHierarchyTableModel<Act> pagedModel;

    /**
     * The cell renderer.
     */
    private static final TableCellRenderer RENDERER = new PatientHistoryTableCellRenderer("ProblemSummary");

    /**
     * Constructs a {@link ProblemBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public ProblemBrowser(ProblemQuery query, LayoutContext context) {
        this(query, (ProblemTableModel) IMObjectTableModelFactory.create(ProblemTableModel.class, context), context);
    }

    /**
     * Constructs a {@link ProblemBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param model   the table model
     * @param context the layout context
     */
    public ProblemBrowser(ProblemQuery query, ProblemTableModel model, LayoutContext context) {
        super(query, model, context);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        if (pagedModel != null) {
            // ensure the table model has the selected child act short names prior to performing the query
            ProblemQuery query = (ProblemQuery) getQuery();
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
        ProblemQuery query = (ProblemQuery) getQuery();
        pagedModel = new PagedActHierarchyTableModel<Act>((IMObjectTableModel<Act>) model, getContext().getContext(),
                                                          query.getActItemShortNames());
        PagedIMTable<Act> result = super.createTable(pagedModel);
        initTable(result);
        return result;
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
}
