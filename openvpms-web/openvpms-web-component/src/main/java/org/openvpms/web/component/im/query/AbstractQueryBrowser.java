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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

import java.util.List;


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
        this.query.addQueryListener(this::onQuery);
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
            set = new EmptyResultSet<>(getQuery().getMaxResults());
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
     * @return the browser state, or {@code null} if this browser doesn't support it
     */
    public BrowserState getBrowserState() {
        Memento<T> result = new Memento<>(this);
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
        int page = memento.getPage();
        if (page != -1) {
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

            // set the page position, or as close to it as possible
            while (page >= 0) {
                if (model.setPage(page)) {
                    int row = memento.getSelectedRow();
                    if (row >= 0) {
                        IMTable<T> table = pagedTable.getTable();
                        List<T> objects = table.getObjects();
                        if (page != memento.getPage()) {
                            row = objects.size() - 1;
                        } else if (row >= objects.size()) {
                            row = objects.size() - 1;
                        }
                        if (row >= 0 && row < objects.size()) {
                            table.getSelectionModel().setSelectedIndex(row, true);
                        }
                    }
                    break;
                } else {
                    --page;
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
        if (container instanceof SplitPane) {
            Extent height = query.getHeight();
            if (height != null) {
                ((SplitPane) container).setSeparatorPosition(height);
            }
        }
        container.add(ColumnFactory.create(Styles.INSET, row));
    }

    /**
     * Lays out the query component.
     *
     * @return the query component
     */
    protected Component doQueryLayout() {
        // query component
        Component component = query.getComponent();

        FocusGroup group = getFocusGroup();
        group.add(query.getFocusGroup());

        ButtonRow row = new ButtonRow(group);
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
            result = new EmptyResultSet<>(query.getMaxResults());
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
         * The query state. May be {@code null}
         */
        private final QueryState queryState;

        /**
         * The selected row or {@code -1} if no row is selected
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
         * Constructs a {@code Memento}.
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
         * @return the query state, or {@code null} if the query doesn't support it
         */
        public QueryState getQueryState() {
            return queryState;
        }

        /**
         * Returns the selected page.
         *
         * @return the selected page, or {@code -1} if no page is selected
         */
        public int getPage() {
            return page;
        }

        /**
         * Returns the selected row.
         *
         * @return the selected row, or {@code -1} if no row is selected
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
         * @return {@code true} if the column is sorted ascending; {@code false} if it is sorted descending
         */
        public boolean isSortedAscending() {
            return sortAscending;
        }

        /**
         * Determines if this state is supported by the specified browser.
         *
         * @param browser the browser
         * @return {@code true} if the state is supported by the browser; otherwise {@code false}
         */
        public boolean supports(Browser browser) {
            return browser instanceof AbstractQueryBrowser;
        }

        /**
         * Determines if this state is supports the specified archetypes and type.
         *
         * @param shortNames the archetype short names
         * @param type       the type returned by the underlying query
         * @return {@code true} if the state supports the specified archetypes and type
         */
        public boolean supports(String[] shortNames, Class type) {
            return queryState != null && queryState.supports(type, shortNames);
        }
    }
}
