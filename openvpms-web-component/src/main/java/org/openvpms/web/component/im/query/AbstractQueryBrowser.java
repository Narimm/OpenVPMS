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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.event.ActionListener;


/**
 * Abstract implementation of the {@link QueryBrowser} interface where the objects being browsed are provided by an
 * {@link Query}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractQueryBrowser<T> extends AbstractTableBrowser<T> implements QueryBrowser<T> {

    /**
     * The query object.
     */
    private final Query<T> query;

    /**
     * The sort criteria. If {@code null}, the query's default sort criteria
     * is used.
     */
    private SortConstraint[] sort;

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";


    /**
     * Constructs an {@link AbstractQueryBrowser}.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be {@code null}
     * @param model   the table model. If {@code null}, one will be created on each query
     * @param context the layout context
     */
    public AbstractQueryBrowser(Query<T> query, SortConstraint[] sort, IMTableModel<T> model, LayoutContext context) {
        super(model, context);
        this.query = query;
        this.sort = sort;
        this.query.addQueryListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
        getFocusGroup().add(query.getFocusGroup());
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    public Query<T> getQuery() {
        return query;
    }

    /**
     * Query using the specified criteria, and populate the browser with
     * matches.
     */
    @Override
    public void query() {
        Component component = getComponent();

        ResultSet<T> set = doQuery();
        boolean hasResults = (set != null && hasResults(set));
        doLayout(component, hasResults);

        if (set == null) {
            set = new EmptyResultSet<T>(getQuery().getMaxResults());
        }
        PagedIMTable<T> table = getTable();
        table.setResultSet(set);
        setFocusOnResults();
    }

    /**
     * Returns the result set.
     *
     * @return the result set
     */
    public ResultSet<T> getResultSet() {
        ResultSet<T> set = getTable().getResultSet();
        try {
            if (set != null) {
                return set.clone();
            }
        } catch (CloneNotSupportedException exception) {
            throw new IllegalStateException(exception);
        }
        return null;
    }


    /**
     * Returns the browser state.
     *
     * @return the browser state, or <tt>null</tt> if this browser doesn't support it
     */
    public BrowserState getBrowserState() {
        Memento<T> result = new Memento<T>(this);
        return (result.getQueryState() != null) ? result : null;
    }

    /**
     * Sets the browser state.
     *
     * @param state the state
     */
    @SuppressWarnings("unchecked")
    public void setBrowserState(BrowserState state) {
        Memento<T> memento = (Memento<T>) state;
        if (memento.getQueryState() != null) {
            getQuery().setQueryState(memento.getQueryState());
        }
        if (memento.getPage() != -1) {
            query();

            // TODO - not ideal. Need to query first before any sorting or page setting can take place.
            // This results in redundant queries.
            PagedIMTable<T> pagedTable = getTable();
            PagedIMTableModel<T, T> model = pagedTable.getModel();
            int sortColumn = memento.getSortColumn();
            boolean ascending = memento.isSortedAscending();
            if (sortColumn != model.getSortColumn() || ascending != model.isSortedAscending()) {
                model.sort(sortColumn, ascending);
            }

            if (model.setPage(memento.page)) {
                int row = memento.getSelectedRow();
                if (row != -1) {
                    pagedTable.getTable().getSelectionModel().setSelectedIndex(row, true);
                }
            }
        }
    }

    /**
     * Sets the sort criteria.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    protected void setSortConstraint(SortConstraint[] sort) {
        this.sort = sort;
    }

    /**
     * Lay out this component.
     */
    @Override
    protected void doLayout() {
        super.doLayout();
        if (query.isAuto()) {
            query();
        }
    }

    /**
     * Lays out this component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Component row = doQueryLayout();
        container.add(row);
    }

    /**
     * Lays out the query component.
     *
     * @return the query component
     */
    protected Component doQueryLayout() {
        // query component
        Component component = query.getComponent();

        ButtonRow row = new ButtonRow(getFocusGroup());
        row.add(component);
        row.addButton(QUERY_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return row;
    }

    /**
     * Performs the query.
     *
     * @return the query result set
     */
    protected ResultSet<T> doQuery() {
        ResultSet<T> result = null;
        try {
            result = (sort != null) ? query.query(sort) : query.query();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        if (result == null) {
            result = new EmptyResultSet<T>(query.getMaxResults());
        }
        return result;
    }

    /**
     * Initialises the table model.
     * <p/>
     * This implementation updates the browser's sort criteria so that it is preserved across queries.
     *
     * @param model the model
     */
    @Override
    protected void initTableModel(IMTableModel<T> model) {
        super.initTableModel(model);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                PagedIMTable<T> table = getTable();
                if (table != null) {
                    ResultSet<T> set = table.getResultSet();
                    if (set != null) {
                        setSortConstraint(set.getSortConstraints());
                    }
                }
            }
        });
    }

    /**
     * Invoked when the query button is pressed. Performs the query and notifies
     * any listeners.
     */
    private void onQuery() {
        query();
        notifyBrowserListeners();
    }

    protected static class Memento<T> implements BrowserState {

        /**
         * The query state. May be <tt>null</tt>
         */
        private final QueryState queryState;

        /**
         * The selected row or <tt>-1</tt> if no row is selected
         */
        private final int selectedRow;

        /**
         * The selected page.
         */
        private final int page;

        /**
         * The sort column.
         */
        private int sortColumn;

        /**
         * Determines if the column is sorted ascending or descending.
         */
        private boolean sortAscending;


        /**
         * Constructs a <tt>Memento</tt>.
         *
         * @param browser the browser
         */
        public Memento(AbstractQueryBrowser<T> browser) {
            queryState = browser.getQuery().getQueryState();
            PagedIMTable<T> table = browser.getTable();
            selectedRow = table.getTable().getSelectionModel().getMinSelectedIndex();
            sortColumn = table.getModel().getSortColumn();
            ResultSet<T> set = table.getResultSet();
            if (set != null) {
                page = set.lastIndex();
                sortAscending = table.getModel().isSortedAscending();
            } else {
                page = -1;
                sortAscending = true;
            }
        }

        /**
         * Returns the query state.
         *
         * @return the query state, or <tt>null</tt> if the query doesn't support it
         */
        public QueryState getQueryState() {
            return queryState;
        }

        /**
         * Returns the selected page.
         *
         * @return the selected page, or <tt>-1</tt> if no page is selected
         */
        public int getPage() {
            return page;
        }

        /**
         * Returns the selected row.
         *
         * @return the selected row, or <tt>-1</tt> if no row is selected
         */
        public int getSelectedRow() {
            return selectedRow;
        }

        /**
         * Returns the sort column.
         *
         * @return the sort column, or <code>-1</code> if no column is sorted.
         */
        public int getSortColumn() {
            return sortColumn;
        }

        /**
         * Determines if the sort column is sorted ascending or descending.
         *
         * @return <tt>true</tt> if the column is sorted ascending;
         *         <tt>false</tt> if it is sorted descending
         */
        public boolean isSortedAscending() {
            return sortAscending;
        }

        /**
         * Determines if this state is supported by the specified browser.
         *
         * @param browser the browser
         * @return <tt>true</tt> if the state is supported by the browser; otherwise <tt>false</tt>
         */
        public boolean supports(Browser browser) {
            return browser instanceof AbstractQueryBrowser;
        }

        /**
         * Determines if this state is supports the specified archetypes and type.
         *
         * @param shortNames the archetype short names
         * @param type       the type returned by the underlying query
         * @return <tt>true</tt> if the state supports the specified archetypes and type
         */
        public boolean supports(String[] shortNames, Class type) {
            return queryState != null && queryState.supports(type, shortNames);
        }
    }
}
