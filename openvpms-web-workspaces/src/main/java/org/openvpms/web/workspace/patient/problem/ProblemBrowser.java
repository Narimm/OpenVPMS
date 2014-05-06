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
import org.apache.commons.collections4.comparators.ReverseComparator;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryTableCellRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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
     * Comparator to order acts on start time, most recent first.
     */
    private static final ReverseComparator<Act> COMPARATOR = new ReverseComparator<Act>(new Comparator<Act>() {
        @Override
        public int compare(Act o1, Act o2) {
            return DateRules.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime());
        }
    });

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
        pagedModel = new PagedProblemTableModel(model, query, getContext().getContext());
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
         * Flattens an act hierarchy, only including those acts matching the supplied short names.
         *
         * @param objects    the acts
         * @param shortNames the child archetype short names
         * @param context    the context
         * @return the acts
         */
        @Override
        protected List<Act> flattenHierarchy(List<Act> objects, String[] shortNames, Context context) {
            List<Act> acts = new ArrayList<Act>();
            Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
            Map<Act, List<Act>> actsByEvent = new TreeMap<Act, List<Act>>(COMPARATOR);
            List<Act> actsWithoutEvents = new ArrayList<Act>();
            for (Act act : super.flattenHierarchy(objects, shortNames, context)) {
                ActBean bean = new ActBean(act);
                boolean isProblem = bean.isA(PatientArchetypes.CLINICAL_PROBLEM);
                if (isProblem) {
                    addAll(acts, actsWithoutEvents, actsByEvent);
                    acts.add(act);
                }
                Act event = getEvent(bean, events);
                if (event != null) {
                    List<Act> list = actsByEvent.get(event);
                    if (list == null) {
                        list = new ArrayList<Act>();
                        actsByEvent.put(event, list);
                    }
                    if (!isProblem) {
                        list.add(act);
                    }
                } else if (!isProblem) {
                    actsWithoutEvents.add(act);
                }
            }
            addAll(acts, actsWithoutEvents, actsByEvent);
            return acts;
        }

        /**
         * Adds acts to a list. This adds the acts not linked to any events first.
         *
         * @param list              the list to add to
         * @param actsWithoutEvents acts not linked to any events
         * @param actsByEvent       acts linked by event
         */
        private void addAll(List<Act> list, List<Act> actsWithoutEvents, Map<Act, List<Act>> actsByEvent) {
            list.addAll(actsWithoutEvents);
            for (Map.Entry<Act, List<Act>> entry : actsByEvent.entrySet()) {
                list.add(entry.getKey());
                list.addAll(entry.getValue());
            }
            actsWithoutEvents.clear();
            actsByEvent.clear();
        }

        /**
         * Returns the event associated with an act.
         *
         * @param bean   the act bean
         * @param events the event cache
         * @return the corresponding event, or {@code null} if none is found
         */
        private Act getEvent(ActBean bean, Map<IMObjectReference, Act> events) {
            Act event = null;
            if (bean.hasNode("event")) {
                List<IMObjectReference> refs = bean.getNodeSourceObjectRefs("event");
                if (!refs.isEmpty()) {
                    IMObjectReference ref = refs.get(0);
                    event = events.get(ref);
                    if (event == null) {
                        event = (Act) IMObjectHelper.getObject(ref, null);
                        if (event != null) {
                            events.put(ref, event);
                        }
                    }
                }
            }
            return event;
        }

    }
}
