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
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryTableCellRenderer;

import java.util.List;


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
            ProblemQuery query = getQuery();
            pagedModel.setShortNames(query.getActItemShortNames());
            pagedModel.setSortAscending(query.isSortAscending());
        }
        super.query();
    }

    /**
     * Returns the event associated with the supplied act.
     *
     * @param act the act. May be {@code null}
     * @return the event, or {@code null} if none is found
     */
    @Override
    public Act getEvent(Act act) {
        Act result = null;
        if (act != null) {
            if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_PROBLEM)) {
                // scan forward through the acts to find the event. It should be the first in the list, unless
                // problem items have failed to be linked to an event
                List<Act> acts = getObjects();
                int index = acts.indexOf(act);
                if (index >= 0) {
                    index++;
                    for (; index < acts.size(); index++) {
                        Act a = acts.get(index);
                        if (TypeHelper.isA(a, PatientArchetypes.CLINICAL_EVENT)) {
                            result = a;
                            break;
                        } else if (TypeHelper.isA(a, PatientArchetypes.CLINICAL_PROBLEM)) {
                            // next problem, so problem not linked to an event
                            break;
                        }
                    }
                }
            } else if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) {
                result = act;
            } else {
                ActBean bean = new ActBean(act);
                if (bean.getNodeSourceObjectRef("event") != null) {
                    result = getTableModel().getParent(act, PatientArchetypes.CLINICAL_EVENT);
                }
            }
        }
        return result;
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    @Override
    public ProblemQuery getQuery() {
        return (ProblemQuery) super.getQuery();
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<Act> createTable(IMTableModel<Act> model) {
        ProblemQuery query = getQuery();
        pagedModel = new PagedProblemTableModel(model, query, getContext().getContext());
        pagedModel.setSortAscending(query.isSortAscending());
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

    /**
     * Determines the page that an object appears on.
     *
     * @param object the object
     * @return the page
     */
    protected int getPage(Act object) {
        return getQuery().getPage(object);
    }

    private class PagedProblemTableModel extends PagedActHierarchyTableModel<Act> {

        /**
         * Constructs a {@link PagedProblemTableModel}.
         *
         * @param model   the underlying table model
         * @param query   the problem query
         * @param context the context
         */
        public PagedProblemTableModel(IMTableModel<Act> model, ProblemQuery query, Context context) {
            super((IMObjectTableModel<Act>) model, context, query.getActItemShortNames());
        }

        /**
         * Creates an iterator over the act hierarchy.
         *
         * @param objects    the objects to iterate over
         * @param shortNames the child archetype short names to include in the iteration
         * @return a new iterator
         */
        @Override
        protected ActHierarchyIterator<Act> createIterator(List<Act> objects, String[] shortNames) {
            ProblemFilter filter = new ProblemFilter(shortNames, isSortAscending());
            return new ProblemHierarchyIterator(objects, filter);
        }

    }
}
