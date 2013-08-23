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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusHelper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of {@link Browser} that renders results in a table.
 * .
 *
 * @author Tim Anderson
 */
public abstract class TableBrowser<T> extends AbstractQueryBrowser<T> {

    /**
     * The paged table.
     */
    private PagedIMTable<T> table;

    /**
     * The model to render results.
     */
    private IMTableModel<T> model;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Determines if the model should be created for each query.
     */
    private boolean createModel;

    /**
     * Determines if the component has been laid out.
     */
    private boolean initialLayout = true;

    /**
     * If true, denotes that the current layout is the 'results layout'.
     * If false, denotes that the current layout is the 'no results layout'.
     */
    private boolean resultsLayout = false;


    /**
     * Construct a new <code>TableBrowser</code> that queries objects using the
     * specified query, displaying them in the table.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be <code>null</code>
     * @param model   the table model. If <tt>null</tt>, one will be created on each query
     * @param context the layout context
     */
    public TableBrowser(Query<T> query, SortConstraint[] sort, IMTableModel<T> model, LayoutContext context) {
        super(query, sort);
        this.model = model;
        this.context = context;
        createModel = (model == null);
        if (model != null) {
            registerTableChangeListener(model);
        }
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <tt>null</tt> if none has been selected.
     */
    public T getSelected() {
        return (table != null) ? table.getTable().getSelected() : null;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        getTable().getTable().setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<T> getObjects() {
        return (model != null) ? model.getObjects() : Collections.<T>emptyList();
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        Component component = getComponent();
        if (createModel) {
            // Destroy any existing model and tsble. These will be recreated in getTable().
            destroyTable();
            initialLayout = true;
        }

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
     * Sets focus on the results.
     */
    public void setFocusOnResults() {
        IMTable<T> table = getTable().getTable();
        if (!table.getObjects().isEmpty() && table.isFocusTraversalParticipant()) {
            FocusHelper.setFocus(table);
        }
    }

    /**
     * Determines if a result set has results.
     *
     * @param set the result set
     * @return <tt>true</tt> if the result set has results
     */
    protected boolean hasResults(ResultSet<T> set) {
        boolean hasResults = false;
        if (set != null) {
            IPage<T> page = set.getPage(0);
            if (page != null) {
                hasResults = !page.getResults().isEmpty();
            }
        }
        return hasResults;
    }

    /**
     * Lays out the container to display results.
     *
     * @param container the container
     */
    protected void doLayoutForResults(Component container) {
        PagedIMTable<T> table = getTable();
        container.add(table);
        getFocusGroup().add(table);
    }

    /**
     * Lays out the container when there are no results to display.
     *
     * @param container the container
     */
    protected void doLayoutForNoResults(Component container) {
        Label label = LabelFactory.create("browser.noresults", "bold");
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        label.setLayoutData(layout);

        container.add(label);
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    protected PagedIMTable<T> createTable(IMTableModel<T> model) {
        return new PagedIMTable<T>(model);
    }

    /**
     * Returns the underlying table, creating it if it doesn't exist.
     *
     * @return the table
     */
    protected PagedIMTable<T> getTable() {
        if (table == null) {
            if (model == null) {
                model = createTableModel(context);
                registerTableChangeListener(model);
            }
            table = createTable(model);
            table.getTable().addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onSelect();
                }
            });
            table.getTable().addPropertyChangeListener(Table.SELECTION_CHANGED_PROPERTY, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    onBrowse();
                }
            });
        }
        return table;
    }

    /**
     * Creates a table model.
     * <p/>
     * Subclasses must override this method if they do not specify a model at construction.
     *
     * @param context the layout context
     * @return a table model
     */
    protected IMTableModel<T> createTableModel(LayoutContext context) {
        throw new IllegalStateException("No table model has been registered");
    }

    /**
     * Returns the underlying table model.
     *
     * @return the table model
     */
    protected IMTableModel<T> getTableModel() {
        return model;
    }

    /**
     * Registers a listener for when the model updates.
     * This implementation updates the browser's sort criteria so that it is preserved across queries.
     *
     * @param model the model
     */
    protected void registerTableChangeListener(IMTableModel<T> model) {
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
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
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return context;
    }

    /**
     * Adds the table to the browser container.
     *
     * @param container  the browser container
     * @param hasResults determines if there are results to display
     */
    private void doLayout(Component container, boolean hasResults) {
        if (initialLayout || (hasResults != resultsLayout)) {
            switchLayout(container, hasResults);
            initialLayout = false;
        }
        resultsLayout = hasResults;
    }

    /**
     * Switches between the layout for displaying results and no results.
     *
     * @param container the container
     * @param results   if <tt>true</tt> denotes that there are results to display
     */
    private void switchLayout(Component container, boolean results) {
        destroyTable();
        container.removeAll();
        doLayout(container);
        if (results) {
            doLayoutForResults(container);
        } else {
            doLayoutForNoResults(container);
        }
    }

    /**
     * Destroys the existing table, if any.
     */
    private void destroyTable() {
        if (table != null) {
            getFocusGroup().remove(table);
            table = null;
        }
        if (createModel) {
            model = null;
        }
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        T selected = getSelected();
        if (selected != null) {
            notifySelected(selected);
        }
    }

    /**
     * Notifies any listeners when an object is browsed.
     */
    private void onBrowse() {
        T selected = getSelected();
        if (selected != null) {
            notifyBrowsed(selected);
        }
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
        public Memento(TableBrowser<T> browser) {
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
            return browser instanceof TableBrowser;
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
